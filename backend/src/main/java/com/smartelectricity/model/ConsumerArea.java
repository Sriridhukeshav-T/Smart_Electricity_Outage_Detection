package com.smartelectricity.model;

/**
 * Abstract class representing a consumer load center in the distribution network.
 * Demonstrates Hierarchical Inheritance.
 */
public abstract class ConsumerArea extends NetworkNode {
    private int consumerCount;

    public ConsumerArea() {
        super();
        this.consumerCount = 0;
    }

    public ConsumerArea(String nodeId, String name, String status, double locationX, double locationY, int consumerCount) {
        super(nodeId, name, status, locationX, locationY);
        this.consumerCount = Math.max(0, consumerCount);
    }

    @Override
    public String getNodeType() {
        return "CONSUMER_AREA";
    }

    @Override
    public boolean isSourceSubstation() {
        return false;
    }

    @Override
    public int getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(int consumerCount) {
        this.consumerCount = Math.max(0, consumerCount);
    }

    /**
     * Polymorphic method to determine if this consumer area hosts critical infrastructure.
     */
    public abstract boolean isCritical();

    public abstract String getConsumerType();
    public abstract String getCriticalFacilityName();
}
