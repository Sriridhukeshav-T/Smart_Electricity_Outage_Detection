# Smart Electricity Outage Detection and Restoration System

> **College Mini-Project** • Computer Science & Engineering / Electrical & Electronics  
> **Tech Stack:** Java 26, Spring Boot 3.3.4, Spring JDBC, MySQL (XAMPP), HTML5, CSS3, JavaScript (ES6)

---

## ⚡ Project Overview

The **Smart Electricity Outage Detection and Restoration System** is a software simulation and grid management application designed to:
1. Model an electrical distribution network composed of source substations, distribution feeder lines, and critical/general consumer load areas.
2. Dynamically detect network outages and islanded load centers using **Multi-Source Breadth-First Search (BFS)** graph traversal starting from active substations.
3. Automatically prioritize restoration jobs using a **Java PriorityQueue** that evaluates critical lifeline infrastructure (hospitals, water pumping stations), affected population counts, and chronological fault reporting order.
4. Manage workforce dispatching by assigning field repair teams to prioritized outages.
5. Restore feeder lines upon completion of repair work, recalculating grid reachability and logging audit trails in real time.
6. Verify operator-approved backup routes to restore power to disconnected loads through alternate paths.

> [!NOTE]  
> **Simulation Disclaimer**: This software is an algorithmic graph reachability simulation. It models topological network connectivity and does not compute real-world electrical load flows, reactive power dynamics, or high-voltage switching transients.

---

## 🏗️ Architecture & Project Structure

```text
SmartElectricity/
├── backend/
│   ├── pom.xml                               # Maven project definition (Java 26 compatible)
│   ├── mvnw.cmd                              # Maven wrapper script for Windows
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartelectricity/
│   │   │   │   ├── SmartElectricityApplication.java # Spring Boot entry point
│   │   │   │   ├── config/                  # CORS and Global Exception Handling
│   │   │   │   ├── controller/              # REST API Controllers (REST Endpoints)
│   │   │   │   ├── dto/                     # Data Transfer Objects & API Schemas
│   │   │   │   ├── model/                   # Domain Model & OOP Hierarchy
│   │   │   │   ├── repository/              # Spring JDBC (JdbcTemplate) Repositories
│   │   │   │   └── service/                 # BFS Engine, PriorityQueue, Network Services
│   │   │   └── resources/
│   │   │       └── application.properties   # Database connectivity & server configuration
│   │   └── test/
│   │       └── java/com/smartelectricity/   # Automated JUnit 5 algorithm & graph tests
├── database/
│   └── setup.sql                            # Idempotent MySQL schema and initial sample network
├── docs/
│   ├── api-documentation.md                 # Detailed REST API endpoints and payloads
│   └── test-cases.md                        # 18 test cases with actual and expected results
├── frontend/
│   ├── index.html                           # Single-page dashboard interface
│   ├── style.css                            # Clean, professional engineering theme
│   └── app.js                               # Live API bindings, SVG canvas visualizer, forms
├── mvnw.cmd                                 # Root Maven wrapper
└── README.md                                # Setup guide & Viva Voce preparation
```

---

## 🛠️ Prerequisites

Before running the application on Windows, verify that the following tools are installed:

1. **Java Development Kit (JDK 21 or Java 26)**  
   Verify by opening PowerShell and executing:
   ```powershell
   java -version
   javac -version
   ```
2. **Apache Maven (3.9+)**  
   Included in the project via `mvnw.cmd` and installed in user tools. Verify:
   ```powershell
   .\mvnw.cmd -version
   ```
3. **XAMPP (with MySQL / MariaDB)**  
   Ensure XAMPP is installed at `C:\xampp`.
4. **Web Browser & VS Code (with Live Server extension)**  
   For serving the frontend dashboard.

---

## 🚀 Step-by-Step Windows Setup Instructions

### Step 1: Start XAMPP MySQL Service
1. Open the **XAMPP Control Panel** (or run `C:\xampp\xampp-control.exe`).
2. Click **Start** next to the **MySQL** module.
3. Verify MySQL is running on port `3306`. (Alternatively, if MySQL is installed as a Windows service, ensure it is active).

### Step 2: Initialize the Database (`outage_db`)
Open PowerShell in the project directory (`C:\SmartElectricity`) and execute:
```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root -e "source database/setup.sql"
```
*Note: If your MySQL `root` user has a password, add `-p<your_password>`.*

This script idempotently creates the database `outage_db` and pre-populates:
- 12 Network Nodes (3 Substations + 9 Consumer Areas, including Hospitals and Water Works)
- 14 Feeder Lines connecting the nodes
- 3 Approved Backup Connections
- 3 Available Repair Teams (`Alpha`, `Beta`, `Gamma`)

Verify tables were created:
```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root -e "USE outage_db; SHOW TABLES; SELECT COUNT(*) FROM network_nodes;"
```

### Step 3: Run the Spring Boot Backend
From the root workspace directory, run:
```powershell
.\mvnw.cmd spring-boot:run
```
*(Or navigate to `cd backend` and run `mvn spring-boot:run`)*.

The backend will start on **`http://localhost:8080`**.  
Verify health by visiting: [http://localhost:8080/api/health](http://localhost:8080/api/health)

### Step 4: Launch the Frontend using VS Code Live Server
1. Open the `SmartElectricity` folder in **Visual Studio Code**.
2. Navigate to `frontend/index.html`.
3. Right-click `index.html` and click **"Open with Live Server"** (default port `5500`).
4. The dashboard will open in your browser at `http://127.0.0.1:5500/frontend/index.html`.
5. The sidebar status indicator will display **"Backend Online (:8080)"**.

---

## 🧪 Running Automated Unit Tests

Automated tests are written using JUnit 5 and verify the core graph algorithms, PriorityQueue ranking logic, and BFS multi-source reachability:

To run all automated tests:
```powershell
.\mvnw.cmd test
```

### Test Suite Highlights:
- `OutageDetectionAlgorithmTest`: Tests normal mesh operation, single feeder severance, critical hospital disconnection, multi-source redundancy, and total blackout when all sources are deactivated.
- `RestorationPriorityQueueTest`: Verifies the 3-tier restoration ranking policy (Critical facilities first $\to$ Consumer density $\to$ FIFO).
- `DistributionNetworkGraphTest`: Tests adjacency-list graph mutations and OOP polymorphic method execution.

All automated tests execute and pass with **0 failures and 0 errors**.

---

## 📚 Viva Voce & Technical Defense Preparation

When presenting this project for college evaluation or viva voce examinations, explain the key architectural decisions:

### 1. Data Structures & Algorithm Selection

| Data Structure / Algorithm | Class Used | Role & Purpose in Project | Time Complexity |
|---|---|---|---|
| **Adjacency-List Graph** | `Map<String, List<FeederLine>>` | Represents the distribution grid topology. Nodes map to incident feeder cables connecting neighbors. | Space: $O(V + E)$<br>Edge Lookup: $O(1)$ |
| **Breadth-First Search (BFS)** | `Queue<String>` (`ArrayDeque`) | Multi-Source BFS starting from all active substations simultaneously. Identifies islanded consumer nodes unreachable from power sources. | $O(V + E)$ |
| **Visited Tracking** | `HashSet<String>` | Tracks energized nodes during BFS traversal to prevent infinite cycles in looped/meshed distribution rings. | Insert / Lookup: $O(1)$ |
| **Restoration Ranking** | `PriorityQueue<RestorationJob>` | Dynamically schedules restoration jobs so repair crews are dispatched to the most urgent faults first. | Insertion: $O(\log N)$<br>Poll: $O(\log N)$ |
| **Entity Registry** | `HashMap<String, NetworkNode>` | Fast $O(1)$ lookup for substations and consumer areas by ID. | $O(1)$ average |
| **Audit Log & History** | `ArrayList<EventHistory>` | Maintains ordered chronological sequence of grid events and switching actions. | Append: $O(1)$ |

### 2. Multi-Source BFS vs Single-Source BFS
- Standard single-source BFS explores outwards from one node.
- In real distribution grids, electricity is fed by **multiple generation sources and bulk supply substations** (e.g., North Grid, Thermal Substation East, Solar Auxiliary).
- Our implementation enqueues **all active source substations** at Level 0 before traversal begins. If Feeder 1 fails, a hospital might still receive electricity through an alternate tie-line connected to Substation 2.
- If all sources are marked `INACTIVE`, the algorithm immediately flags a **Total Grid Blackout**.

### 3. Java PriorityQueue & The Restoration Ranking Policy
Restoration jobs are sorted by the custom `RestorationPolicy` (`Comparator<RestorationJob>`):
1. **Critical Facilities Impacted (Highest First)**: A fault isolating a Hospital or Water Works is prioritized over general commercial loads, regardless of population size.
2. **Total Affected Consumers (Highest First)**: If two faults impact the same number of critical facilities (e.g., 0 or 1), the fault isolating 8,500 consumers is repaired before one isolating 1,200 consumers.
3. **Chronological Fault Time (FIFO)**: If impact scores are equal, the fault reported earlier is dispatched first.

### 4. Object-Oriented Programming (OOP) Principles Demonstrated

- **Encapsulation**:
  - All attributes in classes such as `NetworkNode`, `FeederLine`, `FaultReport`, and `RestorationJob` are declared `private`.
  - State transitions (such as changing feeder health or assigning a team) occur through validated methods and repository transactions.
- **Abstraction**:
  - `NetworkNode` is an `abstract class` defining universal contracts: `getNodeType()`, `isSourceSubstation()`, and `getConsumerCount()`.
  - Concrete details of power capacity versus consumer population are abstracted behind these interfaces.
- **Inheritance**:
  - `Substation` extends `NetworkNode`.
  - `ConsumerArea` extends `NetworkNode`.
  - `CriticalArea` and `GeneralArea` extend `ConsumerArea` (Hierarchical Inheritance).
- **Polymorphism**:
  - When the BFS engine iterates over `Collection<NetworkNode>`, it invokes `node.isSourceSubstation()` and `node.isActive()` polymorphically without inspecting underlying subclasses.
  - When calculating outages, `ca.isCritical()` executes the overridden method in `CriticalArea` (returns true) or `GeneralArea` (returns false).
- **Composition**:
  - `DistributionNetwork` is composed of `NetworkNode` and `FeederLine` objects linked via internal adjacency lists.
  - `RestorationJob` references `FaultReport` and `RepairTeam`.

---

## 🛡️ Input Validation & Grid Integrity Safeguards

- **Self-Connection Prevention**: Feeder lines cannot be created where `sourceNodeId == targetNodeId`.
- **Duplicate ID Detection**: Nodes, feeders, and backup configurations cannot be created with existing IDs.
- **Duplicate Active Fault Prevention**: Reporting a fault on a feeder that already has an unresolved active fault is rejected with HTTP 409 Conflict.
- **Referential Deletion Protection**: A substation or consumer area cannot be deleted while feeder lines remain connected to it.
- **Team Availability Validation**: Busy or off-duty repair teams cannot be double-booked to simultaneous restoration jobs.

---

## 👥 Contributors & Academic Acknowledgments

Developed as a College Mini-Project in Software Engineering, Graph Algorithms, and Java Spring Boot Application Development.
