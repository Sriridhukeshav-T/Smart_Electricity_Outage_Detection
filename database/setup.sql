-- ====================================================================
-- Smart Electricity Outage Detection and Restoration System
-- Database Initialization Script (Safe Setup)
-- Database: outage_db
-- ====================================================================

CREATE DATABASE IF NOT EXISTS outage_db;
USE outage_db;

-- --------------------------------------------------------------------
-- 1. Network Nodes (Substations & Consumer Areas)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS network_nodes (
    node_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    node_type ENUM('SUBSTATION', 'CONSUMER_AREA') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    is_source BOOLEAN NOT NULL DEFAULT FALSE,
    capacity_kva DOUBLE DEFAULT 10000.0,
    consumer_type ENUM('CRITICAL', 'GENERAL') NULL,
    critical_facility_name VARCHAR(100) NULL,
    consumer_count INT DEFAULT 0,
    location_x DOUBLE DEFAULT 100.0,
    location_y DOUBLE DEFAULT 100.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- --------------------------------------------------------------------
-- 2. Feeder Lines (Transmission/Distribution Lines between Nodes)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS feeder_lines (
    feeder_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    source_node_id VARCHAR(50) NOT NULL,
    target_node_id VARCHAR(50) NOT NULL,
    status ENUM('WORKING', 'FAILED') NOT NULL DEFAULT 'WORKING',
    capacity_amps DOUBLE DEFAULT 500.0,
    length_km DOUBLE DEFAULT 1.0,
    is_backup BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feeder_source FOREIGN KEY (source_node_id) REFERENCES network_nodes(node_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_feeder_target FOREIGN KEY (target_node_id) REFERENCES network_nodes(node_id) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- --------------------------------------------------------------------
-- 3. Approved Backup Connections
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS approved_backup_connections (
    backup_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    primary_feeder_id VARCHAR(50) NOT NULL,
    alternate_feeder_id VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    is_approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_backup_primary FOREIGN KEY (primary_feeder_id) REFERENCES feeder_lines(feeder_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_backup_alternate FOREIGN KEY (alternate_feeder_id) REFERENCES feeder_lines(feeder_id) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- --------------------------------------------------------------------
-- 4. Repair Teams
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS repair_teams (
    team_id VARCHAR(50) PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(50),
    status ENUM('AVAILABLE', 'BUSY', 'OFF_DUTY') NOT NULL DEFAULT 'AVAILABLE',
    current_assignment_id VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- --------------------------------------------------------------------
-- 5. Fault Reports
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fault_reports (
    fault_id VARCHAR(50) PRIMARY KEY,
    feeder_id VARCHAR(50) NOT NULL,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fault_cause VARCHAR(255) NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'HIGH',
    status ENUM('REPORTED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED') NOT NULL DEFAULT 'REPORTED',
    assigned_team_id VARCHAR(50) NULL,
    resolved_at TIMESTAMP NULL,
    notes TEXT NULL,
    CONSTRAINT fk_fault_feeder FOREIGN KEY (feeder_id) REFERENCES feeder_lines(feeder_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_fault_team FOREIGN KEY (assigned_team_id) REFERENCES repair_teams(team_id) ON UPDATE CASCADE ON DELETE SET NULL
);

-- --------------------------------------------------------------------
-- 6. Restoration Jobs
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restoration_jobs (
    job_id VARCHAR(50) PRIMARY KEY,
    fault_id VARCHAR(50) NOT NULL,
    feeder_id VARCHAR(50) NOT NULL,
    priority_score INT NOT NULL DEFAULT 0,
    priority_rank INT DEFAULT 0,
    critical_facilities_count INT DEFAULT 0,
    affected_consumers_count INT DEFAULT 0,
    status ENUM('QUEUED', 'ASSIGNED', 'IN_PROGRESS', 'RESTORED') DEFAULT 'QUEUED',
    assigned_team_id VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_fault FOREIGN KEY (fault_id) REFERENCES fault_reports(fault_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_job_feeder FOREIGN KEY (feeder_id) REFERENCES feeder_lines(feeder_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_job_team FOREIGN KEY (assigned_team_id) REFERENCES repair_teams(team_id) ON UPDATE CASCADE ON DELETE SET NULL
);

-- --------------------------------------------------------------------
-- 7. Event & Restoration History
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS event_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(50) NOT NULL,
    details TEXT NOT NULL,
    performed_by VARCHAR(100) DEFAULT 'System Operator',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ====================================================================
-- Initial Seed Data (Only inserted if tables are empty)
-- ====================================================================

-- Seed Network Nodes (12 nodes: 3 Substations + 9 Consumer Areas)
INSERT IGNORE INTO network_nodes (node_id, name, node_type, status, is_source, capacity_kva, consumer_type, critical_facility_name, consumer_count, location_x, location_y) VALUES
('SUB-01', 'Main Grid Substation North', 'SUBSTATION', 'ACTIVE', TRUE, 50000.0, NULL, NULL, 0, 150, 100),
('SUB-02', 'Thermal Power Substation East', 'SUBSTATION', 'ACTIVE', TRUE, 45000.0, NULL, NULL, 0, 700, 100),
('SUB-03', 'Auxiliary Solar Substation West', 'SUBSTATION', 'ACTIVE', TRUE, 25000.0, NULL, NULL, 0, 150, 480),
('CONS-01', 'Metro General Hospital & Trauma Center', 'CONSUMER_AREA', 'ACTIVE', FALSE, 5000.0, 'CRITICAL', 'Metro Hospital', 2800, 420, 100),
('CONS-02', 'Central Water Purification & Pumping', 'CONSUMER_AREA', 'ACTIVE', FALSE, 4000.0, 'CRITICAL', 'Central Water Works', 1200, 420, 260),
('CONS-03', 'District Emergency & Police Headquarters', 'CONSUMER_AREA', 'ACTIVE', FALSE, 3000.0, 'CRITICAL', 'Police & Fire Dispatch', 950, 560, 260),
('CONS-04', 'North Commercial Downtown District', 'CONSUMER_AREA', 'ACTIVE', FALSE, 8000.0, 'GENERAL', NULL, 8500, 150, 260),
('CONS-05', 'Oakwood Residential Sector A', 'CONSUMER_AREA', 'ACTIVE', FALSE, 6000.0, 'GENERAL', NULL, 5200, 350, 400),
('CONS-06', 'Riverside Residential Sector B', 'CONSUMER_AREA', 'ACTIVE', FALSE, 5000.0, 'GENERAL', NULL, 4100, 560, 400),
('CONS-07', 'Industrial Tech & Manufacturing Park', 'CONSUMER_AREA', 'ACTIVE', FALSE, 12000.0, 'GENERAL', NULL, 6800, 700, 260),
('CONS-08', 'Highland Suburban Residential', 'CONSUMER_AREA', 'ACTIVE', FALSE, 4000.0, 'GENERAL', NULL, 3400, 150, 600),
('CONS-09', 'Southport Cold Storage & Clinic Zone', 'CONSUMER_AREA', 'ACTIVE', FALSE, 3500.0, 'CRITICAL', 'Vaccine Cold Storage', 1600, 700, 480);

-- Seed Feeder Lines (14 feeders)
INSERT IGNORE INTO feeder_lines (feeder_id, name, source_node_id, target_node_id, status, capacity_amps, length_km, is_backup) VALUES
('FEED-01', 'Feeder North-Hospital Main', 'SUB-01', 'CONS-01', 'WORKING', 600.0, 2.5, FALSE),
('FEED-02', 'Feeder North-Downtown Line', 'SUB-01', 'CONS-04', 'WORKING', 500.0, 1.8, FALSE),
('FEED-03', 'Inter-Tie Hospital to Water Works', 'CONS-01', 'CONS-02', 'WORKING', 450.0, 1.2, FALSE),
('FEED-04', 'Feeder Water Works to Oakwood', 'CONS-02', 'CONS-05', 'WORKING', 400.0, 2.0, FALSE),
('FEED-05', 'Feeder East to Industrial Park', 'SUB-02', 'CONS-07', 'WORKING', 800.0, 3.1, FALSE),
('FEED-06', 'Feeder Industrial to Emergency HQ', 'CONS-07', 'CONS-03', 'WORKING', 450.0, 1.5, FALSE),
('FEED-07', 'Feeder Emergency HQ to Riverside', 'CONS-03', 'CONS-06', 'WORKING', 400.0, 1.7, FALSE),
('FEED-08', 'Feeder Solar to Highland Suburb', 'SUB-03', 'CONS-08', 'WORKING', 350.0, 2.2, FALSE),
('FEED-09', 'Feeder Highland to Oakwood Tie', 'CONS-08', 'CONS-05', 'WORKING', 300.0, 1.9, FALSE),
('FEED-10', 'Feeder Riverside to Southport Clinic', 'CONS-06', 'CONS-09', 'WORKING', 350.0, 2.8, FALSE),
('FEED-11', 'Feeder Downtown to Water Works Intertie', 'CONS-04', 'CONS-02', 'WORKING', 400.0, 2.1, FALSE),
('FEED-12', 'Feeder Oakwood to Riverside Connector', 'CONS-05', 'CONS-06', 'WORKING', 350.0, 1.4, FALSE),
('FEED-13', 'Backup Line East to Metro Hospital', 'SUB-02', 'CONS-01', 'WORKING', 600.0, 4.0, TRUE),
('FEED-14', 'Backup Line Solar to Water Works', 'SUB-03', 'CONS-02', 'WORKING', 400.0, 3.5, TRUE);

-- Seed Approved Backup Connections (3 backup paths)
INSERT IGNORE INTO approved_backup_connections (backup_id, name, primary_feeder_id, alternate_feeder_id, description, is_approved) VALUES
('BKP-01', 'Hospital Primary Alternate Loop', 'FEED-01', 'FEED-13', 'Connects Metro Hospital to Thermal Substation 2 when Feeder 1 fails', TRUE),
('BKP-02', 'Water Works Solar Backup Feeder', 'FEED-03', 'FEED-14', 'Connects Central Water Works directly to Auxiliary Solar Substation 3', TRUE),
('BKP-03', 'Oakwood Sector Secondary Tie-Line', 'FEED-04', 'FEED-09', 'Reroutes Oakwood residential power via Solar Substation 3 and Highland line', TRUE);

-- Seed Repair Teams (3 teams)
INSERT IGNORE INTO repair_teams (team_id, team_name, contact_number, status, current_assignment_id) VALUES
('TEAM-A', 'Alpha Rapid Response Crew', '+91 98450-10001', 'AVAILABLE', NULL),
('TEAM-B', 'Beta High Voltage Specialists', '+91 98450-10002', 'AVAILABLE', NULL),
('TEAM-C', 'Gamma Substation & Feeder Team', '+91 98450-10003', 'AVAILABLE', NULL);

-- Seed Event History initial record
INSERT IGNORE INTO event_history (history_id, event_type, target_id, details, performed_by) VALUES
(1, 'SYSTEM_INITIALIZED', 'GRID-CORE', 'Distribution network database initialized with 12 nodes, 14 feeder lines, and 3 repair teams.', 'System Setup');
