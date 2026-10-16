package com.smartelectricity.service;

import com.smartelectricity.dto.DashboardStatsResponse;
import com.smartelectricity.model.*;
import com.smartelectricity.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service aggregating dashboard statistics and system health indicators.
 */
@Service
public class DashboardService {
    private final NetworkNodeRepository nodeRepository;
    private final FeederLineRepository feederRepository;
    private final FaultReportRepository faultRepository;
    private final RestorationJobRepository jobRepository;
    private final RepairTeamRepository teamRepository;
    private final OutageDetectionService outageDetectionService;

    public DashboardService(NetworkNodeRepository nodeRepository,
                            FeederLineRepository feederRepository,
                            FaultReportRepository faultRepository,
                            RestorationJobRepository jobRepository,
                            RepairTeamRepository teamRepository,
                            OutageDetectionService outageDetectionService) {
        this.nodeRepository = nodeRepository;
        this.feederRepository = feederRepository;
        this.faultRepository = faultRepository;
        this.jobRepository = jobRepository;
        this.teamRepository = teamRepository;
        this.outageDetectionService = outageDetectionService;
    }

    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        List<NetworkNode> nodes = nodeRepository.findAll();
        List<FeederLine> feeders = feederRepository.findAll();
        List<RepairTeam> teams = teamRepository.findAll();
        List<FaultReport> activeFaults = faultRepository.findActiveFaults();
        List<RestorationJob> activeJobs = jobRepository.findActiveJobs();

        int substations = 0;
        int activeSources = 0;
        int consumerAreas = 0;
        int criticalConsumerAreas = 0;
        int totalConsumers = 0;

        for (NetworkNode n : nodes) {
            if (n instanceof Substation sub) {
                substations++;
                if (sub.isSourceSubstation()) activeSources++;
            } else if (n instanceof ConsumerArea ca) {
                consumerAreas++;
                totalConsumers += ca.getConsumerCount();
                if (ca.isCritical()) criticalConsumerAreas++;
            }
        }

        int workingFeeders = 0;
        int failedFeeders = 0;
        for (FeederLine f : feeders) {
            if (f.isWorking()) workingFeeders++;
            else failedFeeders++;
        }

        int availableTeams = 0;
        int busyTeams = 0;
        for (RepairTeam t : teams) {
            if (t.isAvailable()) availableTeams++;
            else busyTeams++;
        }

        // Run live outage BFS detection
        OutageDetectionResult outageResult = outageDetectionService.detectOutages();

        stats.setTotalNodes(nodes.size());
        stats.setTotalSubstations(substations);
        stats.setActiveSources(activeSources);
        stats.setTotalConsumerAreas(consumerAreas);
        stats.setCriticalConsumerAreas(criticalConsumerAreas);
        stats.setTotalConsumers(totalConsumers);
        stats.setTotalFeeders(feeders.size());
        stats.setWorkingFeeders(workingFeeders);
        stats.setFailedFeeders(failedFeeders);
        stats.setActiveFaults(activeFaults.size());
        stats.setQueuedRestorationJobs(activeJobs.size());
        stats.setAvailableTeams(availableTeams);
        stats.setBusyTeams(busyTeams);
        stats.setAffectedConsumers(outageResult.getTotalConsumersAffected());
        stats.setAffectedCriticalFacilities(outageResult.getTotalCriticalFacilitiesAffected());

        // Grid health percentage
        if (consumerAreas > 0) {
            int energizedConsumers = consumerAreas - outageResult.getDisconnectedConsumerAreas().size();
            double health = ((double) energizedConsumers / consumerAreas) * 100.0;
            stats.setGridHealthPercentage(Math.round(health * 10.0) / 10.0);
        } else {
            stats.setGridHealthPercentage(100.0);
        }

        // Status classification
        if (activeSources == 0) {
            stats.setGridStatus("TOTAL_BLACKOUT");
        } else if (outageResult.getTotalCriticalFacilitiesAffected() > 0) {
            stats.setGridStatus("CRITICAL_OUTAGE");
        } else if (outageResult.getDisconnectedConsumerAreas().size() > 0) {
            stats.setGridStatus("PARTIAL_OUTAGE");
        } else {
            stats.setGridStatus("NORMAL");
        }

        return stats;
    }
}
