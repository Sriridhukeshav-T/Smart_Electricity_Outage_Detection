package com.smartelectricity.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a medium-voltage distribution feeder line between two network nodes.
 * Encapsulates electrical transmission attributes and operating state.
 */
public class FeederLine {
    private String feederId;
    private String name;
    private String sourceNodeId;
    private String targetNodeId;
    private String status; // "WORKING" or "FAILED"
    private double capacityAmps;
    private double lengthKm;
    private boolean isBackup;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FeederLine() {
        this.status = "WORKING";
        this.capacityAmps = 500.0;
        this.lengthKm = 1.0;
        this.isBackup = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public FeederLine(String feederId, String name, String sourceNodeId, String targetNodeId,
                      String status, double capacityAmps, double lengthKm, boolean isBackup) {
        this.feederId = feederId;
        this.name = name;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.status = (status != null && !status.isBlank()) ? status : "WORKING";
        this.capacityAmps = capacityAmps;
        this.lengthKm = lengthKm;
        this.isBackup = isBackup;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isWorking() {
        return "WORKING".equalsIgnoreCase(this.status);
    }

    public boolean connectsNode(String nodeId) {
        if (nodeId == null) return false;
        return nodeId.equals(sourceNodeId) || nodeId.equals(targetNodeId);
    }

    public String getOtherNode(String nodeId) {
        if (nodeId == null) return null;
        if (nodeId.equals(sourceNodeId)) return targetNodeId;
        if (nodeId.equals(targetNodeId)) return sourceNodeId;
        return null;
    }

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

    public double getCapacityAmps() {
        return capacityAmps;
    }

    public void setCapacityAmps(double capacityAmps) {
        this.capacityAmps = capacityAmps;
    }

    public double getLengthKm() {
        return lengthKm;
    }

    public void setLengthKm(double lengthKm) {
        this.lengthKm = lengthKm;
    }

    public boolean isBackup() {
        return isBackup;
    }

    public void setBackup(boolean backup) {
        isBackup = backup;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeederLine that)) return false;
        return Objects.equals(feederId, that.feederId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feederId);
    }

    @Override
    public String toString() {
        return "FeederLine{" +
                "feederId='" + feederId + '\'' +
                ", name='" + name + '\'' +
                ", " + sourceNodeId + " <-> " + targetNodeId +
                ", status='" + status + '\'' +
                ", isBackup=" + isBackup +
                '}';
    }
}
