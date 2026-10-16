package com.smartelectricity.service;

import com.smartelectricity.model.*;
import com.smartelectricity.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service managing restoration jobs, priority ranking using Java PriorityQueue,
 * and repair team dispatching.
 */
@Service
public class RestorationService {
    private static final Logger log = LoggerFactory.getLogger(RestorationService.class);

    private final RestorationJobRepository jobRepository;
    private final FaultReportRepository faultRepository;
    private final RepairTeamRepository teamRepository;
    private final FeederLineRepository feederRepository;
    private final NetworkNodeRepository nodeRepository;
    private final EventHistoryRepository historyRepository;
    private final OutageDetectionService outageDetectionService;
    private final DistributionNetworkService networkService;

    public RestorationService(RestorationJobRepository jobRepository,
                              FaultReportRepository faultRepository,
                              RepairTeamRepository teamRepository,
                              FeederLineRepository feederRepository,
                              NetworkNodeRepository nodeRepository,
                              EventHistoryRepository historyRepository,
                              @Lazy OutageDetectionService outageDetectionService,
                              DistributionNetworkService networkService) {
        this.jobRepository = jobRepository;
        this.faultRepository = faultRepository;
        this.teamRepository = teamRepository;
        this.feederRepository = feederRepository;
        this.nodeRepository = nodeRepository;
        this.historyRepository = historyRepository;
        this.outageDetectionService = outageDetectionService;
        this.networkService = networkService;
    }

    /**
     * Recalculates restoration rankings using a Java PriorityQueue.
     * Evaluates all active (unresolved) faults.
     */
    @Transactional
    public synchronized List<RestorationJob> recalculatePriorities() {
        List<FaultReport> activeFaults = faultRepository.findActiveFaults();
        if (activeFaults.isEmpty()) {
            return Collections.emptyList();
        }

        DistributionNetwork network = networkService.getNetwork();
        OutageDetectionResult currentOutage = outageDetectionService.detectOutages();

        // Instantiate Java PriorityQueue with custom RestorationPolicy Comparator
        PriorityQueue<RestorationJob> priorityQueue = new PriorityQueue<>(new RestorationPolicy());

        for (FaultReport fault : activeFaults) {
            String feederId = fault.getFeederId();
            FeederLine feeder = network.getFeeder(feederId);

            int criticalCount = 0;
            int affectedConsumers = 0;

            if (feeder != null) {
                // Benefit simulation: how many loads are energized if this feeder is restored?
                Set<String> simulateWorking = Collections.singleton(feederId);
                OutageDetectionResult simulated = outageDetectionService.runOutageDetection(
                        network, Collections.emptySet(), simulateWorking
                );

                int critBenefit = Math.max(0, currentOutage.getTotalCriticalFacilitiesAffected() - simulated.getTotalCriticalFacilitiesAffected());
                int consBenefit = Math.max(0, currentOutage.getTotalConsumersAffected() - simulated.getTotalConsumersAffected());

                // Also inspect endpoints of the feeder
                NetworkNode srcNode = network.getNode(feeder.getSourceNodeId());
                NetworkNode tgtNode = network.getNode(feeder.getTargetNodeId());

                int directCrit = 0;
                int directCons = 0;
                if (srcNode instanceof ConsumerArea ca && currentOutage.getDeenergizedNodeIds().contains(ca.getNodeId())) {
                    directCons += ca.getConsumerCount();
                    if (ca instanceof CriticalArea) directCrit++;
                }
                if (tgtNode instanceof ConsumerArea ca && currentOutage.getDeenergizedNodeIds().contains(ca.getNodeId())) {
                    directCons += ca.getConsumerCount();
                    if (ca instanceof CriticalArea) directCrit++;
                }

                criticalCount = Math.max(critBenefit, directCrit);
                affectedConsumers = Math.max(consBenefit, directCons);

                // If outage is network-wide or feeder is an essential branch, ensure non-zero priority metrics
                if (criticalCount == 0 && currentOutage.getTotalCriticalFacilitiesAffected() > 0) {
                    criticalCount = 1;
                }
                if (affectedConsumers == 0 && currentOutage.getTotalConsumersAffected() > 0) {
                    affectedConsumers = 100;
                }
            }

            // Look up existing job or create new one
            Optional<RestorationJob> existingOpt = jobRepository.findByFaultId(fault.getFaultId());
            RestorationJob job;
            if (existingOpt.isPresent()) {
                job = existingOpt.get();
                job.setCriticalFacilitiesCount(criticalCount);
                job.setAffectedConsumersCount(affectedConsumers);
                job.setFaultReportedAt(fault.getReportedAt());
                job.calculateScore();
            } else {
                String jobId = "JOB-" + fault.getFaultId().replace("FLT-", "");
                job = new RestorationJob(jobId, fault.getFaultId(), feederId,
                        criticalCount, affectedConsumers, fault.getReportedAt());
            }

            priorityQueue.offer(job);
        }

        // Drain PriorityQueue in exact priority order to assign ranks
        List<RestorationJob> rankedJobs = new ArrayList<>();
        int rank = 1;
        while (!priorityQueue.isEmpty()) {
            RestorationJob rankedJob = priorityQueue.poll();
            rankedJob.setPriorityRank(rank++);

            // Save or update in database
            if (jobRepository.findById(rankedJob.getJobId()).isPresent()) {
                jobRepository.update(rankedJob);
            } else {
                jobRepository.save(rankedJob);
            }
            rankedJobs.add(rankedJob);
        }

        return rankedJobs;
    }

    public List<RestorationJob> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<RestorationJob> getActiveJobs() {
        return jobRepository.findActiveJobs();
    }

    /**
     * Dispatches an available repair team to a restoration job.
     */
    @Transactional
    public synchronized RestorationJob assignRepairTeam(String identifier, String teamId) {
        // Resolve job by jobId or faultId
        Optional<RestorationJob> jobOpt = jobRepository.findById(identifier);
        if (jobOpt.isEmpty()) {
            jobOpt = jobRepository.findByFaultId(identifier);
        }
        if (jobOpt.isEmpty()) {
            throw new IllegalArgumentException("Restoration job not found for identifier: " + identifier);
        }
        RestorationJob job = jobOpt.get();

        Optional<RepairTeam> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new IllegalArgumentException("Repair team not found: " + teamId);
        }
        RepairTeam team = teamOpt.get();

        if (!team.isAvailable()) {
            throw new IllegalStateException("Repair team " + team.getTeamName() + " (" + teamId + ") is currently " + team.getStatus() + " and cannot be assigned.");
        }

        // Update team
        teamRepository.updateStatus(teamId, "BUSY", job.getJobId());

        // Update job
        job.setStatus("ASSIGNED");
        job.setAssignedTeamId(teamId);
        jobRepository.update(job);

        // Update fault report
        faultRepository.updateStatus(job.getFaultId(), "ASSIGNED", teamId, null);

        // Log history
        historyRepository.save(new EventHistory(
                "TEAM_ASSIGNED",
                job.getJobId(),
                String.format("Assigned team %s (%s) to restoration job %s on feeder %s",
                        team.getTeamName(), teamId, job.getJobId(), job.getFeederId()),
                "Operator"
        ));

        return job;
    }

    /**
     * Completes repair work, restores feeder line to WORKING, and recalculates outages.
     */
    @Transactional
    public synchronized RestorationJob completeRestoration(String identifier) {
        Optional<RestorationJob> jobOpt = jobRepository.findById(identifier);
        if (jobOpt.isEmpty()) {
            jobOpt = jobRepository.findByFaultId(identifier);
        }
        if (jobOpt.isEmpty()) {
            throw new IllegalArgumentException("Restoration job not found for identifier: " + identifier);
        }
        RestorationJob job = jobOpt.get();

        String feederId = job.getFeederId();
        String faultId = job.getFaultId();
        String teamId = job.getAssignedTeamId();

        // 1. Restore feeder line in DB and memory
        feederRepository.updateStatus(feederId, "WORKING");
        FeederLine feeder = networkService.getNetwork().getFeeder(feederId);
        if (feeder != null) {
            feeder.setStatus("WORKING");
        }

        // 2. Mark fault report as RESOLVED
        faultRepository.updateStatus(faultId, "RESOLVED", teamId, LocalDateTime.now());

        // 3. Mark restoration job as RESTORED
        job.setStatus("RESTORED");
        jobRepository.update(job);

        // 4. Free up repair team
        if (teamId != null && !teamId.isBlank()) {
            teamRepository.updateStatus(teamId, "AVAILABLE", null);
        }

        // 5. Recalculate network and priorities
        networkService.reloadNetworkFromDatabase();
        recalculatePriorities();

        // 6. Log history
        historyRepository.save(new EventHistory(
                "FEEDER_RESTORED",
                feederId,
                String.format("Feeder %s restored to WORKING following job %s resolution. Fault %s closed.",
                        feederId, job.getJobId(), faultId),
                "Operator"
        ));

        return job;
    }
}
