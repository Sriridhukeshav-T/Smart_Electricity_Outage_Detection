# Test Cases & Validation Suite
## Project: Smart Electricity Outage Detection and Restoration System

This document outlines the validation and test cases designed to verify the functionality of the distribution network simulation, BFS outage detection algorithm, Java PriorityQueue restoration policy, database persistence, and API validation.

---

### Test Summary Matrix

| ID | Test Scenario | Category | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **TC-01** | Normal Network with all lines working | Outage Detection | All consumer areas energized; 0 affected consumers; 0 critical facilities cut; Grid Health 100%. | Outage BFS reports 0 disconnected loads; energized count = 12; status = NORMAL. | **PASS** |
| **TC-02** | Single Feeder Failure (`FEED-02`) | Outage Detection | Downstream consumer area (`CONS-04`) de-energized; 8,500 consumers affected; 0 critical facilities cut. | Only `CONS-04` flagged as disconnected; 8,500 consumers affected. | **PASS** |
| **TC-03** | Multiple Simultaneous Failures (`FEED-01` & `FEED-05`) | Graph BFS Traversal | Downstream zones of both lines evaluate reachability; alternative redundant tie-lines checked. | Both damaged branches evaluated; affected loads isolated accordingly. | **PASS** |
| **TC-04** | Critical Facility Disconnected (`CONS-01` Metro Hospital) | Critical Infrastructure | `FEED-01` fails; Hospital identified as de-energized; PriorityQueue assigns highest urgency score. | Flagged in `disconnectedCriticalAreas`; Priority Score boosted by 10,000 weight. | **PASS** |
| **TC-05** | Multiple Critical Facilities Disconnected | Critical Infrastructure | `FEED-01`, `FEED-03`, `FEED-06` fail; multiple lifeline units isolated; ranked by critical facility count. | PriorityQueue orders jobs with multiple critical facilities before single facility jobs. | **PASS** |
| **TC-06** | Multiple Active Sources Redundancy | Multi-Source BFS | `SUB-01`, `SUB-02`, and `SUB-03` all energized; BFS traverses from all active sources concurrently. | Power arrives at nodes via any active substation; mesh connectivity preserved. | **PASS** |
| **TC-07** | One Source Deactivated (`SUB-01` INACTIVE) | Substation Management | BFS initiates only from remaining active sources (`SUB-02`, `SUB-03`); unaffected sectors stay energized. | `SUB-01` excluded as BFS root; downstream radial nodes lose power; others remain energized. | **PASS** |
| **TC-08** | All Sources Deactivated (Grid Blackout) | Grid Blackout | 0 active sources; system flags `gridBlackout=true`; 100% of consumer areas reported de-energized. | `activeSourcesCount=0`; all 9 consumer areas de-energized; status message alerts blackout. | **PASS** |
| **TC-09** | Feeder Repair and Grid Restoration | Restoration Lifecycle | `completeRestoration` sets feeder to WORKING, marks fault RESOLVED, frees team to AVAILABLE, clears outage. | Feeder status becomes WORKING; team returns to AVAILABLE; grid health returns to 100%. | **PASS** |
| **TC-10** | Valid Approved Backup Route (`BKP-01`) | Backup Route Verification | `FEED-01` fails; alternate feeder `FEED-13` is WORKING and connected to active `SUB-02`; route evaluated as READY FOR DISPATCH. | Route marked `canRestoreAffectedLoad=true`; analysis reports standby power ready. | **PASS** |
| **TC-11** | Backup Path Blocked by Failed Line | Backup Route Verification | Alternate feeder `FEED-13` is marked FAILED; backup status flags route as BLOCKED / UNAVAILABLE. | `alternatePathAvailable=false`; message warns alternate line is FAILED. | **PASS** |
| **TC-12** | No Available Backup Path | Backup Route Verification | Feeder with no alternate tie-line fails; system reports radial outage with no backup option. | Unassigned feeder isolated; PriorityQueue schedules urgent field crew dispatch. | **PASS** |
| **TC-13** | Repair Team Assignment | Workforce Dispatch | Available crew assigned; crew status changes to BUSY; job status updates to ASSIGNED. | Crew `TEAM-A` updated to BUSY; assignment ID set to `JOB-xxxx`; audit history logged. | **PASS** |
| **TC-14** | Invalid Node or Feeder ID Validation | Input Integrity | Request with non-existent source/target node or feeder ID rejected with HTTP 400 Bad Request. | API returns `success: false` with descriptive error message (e.g., `Node not found: SUB-99`). | **PASS** |
| **TC-15** | Duplicate Active Fault Prevention | Fault Management | Filing a second fault on an already failed/unresolved feeder is rejected with HTTP 409 Conflict. | System detects active fault on feeder and returns `Duplicate active faults are not allowed`. | **PASS** |
| **TC-16** | Dynamic Node & Feeder Creation | Network CRUD | User creates new node `CONS-10` and feeder `FEED-15`; in-memory graph and database update immediately. | Node and line persist in MySQL; BFS includes them in next scan without restart. | **PASS** |
| **TC-17** | Editing Consumer Counts & Critical Type | Dynamic Attributes | Updating `CONS-05` consumer count from 5,200 to 7,000 updates outage calculations and PriorityQueue rank. | Updated load reflected in DB and PriorityQueue consumer count calculations. | **PASS** |
| **TC-18** | Persistence after Application Restart | Data Durability | When backend process stops and restarts, all nodes, feeders, faults, and history load from MySQL. | `DistributionNetworkService` reloads all entities from MySQL; state fully preserved. | **PASS** |

---

### Detailed Test Specifications

#### Test Case 1: Normal Network Operation
- **Objective**: Verify BFS reachability when all grid elements are operational.
- **Input**: Database seed data (12 nodes, 14 working feeders, 3 active sources).
- **Execution**: `GET /api/outages/detect`
- **Expected Result**:
  - `activeSourcesCount`: 3 (`SUB-01`, `SUB-02`, `SUB-03`)
  - `disconnectedConsumerAreas`: `[]` (empty list)
  - `totalConsumersAffected`: 0
  - `gridHealthPercentage`: 100.0%
- **Actual Result**: `activeSourcesCount: 3`, `disconnectedConsumerAreas: []`, `totalConsumersAffected: 0`, `gridHealth: 100.0%`.
- **Verdict**: PASS

#### Test Case 2: Single Feeder Disruption
- **Objective**: Ensure BFS correctly isolates downstream loads on a radial branch.
- **Input**: Feeder `FEED-02` (`SUB-01` to `CONS-04`) marked as `FAILED`.
- **Execution**: `POST /api/feeders/FEED-02/status` with `{"status": "FAILED"}` followed by `GET /api/outages/detect`.
- **Expected Result**:
  - `CONS-04` (North Commercial Downtown District) is unreachable from `SUB-01`, `SUB-02`, `SUB-03`.
  - Affected consumers = 8,500.
  - Critical facilities affected = 0.
- **Actual Result**: `CONS-04` unreachable; 8,500 consumers affected; 0 critical facilities cut.
- **Verdict**: PASS

#### Test Case 3: PriorityQueue Ranking Order Validation
- **Objective**: Verify that `RestorationPolicy` orders jobs by (1) Critical facilities, (2) Consumers, (3) FIFO.
- **Input**:
  - Job A: 0 critical facilities, 10,000 consumers, reported at 10:00 AM.
  - Job B: 1 critical facility (Hospital), 1,500 consumers, reported at 10:15 AM.
  - Job C: 1 critical facility (Water Plant), 3,000 consumers, reported at 10:20 AM.
- **Execution**: Insert all 3 into `PriorityQueue<RestorationJob>`.
- **Expected Polling Order**:
  1. Job C (1 critical facility, 3,000 consumers)
  2. Job B (1 critical facility, 1,500 consumers)
  3. Job A (0 critical facilities, 10,000 consumers)
- **Actual Result**: `poll()` returns Job C first, then Job B, then Job A.
- **Verdict**: PASS (Automated in `RestorationPriorityQueueTest.java`)

#### Test Case 4: Duplicate Active Fault Rejection
- **Objective**: Prevent operators from creating multiple conflicting active faults on the same feeder.
- **Input**:
  - Step 1: Report fault on `FEED-01` (`FLT-1001`).
  - Step 2: Attempt to report second fault on `FEED-01` before `FLT-1001` is resolved.
- **Expected Result**:
  - HTTP 409 Conflict.
  - Message: `Feeder 'FEED-01' already has an active unresolved fault report... Duplicate active faults are not allowed.`
- **Actual Result**: Exception caught by `GlobalExceptionHandler`, HTTP 409 returned with clear error message.
- **Verdict**: PASS

#### Test Case 5: Grid Blackout Condition
- **Objective**: Confirm behavior when all source substations are deactivated.
- **Input**: Deactivate `SUB-01`, `SUB-02`, and `SUB-03` via `POST /api/nodes/{id}/toggle-status`.
- **Execution**: `GET /api/outages/detect`.
- **Expected Result**:
  - `activeSourcesCount`: 0.
  - `gridBlackout`: true.
  - `totalConsumersAffected`: 34,550 (100% of all consumers).
  - Status message: `CRITICAL: No active power sources currently available. Complete grid de-energization.`
- **Actual Result**: `activeSourcesCount: 0`, `gridBlackout: true`, `totalConsumersAffected: 34550`.
- **Verdict**: PASS (Automated in `OutageDetectionAlgorithmTest.java`)

#### Test Case 6: Safe Deletion of Referenced Records
- **Objective**: Ensure network nodes cannot be deleted if feeder lines are attached to them.
- **Input**: Attempt to delete `SUB-01` via `DELETE /api/nodes/SUB-01` while `FEED-01` and `FEED-02` reference it.
- **Expected Result**:
  - Rejection with HTTP 409 Conflict.
  - Error: `Cannot delete node 'SUB-01' because it is referenced by 2 feeder line(s).`
- **Actual Result**: Rejected with descriptive integrity constraint message.
- **Verdict**: PASS
