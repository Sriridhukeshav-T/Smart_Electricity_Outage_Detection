package com.smartelectricity.model;

/**
 * Represents an electrical substation (feed source or step-down distribution station).
 * Demonstrates Inheritance from NetworkNode.
 */
public class Substation extends NetworkNode {
    private boolean isSource;
    private double capacityKva;

    public Substation() {
        super();
        this.isSource = true;
        this.capacityKva = 50000.0;
    }

    public Substation(String nodeId, String name, String status, double locationX, double locationY,
                      boolean isSource, double capacityKva) {
        super(nodeId, name, status, locationX, locationY);
        this.isSource = isSource;
        this.capacityKva = capacityKva;
    }

    @Override
    public String getNodeType() {
        return "SUBSTATION";
    }

    @Override
    public boolean isSourceSubstation() {
        return isSource && isActive();
    }

    @Override
    public int getConsumerCount() {
        return 0; // Substations generate/distribute power, they do not consume load
    }

    public boolean isSource() {
        return isSource;
    }

    public void setSource(boolean source) {
        isSource = source;
    }

    public double getCapacityKva() {
        return capacityKva;
    }

    public void setCapacityKva(double capacityKva) {
        this.capacityKva = capacityKva;
    }
}
