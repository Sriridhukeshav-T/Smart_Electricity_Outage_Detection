package com.smartelectricity.dto;

public class DashboardStatsResponse {
    private int totalNodes;
    private int totalSubstations;
    private int activeSources;
    private int totalConsumerAreas;
    private int criticalConsumerAreas;
    private int totalConsumers;
    private int totalFeeders;
    private int workingFeeders;
    private int failedFeeders;
    private int activeFaults;
    private int queuedRestorationJobs;
    private int availableTeams;
    private int busyTeams;
    private int affectedConsumers;
    private int affectedCriticalFacilities;
    private double gridHealthPercentage;
    private String gridStatus; // "NORMAL", "PARTIAL_OUTAGE", "CRITICAL_OUTAGE", "TOTAL_BLACKOUT"

    public int getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }

    public int getTotalSubstations() {
        return totalSubstations;
    }

    public void setTotalSubstations(int totalSubstations) {
        this.totalSubstations = totalSubstations;
    }

    public int getActiveSources() {
        return activeSources;
    }

    public void setActiveSources(int activeSources) {
        this.activeSources = activeSources;
    }

    public int getTotalConsumerAreas() {
        return totalConsumerAreas;
    }

    public void setTotalConsumerAreas(int totalConsumerAreas) {
        this.totalConsumerAreas = totalConsumerAreas;
    }

    public int getCriticalConsumerAreas() {
        return criticalConsumerAreas;
    }

    public void setCriticalConsumerAreas(int criticalConsumerAreas) {
        this.criticalConsumerAreas = criticalConsumerAreas;
    }

    public int getTotalConsumers() {
        return totalConsumers;
    }

    public void setTotalConsumers(int totalConsumers) {
        this.totalConsumers = totalConsumers;
    }

    public int getTotalFeeders() {
        return totalFeeders;
    }

    public void setTotalFeeders(int totalFeeders) {
        this.totalFeeders = totalFeeders;
    }

    public int getWorkingFeeders() {
        return workingFeeders;
    }

    public void setWorkingFeeders(int workingFeeders) {
        this.workingFeeders = workingFeeders;
    }

    public int getFailedFeeders() {
        return failedFeeders;
    }

    public void setFailedFeeders(int failedFeeders) {
        this.failedFeeders = failedFeeders;
    }

    public int getActiveFaults() {
        return activeFaults;
    }

    public void setActiveFaults(int activeFaults) {
        this.activeFaults = activeFaults;
    }

    public int getQueuedRestorationJobs() {
        return queuedRestorationJobs;
    }

    public void setQueuedRestorationJobs(int queuedRestorationJobs) {
        this.queuedRestorationJobs = queuedRestorationJobs;
    }

    public int getAvailableTeams() {
        return availableTeams;
    }

    public void setAvailableTeams(int availableTeams) {
        this.availableTeams = availableTeams;
    }

    public int getBusyTeams() {
        return busyTeams;
    }

    public void setBusyTeams(int busyTeams) {
        this.busyTeams = busyTeams;
    }

    public int getAffectedConsumers() {
        return affectedConsumers;
    }

    public void setAffectedConsumers(int affectedConsumers) {
        this.affectedConsumers = affectedConsumers;
    }

    public int getAffectedCriticalFacilities() {
        return affectedCriticalFacilities;
    }

    public void setAffectedCriticalFacilities(int affectedCriticalFacilities) {
        this.affectedCriticalFacilities = affectedCriticalFacilities;
    }

    public double getGridHealthPercentage() {
        return gridHealthPercentage;
    }

    public void setGridHealthPercentage(double gridHealthPercentage) {
        this.gridHealthPercentage = gridHealthPercentage;
    }

    public String getGridStatus() {
        return gridStatus;
    }

    public void setGridStatus(String gridStatus) {
        this.gridStatus = gridStatus;
    }
}
