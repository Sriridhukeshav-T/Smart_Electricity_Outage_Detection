package com.smartelectricity.model;

import java.time.LocalDateTime;

/**
 * Audit and event log entry.
 */
public class EventHistory {
    private int historyId;
    private String eventType;
    private String targetId;
    private String details;
    private String performedBy;
    private LocalDateTime timestamp;

    public EventHistory() {
        this.performedBy = "System Operator";
        this.timestamp = LocalDateTime.now();
    }

    public EventHistory(String eventType, String targetId, String details, String performedBy) {
        this.eventType = eventType;
        this.targetId = targetId;
        this.details = details;
        this.performedBy = (performedBy != null && !performedBy.isBlank()) ? performedBy : "System Operator";
        this.timestamp = LocalDateTime.now();
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
