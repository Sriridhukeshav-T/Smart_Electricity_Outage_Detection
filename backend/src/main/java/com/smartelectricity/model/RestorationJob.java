package com.smartelectricity.model;

import java.time.LocalDateTime;

/**
 * Represents a prioritized restoration job scheduled for a repair crew.
 */
public class RestorationJob {
    private String jobId;
    private String faultId;
    private String feederId;
    private int priorityScore;
    private int priorityRank;
    private int criticalFacilitiesCount;
    private int affectedConsumersCount;
    private LocalDateTime faultReportedAt;
    private String status; // "QUEUED", "ASSIGNED", "IN_PROGRESS", "RESTORED"
    private String assignedTeamId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RestorationJob() {
        this.status = "QUEUED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RestorationJob(String jobId, String faultId, String feederId, int criticalFacilitiesCount,
                          int affectedConsumersCount, LocalDateTime faultReportedAt) {
        this.jobId = jobId;
        this.faultId = faultId;
        this.feederId = feederId;
        this.criticalFacilitiesCount = criticalFacilitiesCount;
        this.affectedConsumersCount = affectedConsumersCount;
        this.faultReportedAt = faultReportedAt != null ? faultReportedAt : LocalDateTime.now();
        this.status = "QUEUED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateScore();
    }

    public void calculateScore() {
        // High weight on critical lifeline facilities (10,000 per facility) + consumer count
        this.priorityScore = (criticalFacilitiesCount * 10000) + affectedConsumersCount;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getFaultId() {
        return faultId;
    }

    public void setFaultId(String faultId) {
        this.faultId = faultId;
    }

    public String getFeederId() {
        return feederId;
    }

    public void setFeederId(String feederId) {
        this.feederId = feederId;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(int priorityScore) {
        this.priorityScore = priorityScore;
    }

    public int getPriorityRank() {
        return priorityRank;
    }

    public void setPriorityRank(int priorityRank) {
        this.priorityRank = priorityRank;
    }

    public int getCriticalFacilitiesCount() {
        return criticalFacilitiesCount;
    }

    public void setCriticalFacilitiesCount(int criticalFacilitiesCount) {
        this.criticalFacilitiesCount = criticalFacilitiesCount;
        calculateScore();
    }

    public int getAffectedConsumersCount() {
        return affectedConsumersCount;
    }

    public void setAffectedConsumersCount(int affectedConsumersCount) {
        this.affectedConsumersCount = affectedConsumersCount;
        calculateScore();
    }

    public LocalDateTime getFaultReportedAt() {
        return faultReportedAt;
    }

    public void setFaultReportedAt(LocalDateTime faultReportedAt) {
        this.faultReportedAt = faultReportedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTeamId() {
        return assignedTeamId;
    }

    public void setAssignedTeamId(String assignedTeamId) {
        this.assignedTeamId = assignedTeamId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
