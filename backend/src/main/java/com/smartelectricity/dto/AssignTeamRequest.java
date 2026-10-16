package com.smartelectricity.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignTeamRequest {
    @NotBlank(message = "Job or Fault ID is required")
    private String identifier; // can be jobId or faultId

    @NotBlank(message = "Repair team ID is required")
    private String teamId;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
