package com.smartelectricity.service;

import com.smartelectricity.dto.BackupPathStatusDto;
import com.smartelectricity.dto.CreateBackupRequest;
import com.smartelectricity.model.*;
import com.smartelectricity.repository.ApprovedBackupRepository;
import com.smartelectricity.repository.EventHistoryRepository;
import com.smartelectricity.repository.FeederLineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service managing approved backup routes and verifying reachability.
 */
@Service
public class BackupConnectionService {
    private final ApprovedBackupRepository backupRepository;
    private final FeederLineRepository feederRepository;
    private final EventHistoryRepository historyRepository;
    private final DistributionNetworkService networkService;
    private final OutageDetectionService outageDetectionService;

    public BackupConnectionService(ApprovedBackupRepository backupRepository,
                                   FeederLineRepository feederRepository,
                                   EventHistoryRepository historyRepository,
                                   DistributionNetworkService networkService,
                                   OutageDetectionService outageDetectionService) {
        this.backupRepository = backupRepository;
        this.feederRepository = feederRepository;
        this.historyRepository = historyRepository;
        this.networkService = networkService;
        this.outageDetectionService = outageDetectionService;
    }

    public List<ApprovedBackupConnection> getAllBackups() {
        return backupRepository.findAll();
    }

    public Optional<ApprovedBackupConnection> getBackupById(String backupId) {
        return backupRepository.findById(backupId);
    }

    @Transactional
    public synchronized ApprovedBackupConnection createBackup(CreateBackupRequest req) {
        String backupId = req.getBackupId().trim().toUpperCase();
        String primaryId = req.getPrimaryFeederId().trim();
        String alternateId = req.getAlternateFeederId().trim();

        if (backupRepository.existsById(backupId)) {
            throw new IllegalArgumentException("Backup ID '" + backupId + "' already exists.");
        }
        if (!feederRepository.existsById(primaryId)) {
            throw new IllegalArgumentException("Primary feeder line '" + primaryId + "' does not exist.");
        }
        if (!feederRepository.existsById(alternateId)) {
            throw new IllegalArgumentException("Alternate feeder line '" + alternateId + "' does not exist.");
        }
        if (primaryId.equalsIgnoreCase(alternateId)) {
            throw new IllegalArgumentException("Primary and Alternate feeders cannot be identical.");
        }

        ApprovedBackupConnection backup = new ApprovedBackupConnection(
                backupId,
                req.getName().trim(),
                primaryId,
                alternateId,
                req.getDescription(),
                req.getIsApproved() != null ? req.getIsApproved() : true
        );

        backupRepository.save(backup);

        historyRepository.save(new EventHistory(
                "BACKUP_CONFIG_CREATED",
                backupId,
                String.format("Added approved backup configuration %s for primary feeder %s via %s",
                        backupId, primaryId, alternateId),
                "Operator"
        ));

        return backup;
    }

    @Transactional
    public synchronized void deleteBackup(String backupId) {
        if (!backupRepository.existsById(backupId)) {
            throw new IllegalArgumentException("Backup ID not found: " + backupId);
        }
        backupRepository.deleteById(backupId);

        historyRepository.save(new EventHistory(
                "BACKUP_CONFIG_DELETED",
                backupId,
                "Deleted backup configuration: " + backupId,
                "Operator"
        ));
    }

    /**
     * Evaluates all approved backup connections against live network topology and active sources.
     */
    public List<BackupPathStatusDto> evaluateAllBackupRoutes() {
        List<ApprovedBackupConnection> backups = backupRepository.findAll();
        List<BackupPathStatusDto> results = new ArrayList<>();
        DistributionNetwork network = networkService.getNetwork();

        for (ApprovedBackupConnection backup : backups) {
            results.add(evaluateSingleBackup(backup, network));
        }

        return results;
    }

    public BackupPathStatusDto evaluateSingleBackup(ApprovedBackupConnection backup, DistributionNetwork network) {
        BackupPathStatusDto dto = new BackupPathStatusDto();
        dto.setBackupId(backup.getBackupId());
        dto.setName(backup.getName());
        dto.setPrimaryFeederId(backup.getPrimaryFeederId());
        dto.setAlternateFeederId(backup.getAlternateFeederId());

        FeederLine primaryFeeder = network.getFeeder(backup.getPrimaryFeederId());
        FeederLine alternateFeeder = network.getFeeder(backup.getAlternateFeederId());

        if (primaryFeeder == null || alternateFeeder == null) {
            dto.setPathAnalysis("Configuration error: One or both configured feeder lines do not exist in active topology.");
            dto.setAlternatePathAvailable(false);
            dto.setCanRestoreAffectedLoad(false);
            return dto;
        }

        dto.setPrimaryFeederStatus(primaryFeeder.getStatus());
        dto.setPrimaryFeederSource(primaryFeeder.getSourceNodeId());
        dto.setPrimaryFeederTarget(primaryFeeder.getTargetNodeId());

        dto.setAlternateFeederStatus(alternateFeeder.getStatus());
        dto.setAlternateFeederSource(alternateFeeder.getSourceNodeId());
        dto.setAlternateFeederTarget(alternateFeeder.getTargetNodeId());

        boolean isPrimaryFailed = !primaryFeeder.isWorking();
        boolean isAlternateWorking = alternateFeeder.isWorking();

        // Check if alternate feeder is usable (must be working)
        if (!isAlternateWorking) {
            dto.setAlternatePathAvailable(false);
            dto.setCanRestoreAffectedLoad(false);
            dto.setPathAnalysis(String.format(
                    "BLOCKED: Alternate backup line %s is in FAILED status. Cannot be energized.",
                    alternateFeeder.getFeederId()
            ));
            return dto;
        }

        // Verify if alternate line actually connects to an active source
        OutageDetectionResult simResult = outageDetectionService.runOutageDetection(
                network,
                Collections.emptySet(),
                Collections.singleton(alternateFeeder.getFeederId())
        );

        boolean altSourceEnergized = simResult.getEnergizedNodeIds().contains(alternateFeeder.getSourceNodeId());
        boolean altTargetEnergized = simResult.getEnergizedNodeIds().contains(alternateFeeder.getTargetNodeId());
        boolean hasSourcePath = altSourceEnergized || altTargetEnergized;

        dto.setAlternatePathAvailable(hasSourcePath);

        if (isPrimaryFailed) {
            if (hasSourcePath) {
                dto.setCanRestoreAffectedLoad(true);
                dto.setPathAnalysis(String.format(
                        "READY FOR DISPATCH: Primary feeder %s is FAILED. Approved backup route %s is functional and connected to an active source. Ready to reroute load.",
                        primaryFeeder.getFeederId(), alternateFeeder.getFeederId()
                ));
            } else {
                dto.setCanRestoreAffectedLoad(false);
                dto.setPathAnalysis(String.format(
                        "UNAVAILABLE: Alternate line %s is working, but neither endpoint is reachable from any active substation.",
                        alternateFeeder.getFeederId()
                ));
            }
        } else {
            dto.setCanRestoreAffectedLoad(false);
            dto.setPathAnalysis(String.format(
                    "STANDBY: Primary line %s is WORKING normally. Backup route %s is in healthy standby mode.",
                    primaryFeeder.getFeederId(), alternateFeeder.getFeederId()
            ));
        }

        return dto;
    }
}
