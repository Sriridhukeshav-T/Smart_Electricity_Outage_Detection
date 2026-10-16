package com.smartelectricity.service;

import com.smartelectricity.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core Outage Detection Engine.
 * Implements Multi-Source Breadth-First Search (BFS) using:
 * - Queue / ArrayDeque for level-order BFS traversal.
 * - HashSet for visited-node tracking in O(1) time.
 * - HashMap / Adjacency-list for graph edge traversal.
 */
@Service
public class OutageDetectionService {
    private static final Logger log = LoggerFactory.getLogger(OutageDetectionService.class);

    private final DistributionNetworkService networkService;

    public OutageDetectionService(DistributionNetworkService networkService) {
        this.networkService = networkService;
    }

    /**
     * Performs Outage Detection using Multi-Source BFS on the live network topology.
     */
    public OutageDetectionResult detectOutages() {
        DistributionNetwork network = networkService.getNetwork();
        return runOutageDetection(network, Collections.emptySet(), Collections.emptySet());
    }

    /**
     * Parameterized Outage Detection supporting what-if simulations.
     * @param network The distribution network graph
     * @param simulatedFailedFeeders Additional feeder IDs to treat as failed
     * @param simulatedActiveFeeders Feeder IDs to force treat as working (e.g. backup line energization)
     */
    public OutageDetectionResult runOutageDetection(DistributionNetwork network,
                                                   Set<String> simulatedFailedFeeders,
                                                   Set<String> simulatedActiveFeeders) {
        OutageDetectionResult result = new OutageDetectionResult();
        result.setEvaluatedAt(LocalDateTime.now());

        // 1. Identify all active source substations
        List<Substation> activeSources = network.getActiveSourceSubstations();
        result.setActiveSourcesCount(activeSources.size());

        List<String> sourceIds = new ArrayList<>();
        for (Substation s : activeSources) {
            sourceIds.add(s.getNodeId());
        }
        result.setActiveSourceNodeIds(sourceIds);

        Collection<NetworkNode> allNodes = network.getAllNodes();
        List<ConsumerArea> allConsumerAreas = network.getAllConsumerAreas();

        // Check edge case: If ALL sources are inactive, total grid blackout occurs!
        if (activeSources.isEmpty()) {
            log.warn("Outage Detection: No active source substation found in distribution grid.");
            result.setGridBlackout(true);
            result.setStatusMessage("CRITICAL: No active power sources currently available. Complete grid de-energization.");

            Set<String> deenergized = new HashSet<>();
            for (NetworkNode n : allNodes) {
                deenergized.add(n.getNodeId());
            }
            result.setDeenergizedNodeIds(deenergized);
            result.setEnergizedNodeIds(Collections.emptySet());

            int totalAffectedConsumers = 0;
            List<CriticalArea> critList = new ArrayList<>();
            for (ConsumerArea ca : allConsumerAreas) {
                totalAffectedConsumers += ca.getConsumerCount();
                if (ca instanceof CriticalArea caCrit) {
                    critList.add(caCrit);
                }
            }
            result.setDisconnectedConsumerAreas(new ArrayList<>(allConsumerAreas));
            result.setDisconnectedCriticalAreas(critList);
            result.setTotalConsumersAffected(totalAffectedConsumers);
            result.setTotalCriticalFacilitiesAffected(critList.size());
            return result;
        }

        // 2. Multi-Source BFS Initialisation
        // Java ArrayDeque implemented as standard FIFO Queue
        Queue<String> queue = new ArrayDeque<>();
        // Java HashSet for fast O(1) visited lookups and cycle prevention
        Set<String> visited = new HashSet<>();

        // Enqueue all active source substations
        for (Substation source : activeSources) {
            queue.offer(source.getNodeId());
            visited.add(source.getNodeId());
        }

        // 3. BFS Graph Traversal
        while (!queue.isEmpty()) {
            String currentNodeId = queue.poll();

            // Fetch incident feeder lines from the adjacency list
            List<FeederLine> incidentFeeders = network.getIncidentFeeders(currentNodeId);

            for (FeederLine feeder : incidentFeeders) {
                String feederId = feeder.getFeederId();

                // Determine effective operational status of this feeder
                boolean isFailed = feeder.getStatus().equalsIgnoreCase("FAILED")
                        || simulatedFailedFeeders.contains(feederId);
                if (simulatedActiveFeeders.contains(feederId)) {
                    isFailed = false;
                }

                // Electricity only flows across functional lines
                if (isFailed) {
                    continue;
                }

                String neighborId = feeder.getOtherNode(currentNodeId);
                if (neighborId == null) {
                    continue;
                }

                NetworkNode neighborNode = network.getNode(neighborId);
                if (neighborNode == null || !neighborNode.isActive()) {
                    // Inactive nodes cannot conduct or receive power
                    continue;
                }

                // If not visited, mark energized and add to queue
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    queue.offer(neighborId);
                }
            }
        }

        // 4. Analyze Reachability Results
        result.setEnergizedNodeIds(visited);

        Set<String> deenergizedNodeIds = new HashSet<>();
        List<ConsumerArea> disconnectedConsumers = new ArrayList<>();
        List<CriticalArea> disconnectedCritical = new ArrayList<>();
        int totalAffectedConsumers = 0;

        for (NetworkNode node : allNodes) {
            String id = node.getNodeId();
            if (!visited.contains(id)) {
                deenergizedNodeIds.add(id);

                if (node instanceof ConsumerArea consumerArea) {
                    disconnectedConsumers.add(consumerArea);
                    totalAffectedConsumers += consumerArea.getConsumerCount();

                    if (consumerArea instanceof CriticalArea criticalArea) {
                        disconnectedCritical.add(criticalArea);
                    }
                }
            }
        }

        result.setDeenergizedNodeIds(deenergizedNodeIds);
        result.setDisconnectedConsumerAreas(disconnectedConsumers);
        result.setDisconnectedCriticalAreas(disconnectedCritical);
        result.setTotalConsumersAffected(totalAffectedConsumers);
        result.setTotalCriticalFacilitiesAffected(disconnectedCritical.size());

        if (disconnectedConsumers.isEmpty()) {
            result.setGridBlackout(false);
            result.setStatusMessage("Normal Grid Operation: All consumer areas energized from active sources.");
        } else {
            result.setGridBlackout(disconnectedConsumers.size() == allConsumerAreas.size());
            result.setStatusMessage(String.format(
                    "Outage Detected: %d consumer areas de-energized (%d critical facilities, %d total consumers affected).",
                    disconnectedConsumers.size(), disconnectedCritical.size(), totalAffectedConsumers
            ));
        }

        return result;
    }
}
