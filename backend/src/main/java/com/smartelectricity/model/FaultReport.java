package com.smartelectricity.model;

import java.time.LocalDateTime;

/**
 * Represents an outage fault report filed on a specific feeder line.
 */
public class FaultReport {
    private String faultId;
    private String feederId;
    private LocalDateTime reportedAt;
    private String faultCause;
    private String severity; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    private String status; // "REPORTED", "ASSIGNED", "IN_PROGRESS", "RESOLVED"
    private String assignedTeamId;
    private LocalDateTime resolvedAt;
    private String notes;

    public FaultReport() {
        this.reportedAt = LocalDateTime.now();
        this.status = "REPORTED";
        this.severity = "HIGH";
    }

    public FaultReport(String faultId, String feederId, String faultCause, String severity,
                       String status, String assignedTeamId, String notes) {
        this.faultId = faultId;
        this.feederId = feederId;
        this.reportedAt = LocalDateTime.now();
        this.faultCause = faultCause;
        this.severity = (severity != null && !severity.isBlank()) ? severity : "HIGH";
        this.status = (status != null && !status.isBlank()) ? status : "REPORTED";
        this.assignedTeamId = assignedTeamId;
        this.notes = notes;
    }

    public boolean isActiveFault() {
        return !"RESOLVED".equalsIgnoreCase(this.status);
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

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getFaultCause() {
        return faultCause;
    }

    public void setFaultCause(String faultCause) {
        this.faultCause = faultCause;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
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

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
