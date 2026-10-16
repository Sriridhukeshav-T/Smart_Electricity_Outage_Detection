package com.smartelectricity.service;

import com.smartelectricity.dto.CreateFeederRequest;
import com.smartelectricity.dto.CreateNodeRequest;
import com.smartelectricity.dto.UpdateFeederRequest;
import com.smartelectricity.dto.UpdateNodeRequest;
import com.smartelectricity.model.*;
import com.smartelectricity.repository.ApprovedBackupRepository;
import com.smartelectricity.repository.EventHistoryRepository;
import com.smartelectricity.repository.FaultReportRepository;
import com.smartelectricity.repository.FeederLineRepository;
import com.smartelectricity.repository.NetworkNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service managing dynamic network CRUD operations, topology updates,
 * and comprehensive integrity validations.
 */
@Service
public class NetworkManagementService {
    private final NetworkNodeRepository nodeRepository;
    private final FeederLineRepository feederRepository;
    private final ApprovedBackupRepository backupRepository;
    private final FaultReportRepository faultRepository;
    private final EventHistoryRepository historyRepository;
    private final DistributionNetworkService networkService;
    private final RestorationService restorationService;

    public NetworkManagementService(NetworkNodeRepository nodeRepository,
                                    FeederLineRepository feederRepository,
                                    ApprovedBackupRepository backupRepository,
                                    FaultReportRepository faultRepository,
                                    EventHistoryRepository historyRepository,
                                    DistributionNetworkService networkService,
                                    RestorationService restorationService) {
        this.nodeRepository = nodeRepository;
        this.feederRepository = feederRepository;
        this.backupRepository = backupRepository;
        this.faultRepository = faultRepository;
        this.historyRepository = historyRepository;
        this.networkService = networkService;
        this.restorationService = restorationService;
    }

    public List<NetworkNode> getAllNodes() {
        return nodeRepository.findAll();
    }

    public Optional<NetworkNode> getNodeById(String nodeId) {
        return nodeRepository.findById(nodeId);
    }

    @Transactional
    public synchronized NetworkNode createNode(CreateNodeRequest req) {
        String nodeId = req.getNodeId().trim().toUpperCase();

        // 1. Validate duplicate ID
        if (nodeRepository.existsById(nodeId)) {
            throw new IllegalArgumentException("Node ID '" + nodeId + "' already exists in the network.");
        }

        NetworkNode node;
        if ("SUBSTATION".equalsIgnoreCase(req.getNodeType())) {
            node = new Substation(
                    nodeId,
                    req.getName().trim(),
                    req.getStatus(),
                    req.getLocationX() != null ? req.getLocationX() : 100.0,
                    req.getLocationY() != null ? req.getLocationY() : 100.0,
                    req.getIsSource() != null ? req.getIsSource() : true,
                    req.getCapacityKva() != null ? req.getCapacityKva() : 25000.0
            );
        } else {
            if ("CRITICAL".equalsIgnoreCase(req.getConsumerType())) {
                node = new CriticalArea(
                        nodeId,
                        req.getName().trim(),
                        req.getStatus(),
                        req.getLocationX() != null ? req.getLocationX() : 100.0,
                        req.getLocationY() != null ? req.getLocationY() : 100.0,
                        req.getConsumerCount() != null ? req.getConsumerCount() : 1000,
                        req.getCriticalFacilityName() != null ? req.getCriticalFacilityName() : "Essential Lifeline Unit"
                );
            } else {
                node = new GeneralArea(
                        nodeId,
                        req.getName().trim(),
                        req.getStatus(),
                        req.getLocationX() != null ? req.getLocationX() : 100.0,
                        req.getLocationY() != null ? req.getLocationY() : 100.0,
                        req.getConsumerCount() != null ? req.getConsumerCount() : 1000
                );
            }
        }

        nodeRepository.save(node);
        networkService.reloadNetworkFromDatabase();

        historyRepository.save(new EventHistory(
                "NODE_CREATED",
                nodeId,
                String.format("Added %s node %s (%s)", node.getNodeType(), node.getName(), nodeId),
                "Operator"
        ));

        return node;
    }

    @Transactional
    public synchronized NetworkNode updateNode(String nodeId, UpdateNodeRequest req) {
        Optional<NetworkNode> existingOpt = nodeRepository.findById(nodeId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Node not found: " + nodeId);
        }

        NetworkNode node = existingOpt.get();
        node.setName(req.getName().trim());
        if (req.getStatus() != null) node.setStatus(req.getStatus());
        if (req.getLocationX() != null) node.setLocationX(req.getLocationX());
        if (req.getLocationY() != null) node.setLocationY(req.getLocationY());

        if (node instanceof Substation sub) {
            if (req.getIsSource() != null) sub.setSource(req.getIsSource());
            if (req.getCapacityKva() != null) sub.setCapacityKva(req.getCapacityKva());
        } else if (node instanceof ConsumerArea ca) {
            if (req.getConsumerCount() != null) ca.setConsumerCount(req.getConsumerCount());
            if (ca instanceof CriticalArea crit && req.getCriticalFacilityName() != null) {
                crit.setCriticalFacilityName(req.getCriticalFacilityName());
            }
        }

        nodeRepository.update(node);
        networkService.reloadNetworkFromDatabase();
        restorationService.recalculatePriorities();

        historyRepository.save(new EventHistory(
                "NODE_UPDATED",
                nodeId,
                "Updated configuration for node: " + nodeId,
                "Operator"
        ));

        return node;
    }

    @Transactional
    public synchronized void toggleNodeStatus(String nodeId, String targetStatus) {
        Optional<NetworkNode> opt = nodeRepository.findById(nodeId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Node not found: " + nodeId);
        }
        NetworkNode node = opt.get();
        String newStatus = targetStatus != null ? targetStatus :
                ("ACTIVE".equalsIgnoreCase(node.getStatus()) ? "INACTIVE" : "ACTIVE");

        nodeRepository.updateStatus(nodeId, newStatus);
        networkService.reloadNetworkFromDatabase();
        restorationService.recalculatePriorities();

        historyRepository.save(new EventHistory(
                "NODE_STATUS_CHANGED",
                nodeId,
                String.format("Status of node %s changed to %s", nodeId, newStatus),
                "Operator"
        ));
    }

    @Transactional
    public synchronized void deleteNode(String nodeId) {
        if (!nodeRepository.existsById(nodeId)) {
            throw new IllegalArgumentException("Node not found: " + nodeId);
        }

        // Validate unsafe deletion: Cannot delete node if referenced by feeder lines!
        int feederCount = feederRepository.countByNodeId(nodeId);
        if (feederCount > 0) {
            throw new IllegalStateException(String.format(
                    "Cannot delete node '%s' because it is referenced by %d feeder line(s). Delete or reassign the feeder lines first.",
                    nodeId, feederCount
            ));
        }

        nodeRepository.deleteById(nodeId);
        networkService.reloadNetworkFromDatabase();

        historyRepository.save(new EventHistory(
                "NODE_DELETED",
                nodeId,
                "Deleted network node: " + nodeId,
                "Operator"
        ));
    }

    // ---------------- Feeder Operations ----------------

    public List<FeederLine> getAllFeeders() {
        return feederRepository.findAll();
    }

    public Optional<FeederLine> getFeederById(String feederId) {
        return feederRepository.findById(feederId);
    }

    @Transactional
    public synchronized FeederLine createFeeder(CreateFeederRequest req) {
        String feederId = req.getFeederId().trim().toUpperCase();
        String srcId = req.getSourceNodeId().trim().toUpperCase();
        String tgtId = req.getTargetNodeId().trim().toUpperCase();

        // 1. Validate duplicate ID
        if (feederRepository.existsById(feederId)) {
            throw new IllegalArgumentException("Feeder ID '" + feederId + "' already exists.");
        }

        // 2. Validate self-connection
        if (srcId.equalsIgnoreCase(tgtId)) {
            throw new IllegalArgumentException("Self-connection is invalid: Source and Target node cannot be the same (" + srcId + ").");
        }

        // 3. Validate endpoints exist
        if (!nodeRepository.existsById(srcId)) {
            throw new IllegalArgumentException("Source node '" + srcId + "' does not exist.");
        }
        if (!nodeRepository.existsById(tgtId)) {
            throw new IllegalArgumentException("Target node '" + tgtId + "' does not exist.");
        }

        FeederLine feeder = new FeederLine(
                feederId,
                req.getName().trim(),
                srcId,
                tgtId,
                req.getStatus() != null ? req.getStatus() : "WORKING",
                req.getCapacityAmps() != null ? req.getCapacityAmps() : 500.0,
                req.getLengthKm() != null ? req.getLengthKm() : 1.0,
                req.getIsBackup() != null ? req.getIsBackup() : false
        );

        feederRepository.save(feeder);
        networkService.reloadNetworkFromDatabase();
        restorationService.recalculatePriorities();

        historyRepository.save(new EventHistory(
                "FEEDER_CREATED",
                feederId,
                String.format("Added feeder line %s connecting %s to %s", feederId, srcId, tgtId),
                "Operator"
        ));

        return feeder;
    }

    @Transactional
    public synchronized FeederLine updateFeeder(String feederId, UpdateFeederRequest req) {
        Optional<FeederLine> opt = feederRepository.findById(feederId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Feeder line not found: " + feederId);
        }

        FeederLine feeder = opt.get();
        feeder.setName(req.getName().trim());
        if (req.getStatus() != null) feeder.setStatus(req.getStatus());
        if (req.getCapacityAmps() != null) feeder.setCapacityAmps(req.getCapacityAmps());
        if (req.getLengthKm() != null) feeder.setLengthKm(req.getLengthKm());
        if (req.getIsBackup() != null) feeder.setBackup(req.getIsBackup());

        feederRepository.update(feeder);
        networkService.reloadNetworkFromDatabase();
        restorationService.recalculatePriorities();

        historyRepository.save(new EventHistory(
                "FEEDER_UPDATED",
                feederId,
                "Updated feeder configuration for: " + feederId,
                "Operator"
        ));

        return feeder;
    }

    @Transactional
    public synchronized void setFeederStatus(String feederId, String status) {
        Optional<FeederLine> opt = feederRepository.findById(feederId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Feeder line not found: " + feederId);
        }

        feederRepository.updateStatus(feederId, status);
        networkService.reloadNetworkFromDatabase();
        restorationService.recalculatePriorities();

        historyRepository.save(new EventHistory(
                "FEEDER_STATUS_CHANGED",
                feederId,
                String.format("Feeder %s status changed to %s", feederId, status),
                "Operator"
        ));
    }

    @Transactional
    public synchronized void deleteFeeder(String feederId) {
        if (!feederRepository.existsById(feederId)) {
            throw new IllegalArgumentException("Feeder line not found: " + feederId);
        }

        // Validate unsafe deletion: Cannot delete if referenced in backup configs
        int backupCount = backupRepository.countByFeederId(feederId);
        if (backupCount > 0) {
            throw new IllegalStateException(String.format(
                    "Cannot delete feeder line '%s' because it is referenced in %d approved backup configuration(s).",
                    feederId, backupCount
            ));
        }

        // Validate unsafe deletion: Cannot delete if active fault report exists
        Optional<FaultReport> activeFault = faultRepository.findActiveFaultByFeederId(feederId);
        if (activeFault.isPresent()) {
            throw new IllegalStateException(String.format(
                    "Cannot delete feeder line '%s' because an active fault report (%s) exists on it.",
                    feederId, activeFault.get().getFaultId()
            ));
        }

        feederRepository.deleteById(feederId);
        networkService.reloadNetworkFromDatabase();
        restorationService.recalculatePriorities();

        historyRepository.save(new EventHistory(
                "FEEDER_DELETED",
                feederId,
                "Deleted feeder line: " + feederId,
                "Operator"
        ));
    }
}
