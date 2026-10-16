package com.smartelectricity.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Encapsulates the complete result of BFS-based outage detection.
 */
public class OutageDetectionResult {
    private LocalDateTime evaluatedAt;
    private int activeSourcesCount;
    private List<String> activeSourceNodeIds;
    private Set<String> energizedNodeIds;
    private Set<String> deenergizedNodeIds;
    private List<ConsumerArea> disconnectedConsumerAreas;
    private List<CriticalArea> disconnectedCriticalAreas;
    private int totalConsumersAffected;
    private int totalCriticalFacilitiesAffected;
    private String statusMessage;
    private boolean gridBlackout;
    private final String simulationDisclaimer = "Software simulation only. Does not calculate real electrical power flow or provide real-world switching instructions.";

    public OutageDetectionResult() {
        this.evaluatedAt = LocalDateTime.now();
        this.activeSourceNodeIds = new ArrayList<>();
        this.energizedNodeIds = new HashSet<>();
        this.deenergizedNodeIds = new HashSet<>();
        this.disconnectedConsumerAreas = new ArrayList<>();
        this.disconnectedCriticalAreas = new ArrayList<>();
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public int getActiveSourcesCount() {
        return activeSourcesCount;
    }

    public void setActiveSourcesCount(int activeSourcesCount) {
        this.activeSourcesCount = activeSourcesCount;
    }

    public List<String> getActiveSourceNodeIds() {
        return activeSourceNodeIds;
    }

    public void setActiveSourceNodeIds(List<String> activeSourceNodeIds) {
        this.activeSourceNodeIds = activeSourceNodeIds;
    }

    public Set<String> getEnergizedNodeIds() {
        return energizedNodeIds;
    }

    public void setEnergizedNodeIds(Set<String> energizedNodeIds) {
        this.energizedNodeIds = energizedNodeIds;
    }

    public Set<String> getDeenergizedNodeIds() {
        return deenergizedNodeIds;
    }

    public void setDeenergizedNodeIds(Set<String> deenergizedNodeIds) {
        this.deenergizedNodeIds = deenergizedNodeIds;
    }

    public List<ConsumerArea> getDisconnectedConsumerAreas() {
        return disconnectedConsumerAreas;
    }

    public void setDisconnectedConsumerAreas(List<ConsumerArea> disconnectedConsumerAreas) {
        this.disconnectedConsumerAreas = disconnectedConsumerAreas;
    }

    public List<CriticalArea> getDisconnectedCriticalAreas() {
        return disconnectedCriticalAreas;
    }

    public void setDisconnectedCriticalAreas(List<CriticalArea> disconnectedCriticalAreas) {
        this.disconnectedCriticalAreas = disconnectedCriticalAreas;
    }

    public int getTotalConsumersAffected() {
        return totalConsumersAffected;
    }

    public void setTotalConsumersAffected(int totalConsumersAffected) {
        this.totalConsumersAffected = totalConsumersAffected;
    }

    public int getTotalCriticalFacilitiesAffected() {
        return totalCriticalFacilitiesAffected;
    }

    public void setTotalCriticalFacilitiesAffected(int totalCriticalFacilitiesAffected) {
        this.totalCriticalFacilitiesAffected = totalCriticalFacilitiesAffected;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isGridBlackout() {
        return gridBlackout;
    }

    public void setGridBlackout(boolean gridBlackout) {
        this.gridBlackout = gridBlackout;
    }

    public String getSimulationDisclaimer() {
        return simulationDisclaimer;
    }
}
