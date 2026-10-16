package com.smartelectricity;

import com.smartelectricity.model.RestorationJob;
import com.smartelectricity.model.RestorationPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

class RestorationPriorityQueueTest {

    @Test
    @DisplayName("PriorityQueue ranks higher critical facility count before higher consumer count")
    void testCriticalFacilityTakesPrecedence() {
        PriorityQueue<RestorationJob> pq = new PriorityQueue<>(new RestorationPolicy());

        LocalDateTime now = LocalDateTime.now();

        // Job 1: 0 critical facilities, but massive 15,000 general consumers
        RestorationJob generalJob = new RestorationJob("JOB-1", "FLT-1", "FEED-01", 0, 15000, now.minusMinutes(30));

        // Job 2: 2 critical facilities (Hospital + Water Works), 1,200 consumers
        RestorationJob criticalJob = new RestorationJob("JOB-2", "FLT-2", "FEED-02", 2, 1200, now.minusMinutes(10));

        pq.offer(generalJob);
        pq.offer(criticalJob);

        // First polled item must be the critical facility job!
        RestorationJob first = pq.poll();
        assertNotNull(first);
        assertEquals("JOB-2", first.getJobId(), "Critical facility job must be ranked first");
        assertEquals(2, first.getCriticalFacilitiesCount());

        RestorationJob second = pq.poll();
        assertNotNull(second);
        assertEquals("JOB-1", second.getJobId(), "General consumers job ranked second");
    }

    @Test
    @DisplayName("PriorityQueue ranks higher consumer count when critical counts are equal")
    void testConsumerCountBreaksTie() {
        PriorityQueue<RestorationJob> pq = new PriorityQueue<>(new RestorationPolicy());

        LocalDateTime now = LocalDateTime.now();

        // Both have 1 critical facility
        RestorationJob smallerJob = new RestorationJob("JOB-A", "FLT-A", "FEED-A", 1, 800, now.minusMinutes(20));
        RestorationJob largerJob = new RestorationJob("JOB-B", "FLT-B", "FEED-B", 1, 4500, now.minusMinutes(10));

        pq.offer(smallerJob);
        pq.offer(largerJob);

        RestorationJob first = pq.poll();
        assertNotNull(first);
        assertEquals("JOB-B", first.getJobId(), "Higher consumer count wins when critical counts are identical");
        assertEquals(4500, first.getAffectedConsumersCount());
    }

    @Test
    @DisplayName("PriorityQueue ranks earlier fault report order when critical and consumers are equal")
    void testEarlierReportOrderBreaksTie() {
        PriorityQueue<RestorationJob> pq = new PriorityQueue<>(new RestorationPolicy());

        LocalDateTime timeEarlier = LocalDateTime.now().minusHours(2);
        LocalDateTime timeLater = LocalDateTime.now().minusMinutes(15);

        // Same critical count (1) and same consumer count (2000)
        RestorationJob earlyJob = new RestorationJob("JOB-EARLY", "FLT-1", "FEED-1", 1, 2000, timeEarlier);
        RestorationJob laterJob = new RestorationJob("JOB-LATER", "FLT-2", "FEED-2", 1, 2000, timeLater);

        pq.offer(laterJob);
        pq.offer(earlyJob);

        RestorationJob first = pq.poll();
        assertNotNull(first);
        assertEquals("JOB-EARLY", first.getJobId(), "Earlier fault reported time takes priority on identical impact");
    }

    @Test
    @DisplayName("PriorityQueue handles complex multi-tier ranking sequence correctly")
    void testComplexMultiTierRanking() {
        PriorityQueue<RestorationJob> pq = new PriorityQueue<>(new RestorationPolicy());

        LocalDateTime now = LocalDateTime.now();

        RestorationJob j1 = new RestorationJob("J1", "F1", "FD1", 0, 500, now);
        RestorationJob j2 = new RestorationJob("J2", "F2", "FD2", 3, 2000, now);
        RestorationJob j3 = new RestorationJob("J3", "F3", "FD3", 1, 8000, now);
        RestorationJob j4 = new RestorationJob("J4", "F4", "FD4", 1, 1200, now);
        RestorationJob j5 = new RestorationJob("J5", "F5", "FD5", 0, 10000, now);

        pq.offer(j1);
        pq.offer(j2);
        pq.offer(j3);
        pq.offer(j4);
        pq.offer(j5);

        // Expected order:
        // 1. J2 (3 critical, 2000 cons)
        // 2. J3 (1 critical, 8000 cons)
        // 3. J4 (1 critical, 1200 cons)
        // 4. J5 (0 critical, 10000 cons)
        // 5. J1 (0 critical, 500 cons)
        assertEquals("J2", pq.poll().getJobId());
        assertEquals("J3", pq.poll().getJobId());
        assertEquals("J4", pq.poll().getJobId());
        assertEquals("J5", pq.poll().getJobId());
        assertEquals("J1", pq.poll().getJobId());
        assertTrue(pq.isEmpty());
    }
}
