package com.smartelectricity.model;

/**
 * Represents standard commercial, residential, or industrial consumer loads.
 * Demonstrates Inheritance from ConsumerArea.
 */
public class GeneralArea extends ConsumerArea {

    public GeneralArea() {
        super();
    }

    public GeneralArea(String nodeId, String name, String status, double locationX, double locationY, int consumerCount) {
        super(nodeId, name, status, locationX, locationY, consumerCount);
    }

    @Override
    public boolean isCritical() {
        return false;
    }

    @Override
    public String getConsumerType() {
        return "GENERAL";
    }

    @Override
    public String getCriticalFacilityName() {
        return null;
    }

    @Override
    public String toString() {
        return "GeneralArea{" +
                "nodeId='" + getNodeId() + '\'' +
                ", name='" + getName() + '\'' +
                ", consumers=" + getConsumerCount() +
                '}';
    }
}
