package com.smartelectricity.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class representing any node in the electrical distribution grid.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class NetworkNode {
    private String nodeId;
    private String name;
    private String status; // "ACTIVE" or "INACTIVE"
    private double locationX;
    private double locationY;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NetworkNode() {
        this.status = "ACTIVE";
    }

    public NetworkNode(String nodeId, String name, String status, double locationX, double locationY) {
        this.nodeId = nodeId;
        this.name = name;
        this.status = (status != null && !status.isBlank()) ? status : "ACTIVE";
        this.locationX = locationX;
        this.locationY = locationY;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Abstract methods demonstrating Polymorphism
    public abstract String getNodeType();
    public abstract boolean isSourceSubstation();
    public abstract int getConsumerCount();

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getLocationX() {
        return locationX;
    }

    public void setLocationX(double locationX) {
        this.locationX = locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public void setLocationY(double locationY) {
        this.locationY = locationY;
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
        if (!(o instanceof NetworkNode that)) return false;
        return Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        return "NetworkNode{" +
                "nodeId='" + nodeId + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", type='" + getNodeType() + '\'' +
                '}';
    }
}
