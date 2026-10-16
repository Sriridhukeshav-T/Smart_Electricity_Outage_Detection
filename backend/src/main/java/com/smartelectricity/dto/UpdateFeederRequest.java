package com.smartelectricity.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateFeederRequest {
    @NotBlank(message = "Feeder name is required")
    private String name;

    private String status; // "WORKING" or "FAILED"
    private Double capacityAmps;
    private Double lengthKm;
    private Boolean isBackup;

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
