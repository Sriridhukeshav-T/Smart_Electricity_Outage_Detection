package com.smartelectricity.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateNodeRequest {
    @NotBlank(message = "Node name is required")
    private String name;

    private String status; // "ACTIVE" or "INACTIVE"
    private Boolean isSource;
    private Double capacityKva;
    private String consumerType; // "CRITICAL" or "GENERAL"
    private String criticalFacilityName;
    private Integer consumerCount;
    private Double locationX;
    private Double locationY;

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
