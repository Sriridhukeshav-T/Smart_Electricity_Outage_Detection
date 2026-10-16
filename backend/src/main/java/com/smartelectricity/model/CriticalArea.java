package com.smartelectricity.model;

/**
 * Represents a Consumer Area hosting high-priority critical infrastructure
 * (e.g., Hospitals, Water Treatment Plants, Emergency Centers, Data Hubs).
 * Demonstrates Inheritance and Polymorphic Behavior.
 */
public class CriticalArea extends ConsumerArea {
    private String criticalFacilityName;

    public CriticalArea() {
        super();
        this.criticalFacilityName = "Critical Facility";
    }

    public CriticalArea(String nodeId, String name, String status, double locationX, double locationY,
                        int consumerCount, String criticalFacilityName) {
        super(nodeId, name, status, locationX, locationY, consumerCount);
        this.criticalFacilityName = criticalFacilityName != null ? criticalFacilityName : "Essential Lifeline Unit";
    }

    @Override
    public boolean isCritical() {
        return true;
    }

    @Override
    public String getConsumerType() {
        return "CRITICAL";
    }

    @Override
    public String getCriticalFacilityName() {
        return criticalFacilityName;
    }

    public void setCriticalFacilityName(String criticalFacilityName) {
        this.criticalFacilityName = criticalFacilityName;
    }

    @Override
    public String toString() {
        return "CriticalArea{" +
                "nodeId='" + getNodeId() + '\'' +
                ", name='" + getName() + '\'' +
                ", facility='" + criticalFacilityName + '\'' +
                ", consumers=" + getConsumerCount() +
                '}';
    }
}
