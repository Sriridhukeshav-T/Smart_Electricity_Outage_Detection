package com.smartelectricity;

import com.smartelectricity.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistributionNetworkGraphTest {

    @Test
    @DisplayName("Polymorphism: Substation, CriticalArea, and GeneralArea demonstrate OOP behaviors")
    void testOOPPolymorphism() {
        NetworkNode sub = new Substation("SUB-01", "Main Sub", "ACTIVE", 0, 0, true, 50000);
        NetworkNode crit = new CriticalArea("CONS-01", "Hospital", "ACTIVE", 10, 10, 2000, "City Hospital");
        NetworkNode gen = new GeneralArea("CONS-02", "Residential", "ACTIVE", 20, 20, 4000);

        // Polymorphic method calls on abstract NetworkNode reference
        assertEquals("SUBSTATION", sub.getNodeType());
        assertTrue(sub.isSourceSubstation());
        assertEquals(0, sub.getConsumerCount());

        assertEquals("CONSUMER_AREA", crit.getNodeType());
        assertFalse(crit.isSourceSubstation());
        assertEquals(2000, crit.getConsumerCount());
        assertTrue(((ConsumerArea) crit).isCritical());

        assertEquals("CONSUMER_AREA", gen.getNodeType());
        assertFalse(gen.isSourceSubstation());
        assertEquals(4000, gen.getConsumerCount());
        assertFalse(((ConsumerArea) gen).isCritical());
    }

    @Test
    @DisplayName("Graph Data Structure: Adjacency list and incident feeders maintain bidirectional connectivity")
    void testAdjacencyListGraph() {
        DistributionNetwork network = new DistributionNetwork();

        Substation sub = new Substation("SUB-01", "Grid Main", "ACTIVE", 0, 0, true, 50000);
        CriticalArea hosp = new CriticalArea("CONS-01", "General Hospital", "ACTIVE", 50, 50, 1500, "Hospital");
        network.addNode(sub);
        network.addNode(hosp);

        FeederLine line = new FeederLine("F-10", "Main Feed", "SUB-01", "CONS-01", "WORKING", 500, 2.0, false);
        network.addFeeder(line);

        // Check incident feeders from both ends
        List<FeederLine> subFeeders = network.getIncidentFeeders("SUB-01");
        List<FeederLine> hospFeeders = network.getIncidentFeeders("CONS-01");

        assertEquals(1, subFeeders.size());
        assertEquals("F-10", subFeeders.get(0).getFeederId());

        assertEquals(1, hospFeeders.size());
        assertEquals("F-10", hospFeeders.get(0).getFeederId());

        // Check otherNode helper
        assertEquals("CONS-01", line.getOtherNode("SUB-01"));
        assertEquals("SUB-01", line.getOtherNode("CONS-01"));
        assertNull(line.getOtherNode("UNKNOWN"));
    }

    @Test
    @DisplayName("Graph Node Removal: Deleting a node cleanly cascades to incident feeder removal in graph")
    void testNodeRemoval() {
        DistributionNetwork network = new DistributionNetwork();
        network.addNode(new Substation("SUB-A", "Sub A", "ACTIVE", 0, 0, true, 10000));
        network.addNode(new GeneralArea("CONS-B", "Area B", "ACTIVE", 10, 10, 500));
        network.addFeeder(new FeederLine("F-AB", "Line AB", "SUB-A", "CONS-B", "WORKING", 300, 1.0, false));

        assertTrue(network.hasNode("CONS-B"));
        assertTrue(network.hasFeeder("F-AB"));

        network.removeNode("CONS-B");

        assertFalse(network.hasNode("CONS-B"));
        assertFalse(network.hasFeeder("F-AB"), "Incident feeder should be removed when node is deleted");
        assertTrue(network.getIncidentFeeders("SUB-A").isEmpty());
    }
}
