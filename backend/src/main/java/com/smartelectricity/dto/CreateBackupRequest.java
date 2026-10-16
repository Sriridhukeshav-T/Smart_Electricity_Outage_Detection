package com.smartelectricity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateBackupRequest {
    @NotBlank(message = "Backup ID is required")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Backup ID must contain only alphanumeric characters, underscores, and dashes")
    private String backupId;

    @NotBlank(message = "Backup configuration name is required")
    private String name;

    @NotBlank(message = "Primary feeder ID is required")
    private String primaryFeederId;

    @NotBlank(message = "Alternate feeder ID is required")
    private String alternateFeederId;

    private String description;
    private Boolean isApproved = true;

    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryFeederId() {
        return primaryFeederId;
    }

    public void setPrimaryFeederId(String primaryFeederId) {
        this.primaryFeederId = primaryFeederId;
    }

    public String getAlternateFeederId() {
        return alternateFeederId;
    }

    public void setAlternateFeederId(String alternateFeederId) {
        this.alternateFeederId = alternateFeederId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean approved) {
        isApproved = approved;
    }
}
