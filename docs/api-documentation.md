# Smart Electricity Outage Detection and Restoration System
## REST API Documentation

Base URL: `http://localhost:8080/api`

All API responses follow a uniform JSON structure:
```json
{
  "success": true,
  "message": "Human-readable description of operation result",
  "data": { ... }
}
```

---

### Table of Contents
1. [System & Dashboard](#1-system--dashboard)
2. [Network Nodes & Substations](#2-network-nodes--substations)
3. [Feeder Lines](#3-feeder-lines)
4. [Fault Management](#4-fault-management)
5. [Outage Detection (BFS)](#5-outage-detection-bfs)
6. [Restoration & Priority Queue](#6-restoration--priority-queue)
7. [Repair Teams](#7-repair-teams)
8. [Approved Backup Connections](#8-approved-backup-connections)
9. [Event & Audit History](#9-event--audit-history)

---

### 1. System & Dashboard

#### 1.1 Health Check
- **Endpoint**: `GET /api/health`
- **Description**: Verifies that the Spring Boot server and underlying database connection are healthy.
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Smart Electricity Outage Detection System is online.",
  "data": {
    "status": "UP",
    "system": "Distribution Network Management",
    "timestamp": "2026-10-11T16:11:37"
  }
}
```

#### 1.2 Dashboard Statistics
- **Endpoint**: `GET /api/dashboard/stats`
- **Description**: Aggregates grid operational metrics, health indices, active fault tallies, and workforce allocations.
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Dashboard statistics retrieved successfully.",
  "data": {
    "totalNodes": 12,
    "totalSubstations": 3,
    "activeSources": 3,
    "totalConsumerAreas": 9,
    "criticalConsumerAreas": 4,
    "totalConsumers": 34550,
    "totalFeeders": 14,
    "workingFeeders": 14,
    "failedFeeders": 0,
    "activeFaults": 0,
    "queuedRestorationJobs": 0,
    "availableTeams": 3,
    "busyTeams": 0,
    "affectedConsumers": 0,
    "affectedCriticalFacilities": 0,
    "gridHealthPercentage": 100.0,
    "gridStatus": "NORMAL"
  }
}
```

---

### 2. Network Nodes & Substations

#### 2.1 List All Nodes
- **Endpoint**: `GET /api/nodes`
- **Query Parameters**:
  - `search` (optional): Filter by Node ID or Name
  - `type` (optional): `SUBSTATION` or `CONSUMER_AREA`
  - `status` (optional): `ACTIVE` or `INACTIVE`
- **Response**: `200 OK`

#### 2.2 Get Node by ID
- **Endpoint**: `GET /api/nodes/{id}`
- **Response**: `200 OK` or `404 Not Found`

#### 2.3 Create Network Node
- **Endpoint**: `POST /api/nodes`
- **Request Body (Substation)**:
```json
{
  "nodeId": "SUB-04",
  "name": "Western Solar Feeder Station",
  "nodeType": "SUBSTATION",
  "status": "ACTIVE",
  "isSource": true,
  "capacityKva": 30000.0,
  "locationX": 120.0,
  "locationY": 350.0
}
```
- **Request Body (Critical Consumer Area)**:
```json
{
  "nodeId": "CONS-10",
  "name": "District Children's Hospital",
  "nodeType": "CONSUMER_AREA",
  "status": "ACTIVE",
  "consumerType": "CRITICAL",
  "criticalFacilityName": "Pediatric Emergency Ward",
  "consumerCount": 1800,
  "locationX": 500.0,
  "locationY": 520.0
}
```
- **Response**: `201 Created`

#### 2.4 Update Node
- **Endpoint**: `PUT /api/nodes/{id}`
- **Response**: `200 OK`

#### 2.5 Toggle Node Status
- **Endpoint**: `POST /api/nodes/{id}/toggle-status`
- **Description**: Switches substation or load node between `ACTIVE` and `INACTIVE`. Deactivating a source substation triggers network reachability recalculation.
- **Response**: `200 OK`

#### 2.6 Delete Node
- **Endpoint**: `DELETE /api/nodes/{id}`
- **Description**: Removes node if no feeder lines reference it. If feeders exist, returns `409 Conflict`.
- **Response**: `200 OK` or `409 Conflict`

---

### 3. Feeder Lines

#### 3.1 List Feeder Lines
- **Endpoint**: `GET /api/feeders`
- **Query Parameters**:
  - `search` (optional): Search query
  - `status` (optional): `WORKING` or `FAILED`
  - `isBackup` (optional): `true` or `false`
- **Response**: `200 OK`

#### 3.2 Create Feeder Line
- **Endpoint**: `POST /api/feeders`
- **Request Body**:
```json
{
  "feederId": "FEED-15",
  "name": "Auxiliary Solar to Pediatric Clinic Feeder",
  "sourceNodeId": "SUB-04",
  "targetNodeId": "CONS-10",
  "status": "WORKING",
  "capacityAmps": 450.0,
  "lengthKm": 2.1,
  "isBackup": false
}
```
- **Response**: `201 Created`

#### 3.3 Update Feeder Status
- **Endpoint**: `POST /api/feeders/{id}/status`
- **Request Body**: `{"status": "FAILED"}` or `{"status": "WORKING"}`
- **Response**: `200 OK`

#### 3.4 Delete Feeder Line
- **Endpoint**: `DELETE /api/feeders/{id}`
- **Description**: Validates that no active fault reports or backup routes reference this feeder before deletion.
- **Response**: `200 OK` or `409 Conflict`

---

### 4. Fault Management

#### 4.1 List Fault Reports
- **Endpoint**: `GET /api/faults`
- **Response**: `200 OK`

#### 4.2 List Active Faults
- **Endpoint**: `GET /api/faults/active`
- **Response**: `200 OK`

#### 4.3 Report Feeder Line Failure
- **Endpoint**: `POST /api/faults`
- **Request Body**:
```json
{
  "feederId": "FEED-01",
  "faultCause": "Lightning strike on insulator string",
  "severity": "CRITICAL",
  "notes": "Tower 12 insulator shattered; line trip detected"
}
```
- **Workflow Executed**:
  1. Validates feeder existence and checks that no unresolved fault exists on this feeder.
  2. Updates feeder status to `FAILED`.
  3. Inserts `FaultReport` record in database.
  4. Triggers BFS outage detection recalculation.
  5. Schedules/updates restoration job in PriorityQueue.
  6. Inserts `FAULT_REPORTED` event into audit history.
- **Response**: `201 Created`

---

### 5. Outage Detection (BFS)

#### 5.1 Run Outage Detection
- **Endpoint**: `GET /api/outages/detect`
- **Description**: Executes Multi-Source BFS starting from all active source substations. Identifies reachable nodes vs de-energized islands, sums affected consumer counts, and flags isolated critical facilities.
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Outage detection BFS completed successfully.",
  "data": {
    "evaluatedAt": "2026-10-11T16:11:48",
    "activeSourcesCount": 3,
    "activeSourceNodeIds": ["SUB-01", "SUB-02", "SUB-03"],
    "energizedNodeIds": ["SUB-01", "SUB-02", "SUB-03", "CONS-01", "CONS-02", ...],
    "deenergizedNodeIds": [],
    "disconnectedConsumerAreas": [],
    "disconnectedCriticalAreas": [],
    "totalConsumersAffected": 0,
    "totalCriticalFacilitiesAffected": 0,
    "statusMessage": "Normal Grid Operation: All consumer areas energized from active sources.",
    "gridBlackout": false,
    "simulationDisclaimer": "Software simulation only. Does not calculate real electrical power flow or provide real-world switching instructions."
  }
}
```

---

### 6. Restoration & Priority Queue

#### 6.1 Get Prioritized Restoration Queue
- **Endpoint**: `GET /api/restoration/queue`
- **Description**: Retrieves active restoration jobs ranked dynamically by the Java `RestorationPolicy` in a `PriorityQueue`.
- **Ranking Criteria**:
  1. Critical facilities impacted (highest first)
  2. Total affected consumers (highest first)
  3. Earlier fault reported timestamp (FIFO)
- **Response**: `200 OK`

#### 6.2 Assign Repair Team
- **Endpoint**: `POST /api/restoration/assign`
- **Request Body**:
```json
{
  "identifier": "JOB-1001",
  "teamId": "TEAM-A"
}
```
- **Description**: Validates team availability, assigns crew to job and fault report, sets team status to `BUSY`, and logs assignment in event history.
- **Response**: `200 OK`

#### 6.3 Complete Repair & Restore Feeder
- **Endpoint**: `POST /api/restoration/complete/{identifier}`
- **Description**: Marks repair finished, sets feeder status to `WORKING`, closes fault report as `RESOLVED`, marks job `RESTORED`, frees repair crew to `AVAILABLE`, and re-evaluates grid BFS reachability.
- **Response**: `200 OK`

---

### 7. Repair Teams

#### 7.1 List All Teams
- **Endpoint**: `GET /api/teams`
- **Response**: `200 OK`

#### 7.2 List Available Teams
- **Endpoint**: `GET /api/teams/available`
- **Response**: `200 OK`

---

### 8. Approved Backup Connections

#### 8.1 List All Backup Configurations
- **Endpoint**: `GET /api/backups`
- **Response**: `200 OK`

#### 8.2 Evaluate Backup Route Health
- **Endpoint**: `GET /api/backups/routes`
- **Description**: Performs graph simulation on every approved backup configuration. Verifies whether alternate tie-line is in working condition and connected to an active power source.
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Evaluated approved backup routes against current network state.",
  "data": [
    {
      "backupId": "BKP-01",
      "name": "Hospital Primary Alternate Loop",
      "primaryFeederId": "FEED-01",
      "primaryFeederStatus": "WORKING",
      "alternateFeederId": "FEED-13",
      "alternateFeederStatus": "WORKING",
      "alternatePathAvailable": true,
      "canRestoreAffectedLoad": false,
      "pathAnalysis": "STANDBY: Primary line FEED-01 is WORKING normally. Backup route FEED-13 is in healthy standby mode."
    }
  ]
}
```

#### 8.3 Register Approved Backup Route
- **Endpoint**: `POST /api/backups`
- **Request Body**:
```json
{
  "backupId": "BKP-04",
  "name": "Downtown Auxiliary Loop",
  "primaryFeederId": "FEED-02",
  "alternateFeederId": "FEED-11",
  "description": "Bypasses primary downtown line via Water Works tie line"
}
```
- **Response**: `201 Created`

---

### 9. Event & Audit History

#### 9.1 List Audit Events
- **Endpoint**: `GET /api/history?limit=50`
- **Description**: Returns chronological audit entries of network actions, disruptions, crew assignments, and line energizations.
- **Response**: `200 OK`
