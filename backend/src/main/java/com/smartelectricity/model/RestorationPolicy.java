package com.smartelectricity.model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Strategy/Policy class defining the ordering in the Restoration PriorityQueue.
 * Ranking rules:
 * 1. Critical facilities affected, highest first (descending).
 * 2. Total affected consumers, highest first (descending).
 * 3. Earlier fault-report order first (ascending by reported time).
 */
public class RestorationPolicy implements Comparator<RestorationJob>, Serializable {

    @Override
    public int compare(RestorationJob j1, RestorationJob j2) {
        if (j1 == null && j2 == null) return 0;
        if (j1 == null) return 1;
        if (j2 == null) return -1;

        // Rule 1: Critical facilities affected, highest first
        int criticalCompare = Integer.compare(j2.getCriticalFacilitiesCount(), j1.getCriticalFacilitiesCount());
        if (criticalCompare != 0) {
            return criticalCompare;
        }

        // Rule 2: Total affected consumers, highest first
        int consumerCompare = Integer.compare(j2.getAffectedConsumersCount(), j1.getAffectedConsumersCount());
        if (consumerCompare != 0) {
            return consumerCompare;
        }

        // Rule 3: Earlier fault-report order first (FIFO for ties)
        if (j1.getFaultReportedAt() != null && j2.getFaultReportedAt() != null) {
            int timeCompare = j1.getFaultReportedAt().compareTo(j2.getFaultReportedAt());
            if (timeCompare != 0) {
                return timeCompare;
            }
        }

        // Deterministic fallback by Job ID
        if (j1.getJobId() != null && j2.getJobId() != null) {
            return j1.getJobId().compareTo(j2.getJobId());
        }

        return 0;
    }
}
