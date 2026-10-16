package com.smartelectricity;

import com.smartelectricity.model.*;
import com.smartelectricity.service.DistributionNetworkService;
import com.smartelectricity.service.OutageDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OutageDetectionAlgorithmTest {

    private DistributionNetwork network;
    private OutageDetectionService outageService;

    @BeforeEach
    void setUp() {
        network = new DistributionNetwork();

        // 2 Substations
        Substation sub1 = new Substation("SUB-01", "Substation North", "ACTIVE", 100, 100, true, 50000);
        Substation sub2 = new Substation("SUB-02", "Substation South", "ACTIVE", 700, 100, true, 40000);

        // 4 Consumer Areas
        CriticalArea hospital = new CriticalArea("CONS-01", "City Hospital", "ACTIVE", 300, 100, 2500, "City Hospital");
        CriticalArea waterWorks = new CriticalArea("CONS-02", "Water Plant", "ACTIVE", 300, 300, 1200, "Central Water Works");
        GeneralArea residential = new GeneralArea("CONS-03", "Downtown Residential", "ACTIVE", 100, 300, 4500);
        GeneralArea industrial = new GeneralArea("CONS-04", "Industrial Zone", "ACTIVE", 700, 300, 6000);

        network.addNode(sub1);
        network.addNode(sub2);
        network.addNode(hospital);
        network.addNode(waterWorks);
        network.addNode(residential);
        network.addNode(industrial);

        // Feeder Lines
        FeederLine f1 = new FeederLine("FEED-01", "Sub1-Hospital", "SUB-01", "CONS-01", "WORKING", 500, 1.5, false);
        FeederLine f2 = new FeederLine("FEED-02", "Sub1-Residential", "SUB-01", "CONS-03", "WORKING", 400, 1.2, false);
        FeederLine f3 = new FeederLine("FEED-03", "Hospital-WaterWorks", "CONS-01", "CONS-02", "WORKING", 300, 1.0, false);
        FeederLine f4 = new FeederLine("FEED-04", "Sub2-Industrial", "SUB-02", "CONS-04", "WORKING", 600, 2.0, false);
        FeederLine f5 = new FeederLine("FEED-05", "WaterWorks-Industrial Tie", "CONS-02", "CONS-04", "WORKING", 350, 2.5, true);

        network.addFeeder(f1);
        network.addFeeder(f2);
        network.addFeeder(f3);
        network.addFeeder(f4);
        network.addFeeder(f5);

        DistributionNetworkService mockNetworkService = new DistributionNetworkService(null, null) {
            @Override
            public DistributionNetwork getNetwork() {
                return network;
            }
        };

        outageService = new OutageDetectionService(mockNetworkService);
    }

    @Test
    @DisplayName("Case 1: Normal network with all lines working -> 0 outages")
    void testNormalNetworkAllWorking() {
        OutageDetectionResult result = outageService.detectOutages();

        assertEquals(0, result.getDisconnectedConsumerAreas().size(), "No consumer areas should be disconnected");
        assertEquals(0, result.getTotalConsumersAffected(), "0 consumers affected");
        assertEquals(0, result.getTotalCriticalFacilitiesAffected(), "0 critical facilities affected");
        assertFalse(result.isGridBlackout());
        assertTrue(result.getEnergizedNodeIds().contains("CONS-01"));
        assertTrue(result.getEnergizedNodeIds().contains("CONS-02"));
        assertTrue(result.getEnergizedNodeIds().contains("CONS-03"));
        assertTrue(result.getEnergizedNodeIds().contains("CONS-04"));
    }

    @Test
    @DisplayName("Case 2: Single feeder failure isolates downstream radial branch")
    void testSingleFeederFailure() {
        // Break FEED-02 (Sub1 -> Residential)
        network.getFeeder("FEED-02").setStatus("FAILED");

        OutageDetectionResult result = outageService.detectOutages();

        assertEquals(1, result.getDisconnectedConsumerAreas().size());
        assertEquals("CONS-03", result.getDisconnectedConsumerAreas().get(0).getNodeId());
        assertEquals(4500, result.getTotalConsumersAffected());
        assertEquals(0, result.getTotalCriticalFacilitiesAffected());
    }

    @Test
    @DisplayName("Case 3: Feeder failure disconnecting Critical Facility (Hospital & Water Works)")
    void testCriticalFacilityDisconnection() {
        // Disconnect Hospital main feeder (FEED-01) and tie line (FEED-05)
        network.getFeeder("FEED-01").setStatus("FAILED");
        network.getFeeder("FEED-05").setStatus("FAILED");

        OutageDetectionResult result = outageService.detectOutages();

        // CONS-01 (Hospital) and CONS-02 (Water Works) become disconnected!
        assertEquals(2, result.getDisconnectedConsumerAreas().size());
        assertEquals(2, result.getTotalCriticalFacilitiesAffected());
        assertEquals(2500 + 1200, result.getTotalConsumersAffected());

        boolean hasHospital = result.getDisconnectedCriticalAreas().stream()
                .anyMatch(c -> c.getNodeId().equals("CONS-01"));
        boolean hasWaterWorks = result.getDisconnectedCriticalAreas().stream()
                .anyMatch(c -> c.getNodeId().equals("CONS-02"));
        assertTrue(hasHospital);
        assertTrue(hasWaterWorks);
    }

    @Test
    @DisplayName("Case 4: Multi-source BFS keeps network energized when one source is cut")
    void testMultiSourceBFSRedundancy() {
        // Fail FEED-01, but FEED-05 connects Industrial to WaterWorks and Hospital!
        // SUB-02 is active, so power should flow SUB-02 -> Industrial -> WaterWorks -> Hospital!
        network.getFeeder("FEED-01").setStatus("FAILED");

        OutageDetectionResult result = outageService.detectOutages();

        // Multi-source BFS traverses from SUB-02 to Hospital through the tie line!
        assertEquals(0, result.getDisconnectedConsumerAreas().size(), "Hospital receives alternate power through SUB-02 tie line");
        assertTrue(result.getEnergizedNodeIds().contains("CONS-01"));
        assertTrue(result.getEnergizedNodeIds().contains("CONS-02"));
    }

    @Test
    @DisplayName("Case 5: All active sources deactivated results in Total Blackout")
    void testAllSourcesDeactivatedBlackout() {
        // Deactivate all substations
        network.getNode("SUB-01").setStatus("INACTIVE");
        network.getNode("SUB-02").setStatus("INACTIVE");

        OutageDetectionResult result = outageService.detectOutages();

        assertTrue(result.isGridBlackout());
        assertEquals(0, result.getActiveSourcesCount());
        assertEquals(4, result.getDisconnectedConsumerAreas().size());
        assertEquals(2500 + 1200 + 4500 + 6000, result.getTotalConsumersAffected());
        assertEquals(2, result.getTotalCriticalFacilitiesAffected());
        assertTrue(result.getStatusMessage().contains("No active power sources"));
    }

    @Test
    @DisplayName("Case 6: One source deactivated while second source keeps its subsystem energized")
    void testOneSourceDeactivated() {
        // Deactivate SUB-01 only, and isolate FEED-05
        network.getNode("SUB-01").setStatus("INACTIVE");
        network.getFeeder("FEED-05").setStatus("FAILED");

        OutageDetectionResult result = outageService.detectOutages();

        // SUB-02 is still active and energizes CONS-04 (Industrial)
        assertTrue(result.getEnergizedNodeIds().contains("SUB-02"));
        assertTrue(result.getEnergizedNodeIds().contains("CONS-04"));

        // CONS-01, CONS-02, CONS-03 lose power from SUB-01
        assertTrue(result.getDeenergizedNodeIds().contains("CONS-01"));
        assertTrue(result.getDeenergizedNodeIds().contains("CONS-02"));
        assertTrue(result.getDeenergizedNodeIds().contains("CONS-03"));
        assertEquals(3, result.getDisconnectedConsumerAreas().size());
    }
}
