package com.smartelectricity.model;

import java.time.LocalDateTime;

/**
 * Represents an electrical maintenance and field repair team.
 */
public class RepairTeam {
    private String teamId;
    private String teamName;
    private String contactNumber;
    private String status; // "AVAILABLE", "BUSY", "OFF_DUTY"
    private String currentAssignmentId;
    private LocalDateTime createdAt;

    public RepairTeam() {
        this.status = "AVAILABLE";
        this.createdAt = LocalDateTime.now();
    }

    public RepairTeam(String teamId, String teamName, String contactNumber, String status, String currentAssignmentId) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.contactNumber = contactNumber;
        this.status = (status != null && !status.isBlank()) ? status : "AVAILABLE";
        this.currentAssignmentId = currentAssignmentId;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(this.status);
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentAssignmentId() {
        return currentAssignmentId;
    }

    public void setCurrentAssignmentId(String currentAssignmentId) {
        this.currentAssignmentId = currentAssignmentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
