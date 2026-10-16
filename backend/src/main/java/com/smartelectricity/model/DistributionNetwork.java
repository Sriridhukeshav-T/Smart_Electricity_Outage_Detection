package com.smartelectricity.model;

import java.util.*;

/**
 * Encapsulates the electrical distribution network graph structure.
 * Implemented using an Adjacency-List Graph, HashMaps for O(1) lookups,
 * and ArrayLists for edge incidence.
 */
public class DistributionNetwork {
    private final Map<String, NetworkNode> nodeMap;
    private final Map<String, FeederLine> feederMap;
    private final Map<String, List<FeederLine>> adjacencyList;

    public DistributionNetwork() {
        this.nodeMap = new HashMap<>();
        this.feederMap = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public synchronized void clear() {
        nodeMap.clear();
        feederMap.clear();
        adjacencyList.clear();
    }

    public synchronized void addNode(NetworkNode node) {
        if (node == null || node.getNodeId() == null) return;
        nodeMap.put(node.getNodeId(), node);
        adjacencyList.putIfAbsent(node.getNodeId(), new ArrayList<>());
    }

    public synchronized NetworkNode getNode(String nodeId) {
        return nodeMap.get(nodeId);
    }

    public synchronized boolean hasNode(String nodeId) {
        return nodeMap.containsKey(nodeId);
    }

    public synchronized Collection<NetworkNode> getAllNodes() {
        return Collections.unmodifiableCollection(nodeMap.values());
    }

    public synchronized void removeNode(String nodeId) {
        nodeMap.remove(nodeId);
        adjacencyList.remove(nodeId);
        // Also remove any feeders incident to this node
        List<String> toRemove = new ArrayList<>();
        for (FeederLine feeder : feederMap.values()) {
            if (feeder.connectsNode(nodeId)) {
                toRemove.add(feeder.getFeederId());
            }
        }
        for (String feederId : toRemove) {
            removeFeeder(feederId);
        }
    }

    public synchronized void addFeeder(FeederLine feeder) {
        if (feeder == null || feeder.getFeederId() == null) return;
        feederMap.put(feeder.getFeederId(), feeder);

        // Ensure both incident nodes are in adjacency list
        adjacencyList.putIfAbsent(feeder.getSourceNodeId(), new ArrayList<>());
        adjacencyList.putIfAbsent(feeder.getTargetNodeId(), new ArrayList<>());

        // Avoid duplicate edges in adjacency list
        List<FeederLine> srcList = adjacencyList.get(feeder.getSourceNodeId());
        srcList.removeIf(f -> f.getFeederId().equals(feeder.getFeederId()));
        srcList.add(feeder);

        List<FeederLine> tgtList = adjacencyList.get(feeder.getTargetNodeId());
        tgtList.removeIf(f -> f.getFeederId().equals(feeder.getFeederId()));
        tgtList.add(feeder);
    }

    public synchronized FeederLine getFeeder(String feederId) {
        return feederMap.get(feederId);
    }

    public synchronized boolean hasFeeder(String feederId) {
        return feederMap.containsKey(feederId);
    }

    public synchronized Collection<FeederLine> getAllFeeders() {
        return Collections.unmodifiableCollection(feederMap.values());
    }

    public synchronized void removeFeeder(String feederId) {
        FeederLine feeder = feederMap.remove(feederId);
        if (feeder != null) {
            List<FeederLine> srcList = adjacencyList.get(feeder.getSourceNodeId());
            if (srcList != null) srcList.removeIf(f -> f.getFeederId().equals(feederId));

            List<FeederLine> tgtList = adjacencyList.get(feeder.getTargetNodeId());
            if (tgtList != null) tgtList.removeIf(f -> f.getFeederId().equals(feederId));
        }
    }

    public synchronized List<FeederLine> getIncidentFeeders(String nodeId) {
        List<FeederLine> list = adjacencyList.get(nodeId);
        return (list != null) ? Collections.unmodifiableList(new ArrayList<>(list)) : Collections.emptyList();
    }

    /**
     * Retrieves all active source substations to initiate Multi-Source BFS.
     */
    public synchronized List<Substation> getActiveSourceSubstations() {
        List<Substation> sources = new ArrayList<>();
        for (NetworkNode node : nodeMap.values()) {
            if (node instanceof Substation substation && substation.isSourceSubstation()) {
                sources.add(substation);
            }
        }
        return sources;
    }

    /**
     * Retrieves all consumer areas in the network.
     */
    public synchronized List<ConsumerArea> getAllConsumerAreas() {
        List<ConsumerArea> consumers = new ArrayList<>();
        for (NetworkNode node : nodeMap.values()) {
            if (node instanceof ConsumerArea consumerArea) {
                consumers.add(consumerArea);
            }
        }
        return consumers;
    }
}
