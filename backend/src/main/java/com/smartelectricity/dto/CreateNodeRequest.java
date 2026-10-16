package com.smartelectricity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateNodeRequest {
    @NotBlank(message = "Node ID is required")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Node ID must contain only alphanumeric characters, underscores, and dashes")
    private String nodeId;

    @NotBlank(message = "Node name is required")
    private String name;

    @NotBlank(message = "Node type is required")
    @Pattern(regexp = "SUBSTATION|CONSUMER_AREA", message = "Node type must be SUBSTATION or CONSUMER_AREA")
    private String nodeType;

    private String status = "ACTIVE";
    private Boolean isSource = false;
    private Double capacityKva = 10000.0;
    private String consumerType = "GENERAL"; // "CRITICAL" or "GENERAL"
    private String criticalFacilityName;
    private Integer consumerCount = 0;
    private Double locationX = 100.0;
    private Double locationY = 100.0;

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

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsSource() {
        return isSource;
    }

    public void setIsSource(Boolean source) {
        isSource = source;
    }

    public Double getCapacityKva() {
        return capacityKva;
    }

    public void setCapacityKva(Double capacityKva) {
        this.capacityKva = capacityKva;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(String consumerType) {
        this.consumerType = consumerType;
    }

    public String getCriticalFacilityName() {
        return criticalFacilityName;
    }

    public void setCriticalFacilityName(String criticalFacilityName) {
        this.criticalFacilityName = criticalFacilityName;
    }

    public Integer getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(Integer consumerCount) {
        this.consumerCount = consumerCount;
    }

    public Double getLocationX() {
        return locationX;
    }

    public void setLocationX(Double locationX) {
        this.locationX = locationX;
    }

    public Double getLocationY() {
        return locationY;
    }

    public void setLocationY(Double locationY) {
        this.locationY = locationY;
    }
}
