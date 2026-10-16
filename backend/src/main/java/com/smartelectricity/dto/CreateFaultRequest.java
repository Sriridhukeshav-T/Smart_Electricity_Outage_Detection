package com.smartelectricity.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateFaultRequest {
    @NotBlank(message = "Feeder ID is required")
    private String feederId;

    @NotBlank(message = "Fault cause description is required")
    private String faultCause;

    private String severity = "HIGH"; // LOW, MEDIUM, HIGH, CRITICAL
    private String notes;

    public String getFeederId() {
        return feederId;
    }

    public void setFeederId(String feederId) {
        this.feederId = feederId;
    }

    public String getFaultCause() {
        return faultCause;
    }

    public void setFaultCause(String faultCause) {
        this.faultCause = faultCause;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
