package com.smartelectricity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateFeederRequest {
    @NotBlank(message = "Feeder ID is required")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Feeder ID must contain only alphanumeric characters, underscores, and dashes")
    private String feederId;

    @NotBlank(message = "Feeder name is required")
    private String name;

    @NotBlank(message = "Source node ID is required")
    private String sourceNodeId;

    @NotBlank(message = "Target node ID is required")
    private String targetNodeId;

    private String status = "WORKING"; // "WORKING" or "FAILED"
    private Double capacityAmps = 500.0;
    private Double lengthKm = 1.0;
    private Boolean isBackup = false;

    public String getFeederId() {
        return feederId;
    }

    public void setFeederId(String feederId) {
        this.feederId = feederId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(String targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getCapacityAmps() {
        return capacityAmps;
    }

    public void setCapacityAmps(Double capacityAmps) {
        this.capacityAmps = capacityAmps;
    }

    public Double getLengthKm() {
        return lengthKm;
    }

    public void setLengthKm(Double lengthKm) {
        this.lengthKm = lengthKm;
    }

    public Boolean getIsBackup() {
        return isBackup;
    }

    public void setIsBackup(Boolean backup) {
        isBackup = backup;
    }
}
