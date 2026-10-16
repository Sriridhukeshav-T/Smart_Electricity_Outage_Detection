package com.smartelectricity.model;

import java.time.LocalDateTime;

/**
 * Represents a pre-approved, operator-configured alternate backup feeder path.
 * In a real utility, backups undergo strict protection coordination reviews.
 */
public class ApprovedBackupConnection {
    private String backupId;
    private String name;
    private String primaryFeederId;
    private String alternateFeederId;
    private String description;
    private boolean isApproved;
    private LocalDateTime createdAt;

    public ApprovedBackupConnection() {
        this.isApproved = true;
        this.createdAt = LocalDateTime.now();
    }

    public ApprovedBackupConnection(String backupId, String name, String primaryFeederId,
                                    String alternateFeederId, String description, boolean isApproved) {
        this.backupId = backupId;
        this.name = name;
        this.primaryFeederId = primaryFeederId;
        this.alternateFeederId = alternateFeederId;
        this.description = description;
        this.isApproved = isApproved;
        this.createdAt = LocalDateTime.now();
    }

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

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
