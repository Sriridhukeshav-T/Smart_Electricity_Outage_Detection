/**
 * Smart Electricity Outage Detection and Restoration System
 * Frontend Client Application
 * Communicates with Spring Boot REST API on http://localhost:8080/api
 */

const API_BASE = 'http://localhost:8080/api';

// State Cache
let state = {
  stats: null,
  nodes: [],
  feeders: [],
  faults: [],
  jobs: [],
  teams: [],
  backups: [],
  history: [],
  outageResult: null,
  activeTab: 'dashboard'
};

// ====================================================================
// Initialization & Event Listeners
// ====================================================================

document.addEventListener('DOMContentLoaded', () => {
  setupNavigation();
  setupModals();
  setupFormHandlers();
  setupFilterHandlers();
  
  // Initial check & load
  checkBackendHealth();
  refreshAllData();

  // Periodic heartbeat / refresh every 15 seconds
  setInterval(refreshAllData, 15000);
});

function setupNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const tabId = item.getAttribute('data-tab');
      switchTab(tabId);
    });
  });

  const refreshBtn = document.getElementById('refreshAllBtn');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
      showToast('Refreshing grid data...', 'info');
      refreshAllData();
    });
  }

  const runBfsBtn = document.getElementById('runBfsBtn');
  if (runBfsBtn) {
    runBfsBtn.addEventListener('click', runBfsScan);
  }

  const recalcQueueBtn = document.getElementById('recalcQueueBtn');
  if (recalcQueueBtn) {
    recalcQueueBtn.addEventListener('click', recalculatePriorityQueue);
  }

  const refreshHistoryBtn = document.getElementById('refreshHistoryBtn');
  if (refreshHistoryBtn) {
    refreshHistoryBtn.addEventListener('click', loadHistory);
  }

  const fitMapBtn = document.getElementById('btnFitMap');
  if (fitMapBtn) {
    fitMapBtn.addEventListener('click', () => {
      renderNetworkTopology(state.nodes, state.feeders, state.outageResult, 'networkTopologySvgFull');
    });
  }
}

function switchTab(tabId) {
  state.activeTab = tabId;

  // Update nav item classes
  document.querySelectorAll('.nav-item').forEach(el => {
    el.classList.toggle('active', el.getAttribute('data-tab') === tabId);
  });

  // Update tab pane visibility
  document.querySelectorAll('.tab-pane').forEach(el => {
    el.classList.toggle('active', el.id === `tab-${tabId}`);
  });

  // Update header text
  const titles = {
    dashboard: ['Grid Overview Dashboard', 'Real-time distribution topology, BFS outage detection & priority restoration'],
    topology: ['Visual Grid Distribution Map', 'Interactive SVG canvas of electrical nodes, feeder lines & energized paths'],
    nodes: ['Network Nodes & Substations', 'Manage transmission sources, step-downs, and consumer load centers'],
    feeders: ['Distribution Feeder Lines', 'Manage electrical feeders, line capacities, and operational statuses'],
    faults: ['Fault Management & Reporting', 'Report feeder line disruptions and monitor incident response'],
    outages: ['Outage Analysis Engine', 'Multi-Source Breadth-First Search (BFS) reachability evaluation'],
    restoration: ['Restoration Priority Queue', 'Java PriorityQueue ranking based on critical facilities, load, and FIFO'],
    teams: ['Field Repair Teams', 'Crew availability, assignment tracking, and dispatch control'],
    backups: ['Approved Backup Routes', 'Operator-predefined alternate tie-lines with automated route evaluation'],
    history: ['Event History & Audit Log', 'Chronological log of grid disruptions, team assignments, and line recoveries']
  };

  const titleElem = document.getElementById('currentSectionTitle');
  const subElem = document.getElementById('currentSectionSubtitle');
  if (titleElem && titles[tabId]) titleElem.textContent = titles[tabId][0];
  if (subElem && titles[tabId]) subElem.textContent = titles[tabId][1];

  // If switched to visual map, render full SVG
  if (tabId === 'topology') {
    renderNetworkTopology(state.nodes, state.feeders, state.outageResult, 'networkTopologySvgFull');
  }
}

// ====================================================================
// Health Check & Master Data Fetch
// ====================================================================

async function checkBackendHealth() {
  const statusEl = document.getElementById('backendStatus');
  try {
    const res = await fetch(`${API_BASE}/health`);
    if (res.ok) {
      statusEl.innerHTML = `<span class="status-dot online"></span><span>Backend Online (:8080)</span>`;
    } else {
      statusEl.innerHTML = `<span class="status-dot offline"></span><span>Backend Error (${res.status})</span>`;
    }
  } catch (err) {
    statusEl.innerHTML = `<span class="status-dot offline"></span><span>Backend Disconnected</span>`;
  }
}

async function refreshAllData() {
  try {
    await Promise.all([
      loadDashboardStats(),
      loadNodes(),
      loadFeeders(),
      loadFaults(),
      loadRestorationQueue(),
      loadTeams(),
      loadBackups(),
      loadHistory()
    ]);
    checkBackendHealth();
  } catch (err) {
    console.error('Data refresh error:', err);
  }
}

// ====================================================================
// 1. Dashboard
// ====================================================================

async function loadDashboardStats() {
  try {
    const res = await fetch(`${API_BASE}/dashboard/stats`);
    const json = await res.json();
    if (!json.success) return;

    state.stats = json.data;
    const s = state.stats;

    // Update KPI UI
    document.getElementById('kpiHealthValue').textContent = `${s.gridHealthPercentage}%`;
    document.getElementById('kpiGridStatus').textContent = `Status: ${s.gridStatus.replace('_', ' ')}`;

    const healthCard = document.getElementById('kpiHealthCard');
    healthCard.className = 'kpi-card';
    if (s.gridStatus === 'NORMAL') healthCard.classList.add('text-success');
    else if (s.gridStatus === 'TOTAL_BLACKOUT' || s.gridStatus === 'CRITICAL_OUTAGE') healthCard.classList.add('alert-card');

    document.getElementById('kpiActiveSources').textContent = `${s.activeSources} / ${s.totalSubstations}`;
    document.getElementById('kpiTotalNodes').textContent = `${s.totalNodes} total nodes registered`;

    document.getElementById('kpiFeeders').textContent = `${s.workingFeeders} / ${s.totalFeeders}`;
    document.getElementById('kpiFailedFeeders').textContent = `${s.failedFeeders} failed lines`;

    document.getElementById('kpiAffectedConsumers').textContent = s.affectedConsumers.toLocaleString();
    document.getElementById('kpiAffectedCritical').textContent = `${s.affectedCriticalFacilities} critical facilities affected`;

    document.getElementById('kpiActiveFaults').textContent = s.activeFaults;
    document.getElementById('kpiQueuedJobs').textContent = `${s.queuedRestorationJobs} jobs in PriorityQueue`;

    document.getElementById('kpiTeamsAvailable').textContent = `${s.availableTeams} Available`;
    document.getElementById('kpiTeamsBusy').textContent = `${s.busyTeams} currently dispatched`;

    // Fetch Outage Results for Topo Graph
    await runBfsScan(false);
  } catch (err) {
    console.warn('Failed to load dashboard stats:', err);
  }
}

// ====================================================================
// 2. Network Topology SVG Visualizer
// ====================================================================

function renderNetworkTopology(nodes, feeders, outageResult, svgId = 'networkTopologySvg') {
  const svg = document.getElementById(svgId);
  if (!svg || !nodes || nodes.length === 0) return;

  svg.innerHTML = '';

  const energizedSet = new Set((outageResult && outageResult.energizedNodeIds) || []);
  const isBlackout = outageResult && outageResult.gridBlackout;

  // Build node coordinate map
  const nodeCoords = {};
  nodes.forEach(n => {
    nodeCoords[n.nodeId] = {
      x: n.locationX || 100,
      y: n.locationY || 100,
      node: n
    };
  });

  // 1. Draw Feeder Lines (Edges)
  feeders.forEach(f => {
    const src = nodeCoords[f.sourceNodeId];
    const tgt = nodeCoords[f.targetNodeId];
    if (!src || !tgt) return;

    const isWorking = f.status === 'WORKING';
    const isBackup = f.backup === true;

    const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line.setAttribute('x1', src.x);
    line.setAttribute('y1', src.y);
    line.setAttribute('x2', tgt.x);
    line.setAttribute('y2', tgt.y);

    let cls = 'svg-edge ';
    if (!isWorking) cls += 'failed';
    else if (isBackup) cls += 'backup';
    else cls += 'working';

    line.setAttribute('class', cls);
    line.setAttribute('data-feeder-id', f.feederId);

    // Title tooltip
    const title = document.createElementNS('http://www.w3.org/2000/svg', 'title');
    title.textContent = `Feeder: ${f.feederId} (${f.name})\n${f.sourceNodeId} <-> ${f.targetNodeId}\nStatus: ${f.status} | Capacity: ${f.capacityAmps}A`;
    line.appendChild(title);

    line.addEventListener('click', () => showFeederDetails(f));

    svg.appendChild(line);
  });

  // 2. Draw Network Nodes
  nodes.forEach(n => {
    const coords = nodeCoords[n.nodeId];
    if (!coords) return;

    const isSubstation = n.nodeType === 'SUBSTATION';
    const isSource = isSubstation && n.source;
    const isCritical = n.consumerType === 'CRITICAL';
    const isEnergized = !isBlackout && energizedSet.has(n.nodeId);

    const group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    group.setAttribute('class', 'svg-node');
    group.setAttribute('transform', `translate(${coords.x}, ${coords.y})`);

    if (isSubstation) {
      // Substation: Square shape
      const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
      rect.setAttribute('x', -20);
      rect.setAttribute('y', -20);
      rect.setAttribute('width', 40);
      rect.setAttribute('height', 40);
      rect.setAttribute('rx', 6);
      rect.setAttribute('fill', n.status === 'ACTIVE' ? '#1e3a8a' : '#64748b');
      rect.setAttribute('stroke', isEnergized ? '#22c55e' : '#dc2626');
      rect.setAttribute('stroke-width', 2.5);
      group.appendChild(rect);

      // Icon symbol inside
      const icon = document.createElementNS('http://www.w3.org/2000/svg', 'text');
      icon.setAttribute('text-anchor', 'middle');
      icon.setAttribute('dy', 5);
      icon.setAttribute('fill', '#ffffff');
      icon.setAttribute('font-size', '16');
      icon.textContent = '⚡';
      group.appendChild(icon);
    } else {
      // Consumer Area: Circle shape
      const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
      circle.setAttribute('r', 16);
      
      let fill = '#0284c7';
      if (isCritical) fill = '#dc2626';
      if (!isEnergized) fill = '#94a3b8'; // De-energized gray

      circle.setAttribute('fill', fill);
      circle.setAttribute('stroke', isEnergized ? '#16a34a' : '#b91c1c');
      circle.setAttribute('stroke-width', isEnergized ? 2 : 3);
      group.appendChild(circle);

      // Small icon
      const icon = document.createElementNS('http://www.w3.org/2000/svg', 'text');
      icon.setAttribute('text-anchor', 'middle');
      icon.setAttribute('dy', 4);
      icon.setAttribute('fill', '#ffffff');
      icon.setAttribute('font-size', '11');
      icon.textContent = isCritical ? '🏥' : '🏘️';
      group.appendChild(icon);
    }

    // Node Label
    const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
    text.setAttribute('class', 'svg-node-text');
    text.setAttribute('text-anchor', 'middle');
    text.setAttribute('dy', 30);
    text.textContent = n.nodeId;
    group.appendChild(text);

    // Subtext (Name snippet)
    const subtext = document.createElementNS('http://www.w3.org/2000/svg', 'text');
    subtext.setAttribute('class', 'svg-node-subtext');
    subtext.setAttribute('text-anchor', 'middle');
    subtext.setAttribute('dy', 42);
    const shortName = n.name.length > 18 ? n.name.substring(0, 16) + '...' : n.name;
    subtext.textContent = shortName;
    group.appendChild(subtext);

    // Tooltip
    const title = document.createElementNS('http://www.w3.org/2000/svg', 'title');
    let tip = `${n.nodeId}: ${n.name}\nType: ${n.nodeType} | Status: ${n.status}\nEnergized: ${isEnergized ? 'YES' : 'NO'}`;
    if (!isSubstation) {
      tip += `\nClassification: ${n.consumerType || 'GENERAL'}\nConsumers: ${n.consumerCount || 0}`;
      if (n.criticalFacilityName) tip += `\nCritical Unit: ${n.criticalFacilityName}`;
    }
    title.textContent = tip;
    group.appendChild(title);

    group.addEventListener('click', () => showNodeDetails(n, isEnergized));

    svg.appendChild(group);
  });
}

function showNodeDetails(node, isEnergized) {
  const panel = document.getElementById('topologyDetailsPanel');
  if (!panel) return;

  panel.classList.remove('hidden');
  panel.innerHTML = `
    <div class="card p-3">
      <div class="d-flex justify-content-between align-items-center">
        <h4>${node.nodeId} — ${node.name}</h4>
        <button class="btn btn-outline btn-sm" onclick="document.getElementById('topologyDetailsPanel').classList.add('hidden')">Close</button>
      </div>
      <p class="mt-2"><strong>Type:</strong> ${node.nodeType} | <strong>Operating Status:</strong> ${node.status}</p>
      <p><strong>Grid Energization:</strong> <span class="badge ${isEnergized ? 'badge-success' : 'badge-danger'}">${isEnergized ? 'ENERGIZED' : 'DE-ENERGIZED (OUTAGE)'}</span></p>
      ${node.nodeType === 'CONSUMER_AREA' ? `
        <p><strong>Consumers:</strong> ${(node.consumerCount || 0).toLocaleString()} | <strong>Classification:</strong> ${node.consumerType || 'GENERAL'}</p>
        ${node.criticalFacilityName ? `<p><strong>Critical Lifeline:</strong> ${node.criticalFacilityName}</p>` : ''}
      ` : `
        <p><strong>Substation Role:</strong> ${node.source ? 'Source Power Substation' : 'Step-Down Substation'} | <strong>Capacity:</strong> ${node.capacityKva || 0} kVA</p>
        <button class="btn btn-outline btn-sm mt-2" onclick="toggleNodeStatus('${node.nodeId}')">Toggle Substation Active/Inactive</button>
      `}
    </div>
  `;
}

function showFeederDetails(feeder) {
  const panel = document.getElementById('topologyDetailsPanel');
  if (!panel) return;

  panel.classList.remove('hidden');
  panel.innerHTML = `
    <div class="card p-3">
      <div class="d-flex justify-content-between align-items-center">
        <h4>Feeder ${feeder.feederId} — ${feeder.name}</h4>
        <button class="btn btn-outline btn-sm" onclick="document.getElementById('topologyDetailsPanel').classList.add('hidden')">Close</button>
      </div>
      <p class="mt-2"><strong>Connected Nodes:</strong> <code>${feeder.sourceNodeId}</code> &harr; <code>${feeder.targetNodeId}</code></p>
      <p><strong>Current Status:</strong> <span class="badge ${feeder.status === 'WORKING' ? 'badge-success' : 'badge-danger'}">${feeder.status}</span></p>
      <p><strong>Capacity:</strong> ${feeder.capacityAmps} A | <strong>Length:</strong> ${feeder.lengthKm} km | <strong>Role:</strong> ${feeder.backup ? 'Operator Approved Backup' : 'Standard Distribution Line'}</p>
      <div class="mt-3">
        ${feeder.status === 'WORKING' ? `
          <button class="btn btn-danger btn-sm" onclick="promptReportFault('${feeder.feederId}')">Report Fault On This Line</button>
          <button class="btn btn-outline btn-sm" onclick="toggleFeederStatus('${feeder.feederId}', 'FAILED')">Mark as FAILED</button>
        ` : `
          <button class="btn btn-primary btn-sm" onclick="toggleFeederStatus('${feeder.feederId}', 'WORKING')">Restore Feeder to WORKING</button>
        `}
      </div>
    </div>
  `;
}

// ====================================================================
// 3. Network Nodes Management
// ====================================================================

async function loadNodes() {
  try {
    const res = await fetch(`${API_BASE}/nodes`);
    const json = await res.json();
    if (!json.success) return;

    state.nodes = json.data;
    renderNodesTable(state.nodes);
    populateNodeSelects(state.nodes);
  } catch (err) {
    console.error('Failed to load nodes:', err);
  }
}

function renderNodesTable(nodes) {
  const tbody = document.getElementById('nodesTableBody');
  if (!tbody) return;

  if (!nodes || nodes.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No network nodes found matching criteria.</td></tr>`;
    return;
  }

  const energizedSet = new Set((state.outageResult && state.outageResult.energizedNodeIds) || []);

  tbody.innerHTML = nodes.map(n => {
    const isSub = n.nodeType === 'SUBSTATION';
    const isEnergized = energizedSet.has(n.nodeId);
    let details = isSub ? (n.source ? '⚡ Active Source Substation' : 'Step-Down Substation') :
      (n.critical ? `🏥 Critical: ${n.criticalFacilityName}` : 'General Sector');

    return `
      <tr>
        <td><code>${n.nodeId}</code></td>
        <td><strong>${n.name}</strong></td>
        <td><span class="badge ${isSub ? 'badge-info' : 'badge-secondary'}">${n.nodeType}</span></td>
        <td>${details}</td>
        <td>${isSub ? '—' : (n.consumerCount || 0).toLocaleString()}</td>
        <td>
          <span class="badge ${n.status === 'ACTIVE' ? 'badge-success' : 'badge-secondary'}">${n.status}</span>
        </td>
        <td>
          <span class="badge ${isEnergized ? 'badge-success' : 'badge-danger'}">
            ${isEnergized ? 'ENERGIZED' : 'DE-ENERGIZED'}
          </span>
        </td>
        <td>
          <div class="d-flex gap-1">
            <button class="btn btn-outline btn-sm" onclick="toggleNodeStatus('${n.nodeId}')" title="Toggle active status">
              ${n.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
            </button>
            <button class="btn btn-outline btn-sm text-danger" onclick="deleteNode('${n.nodeId}')" title="Delete node">
              🗑️
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

async function toggleNodeStatus(nodeId) {
  try {
    const res = await fetch(`${API_BASE}/nodes/${nodeId}/toggle-status`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({})
    });
    const json = await res.json();
    if (json.success) {
      showToast(`Node ${nodeId} status updated.`, 'success');
      refreshAllData();
    } else {
      showToast(json.message, 'error');
    }
  } catch (err) {
    showToast('Failed to toggle node status.', 'error');
  }
}

async function deleteNode(nodeId) {
  if (!confirm(`Are you sure you want to delete network node ${nodeId}?`)) return;

  try {
    const res = await fetch(`${API_BASE}/nodes/${nodeId}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast(`Node ${nodeId} deleted successfully.`, 'success');
      refreshAllData();
    } else {
      showToast(json.message, 'error');
    }
  } catch (err) {
    showToast('Failed to delete node.', 'error');
  }
}

// ====================================================================
// 4. Feeder Lines Management
// ====================================================================

async function loadFeeders() {
  try {
    const res = await fetch(`${API_BASE}/feeders`);
    const json = await res.json();
    if (!json.success) return;

    state.feeders = json.data;
    renderFeedersTable(state.feeders);
    populateFeederSelects(state.feeders);
  } catch (err) {
    console.error('Failed to load feeders:', err);
  }
}

function renderFeedersTable(feeders) {
  const tbody = document.getElementById('feedersTableBody');
  if (!tbody) return;

  if (!feeders || feeders.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">No feeder lines found matching criteria.</td></tr>`;
    return;
  }

  tbody.innerHTML = feeders.map(f => {
    const isWorking = f.status === 'WORKING';
    return `
      <tr>
        <td><code>${f.feederId}</code></td>
        <td><strong>${f.name}</strong></td>
        <td><code>${f.sourceNodeId}</code> &harr; <code>${f.targetNodeId}</code></td>
        <td>${f.capacityAmps} A / ${f.lengthKm} km</td>
        <td><span class="badge ${f.backup ? 'badge-warning' : 'badge-secondary'}">${f.backup ? 'Backup Feeder' : 'Primary Feeder'}</span></td>
        <td>
          <span class="badge ${isWorking ? 'badge-success' : 'badge-danger'}">${f.status}</span>
        </td>
        <td>
          <div class="d-flex gap-1">
            ${isWorking ? `
              <button class="btn btn-outline btn-sm text-danger" onclick="promptReportFault('${f.feederId}')">
                Report Fault
              </button>
              <button class="btn btn-outline btn-sm" onclick="toggleFeederStatus('${f.feederId}', 'FAILED')">
                Set Failed
              </button>
            ` : `
              <button class="btn btn-primary btn-sm" onclick="toggleFeederStatus('${f.feederId}', 'WORKING')">
                Restore
              </button>
            `}
            <button class="btn btn-outline btn-sm text-danger" onclick="deleteFeeder('${f.feederId}')" title="Delete Feeder">
              🗑️
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

async function toggleFeederStatus(feederId, newStatus) {
  try {
    const res = await fetch(`${API_BASE}/feeders/${feederId}/status`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    });
    const json = await res.json();
    if (json.success) {
      showToast(`Feeder ${feederId} status changed to ${newStatus}.`, 'success');
      refreshAllData();
    } else {
      showToast(json.message, 'error');
    }
  } catch (err) {
    showToast('Failed to update feeder status.', 'error');
  }
}

async function deleteFeeder(feederId) {
  if (!confirm(`Are you sure you want to delete feeder line ${feederId}?`)) return;

  try {
    const res = await fetch(`${API_BASE}/feeders/${feederId}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast(`Feeder ${feederId} deleted successfully.`, 'success');
      refreshAllData();
    } else {
      showToast(json.message, 'error');
    }
  } catch (err) {
    showToast('Failed to delete feeder line.', 'error');
  }
}

// ====================================================================
// 5. Fault Management
// ====================================================================

async function loadFaults() {
  try {
    const res = await fetch(`${API_BASE}/faults`);
    const json = await res.json();
    if (!json.success) return;

    state.faults = json.data;
    renderFaultsTable(state.faults);
  } catch (err) {
    console.error('Failed to load faults:', err);
  }
}

function renderFaultsTable(faults) {
  const tbody = document.getElementById('faultsTableBody');
  if (!tbody) return;

  if (!faults || faults.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No fault records found. Grid is operational.</td></tr>`;
    return;
  }

  tbody.innerHTML = faults.map(f => {
    const isActive = f.status !== 'RESOLVED';
    const severityBadge = f.severity === 'CRITICAL' ? 'badge-danger' :
      (f.severity === 'HIGH' ? 'badge-warning' : 'badge-secondary');

    return `
      <tr>
        <td><code>${f.faultId}</code></td>
        <td><code>${f.feederId}</code></td>
        <td><strong>${f.faultCause}</strong><br><small class="text-muted">${f.notes || ''}</small></td>
        <td><span class="badge ${severityBadge}">${f.severity}</span></td>
        <td>${formatDate(f.reportedAt)}</td>
        <td>
          <span class="badge ${isActive ? 'badge-danger' : 'badge-success'}">${f.status}</span>
        </td>
        <td>${f.assignedTeamId ? `<code>${f.assignedTeamId}</code>` : '<span class="text-muted">Unassigned</span>'}</td>
        <td>
          ${isActive ? `
            <button class="btn btn-primary btn-sm" onclick="openAssignTeamModal('${f.faultId}', '${f.feederId}')">
              Assign Team
            </button>
          ` : `
            <span class="text-success font-weight-bold">Resolved</span>
          `}
        </td>
      </tr>
    `;
  }).join('');
}

// ====================================================================
// 6. Outage Analysis (BFS Engine)
// ====================================================================

async function runBfsScan(showToastNotification = true) {
  try {
    const res = await fetch(`${API_BASE}/outages/detect`);
    const json = await res.json();
    if (!json.success) return;

    state.outageResult = json.data;
    const r = state.outageResult;

    // Update Outage Tab stats
    const banner = document.getElementById('outageStatusBanner');
    const statusText = document.getElementById('outageStatusText');
    if (banner && statusText) {
      statusText.textContent = r.statusMessage;
      if (r.gridBlackout) {
        banner.className = 'alert-box alert-danger';
      } else if (r.disconnectedConsumerAreas.length > 0) {
        banner.className = 'alert-box alert-warning';
      } else {
        banner.className = 'alert-box alert-success';
      }
    }

    const setVal = (id, val) => {
      const el = document.getElementById(id);
      if (el) el.textContent = val;
    };

    setVal('bfsActiveSources', r.activeSourcesCount);
    setVal('bfsEnergizedNodes', (r.energizedNodeIds || []).length);
    setVal('bfsDeenergizedNodes', (r.deenergizedNodeIds || []).length);
    setVal('bfsCriticalAffected', r.totalCriticalFacilitiesAffected);
    setVal('bfsConsumersAffected', r.totalConsumersAffected.toLocaleString());

    // Disconnected Areas Table
    const tbody = document.getElementById('disconnectedAreasTableBody');
    if (tbody) {
      if (r.disconnectedConsumerAreas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-success font-weight-bold">All consumer areas are energized. No active outages.</td></tr>`;
      } else {
        tbody.innerHTML = r.disconnectedConsumerAreas.map(a => `
          <tr class="table-danger">
            <td><code>${a.nodeId}</code></td>
            <td><strong>${a.name}</strong></td>
            <td><span class="badge ${a.critical ? 'badge-danger' : 'badge-secondary'}">${a.consumerType || 'GENERAL'}</span></td>
            <td>${a.criticalFacilityName ? `🏥 ${a.criticalFacilityName}` : '—'}</td>
            <td><strong>${(a.consumerCount || 0).toLocaleString()}</strong></td>
            <td>
              <a href="#restoration" class="btn btn-outline btn-sm" onclick="switchTab('restoration')">View Priority Queue</a>
            </td>
          </tr>
        `).join('');
      }
    }

    // Refresh Topo SVGs
    renderNetworkTopology(state.nodes, state.feeders, state.outageResult, 'networkTopologySvg');
    if (state.activeTab === 'topology') {
      renderNetworkTopology(state.nodes, state.feeders, state.outageResult, 'networkTopologySvgFull');
    }

    if (showToastNotification) {
      showToast('BFS Outage Detection completed.', 'info');
    }
  } catch (err) {
    console.error('Failed to run BFS scan:', err);
  }
}

// ====================================================================
// 7. Restoration Priority Queue (Java PriorityQueue)
// ====================================================================

async function loadRestorationQueue() {
  try {
    const res = await fetch(`${API_BASE}/restoration/queue`);
    const json = await res.json();
    if (!json.success) return;

    state.jobs = json.data;
    renderRestorationQueueTable(state.jobs);
    renderDashboardQueueMini(state.jobs);
  } catch (err) {
    console.error('Failed to load restoration queue:', err);
  }
}

function renderRestorationQueueTable(jobs) {
  const tbody = document.getElementById('restorationQueueTableBody');
  if (!tbody) return;

  if (!jobs || jobs.length === 0) {
    tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted">Priority Queue is empty. No active restoration jobs needed.</td></tr>`;
    return;
  }

  tbody.innerHTML = jobs.map(j => {
    const isRestored = j.status === 'RESTORED';
    return `
      <tr>
        <td><span class="step-num">${j.priorityRank}</span></td>
        <td><code>${j.jobId}</code></td>
        <td><code>${j.feederId}</code></td>
        <td><strong class="${j.criticalFacilitiesCount > 0 ? 'text-danger' : ''}">${j.criticalFacilitiesCount}</strong></td>
        <td><strong>${(j.affectedConsumersCount || 0).toLocaleString()}</strong></td>
        <td><code>${j.priorityScore}</code></td>
        <td><span class="badge ${j.status === 'ASSIGNED' ? 'badge-warning' : (j.status === 'QUEUED' ? 'badge-danger' : 'badge-success')}">${j.status}</span></td>
        <td>${j.assignedTeamId ? `<code>${j.assignedTeamId}</code>` : '<span class="text-muted">None</span>'}</td>
        <td>
          <div class="d-flex gap-1">
            ${!j.assignedTeamId ? `
              <button class="btn btn-primary btn-sm" onclick="openAssignTeamModal('${j.jobId}', '${j.feederId}', ${j.priorityRank}, ${j.criticalFacilitiesCount}, ${j.affectedConsumersCount})">
                Assign Team
              </button>
            ` : `
              <button class="btn btn-success btn-sm" onclick="completeRestoration('${j.jobId}')">
                ✓ Restore Feeder
              </button>
            `}
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function renderDashboardQueueMini(jobs) {
  const list = document.getElementById('dashboardQueueList');
  if (!list) return;

  if (!jobs || jobs.length === 0) {
    list.innerHTML = `<div class="empty-state">No active restoration jobs. Grid is healthy.</div>`;
    return;
  }

  list.innerHTML = jobs.slice(0, 4).map(j => `
    <div class="algo-step mb-2" style="padding: 8px 12px;">
      <span class="step-num">#${j.priorityRank}</span>
      <div class="flex-1">
        <div class="d-flex justify-content-between">
          <strong>Feeder ${j.feederId}</strong>
          <span class="badge ${j.status === 'ASSIGNED' ? 'badge-warning' : 'badge-danger'}">${j.status}</span>
        </div>
        <small class="text-muted">Critical: ${j.criticalFacilitiesCount} | Consumers: ${(j.affectedConsumersCount || 0).toLocaleString()}</small>
      </div>
      <div>
        ${!j.assignedTeamId ? `
          <button class="btn btn-outline btn-sm" onclick="openAssignTeamModal('${j.jobId}', '${j.feederId}', ${j.priorityRank}, ${j.criticalFacilitiesCount}, ${j.affectedConsumersCount})">Dispatch</button>
        ` : `
          <button class="btn btn-primary btn-sm" onclick="completeRestoration('${j.jobId}')">Restore</button>
        `}
      </div>
    </div>
  `).join('');
}

async function recalculatePriorityQueue() {
  try {
    const res = await fetch(`${API_BASE}/restoration/recalculate`, { method: 'POST' });
    const json = await res.json();
    if (json.success) {
      showToast('Priority Queue recalculated using Java PriorityQueue comparator.', 'success');
      loadRestorationQueue();
    } else {
      showToast(json.message, 'error');
    }
  } catch (err) {
    showToast('Failed to recalculate priorities.', 'error');
  }
}

async function completeRestoration(jobId) {
  if (!confirm(`Confirm completion of repairs for ${jobId}?\nThis will energize the feeder line and restore connected consumers.`)) return;

  try {
    const res = await fetch(`${API_BASE}/restoration/complete/${jobId}`, { method: 'POST' });
    const json = await res.json();
    if (json.success) {
      showToast(`Restoration complete! Feeder energized and repair crew released.`, 'success');
      refreshAllData();
    } else {
      showToast(json.message, 'error');
    }
  } catch (err) {
    showToast('Failed to complete restoration.', 'error');
  }
}

// ====================================================================
// 8. Repair Teams
// ====================================================================

async function loadTeams() {
  try {
    const res = await fetch(`${API_BASE}/teams`);
    const json = await res.json();
    if (!json.success) return;

    state.teams = json.data;
    renderTeamsGrid(state.teams);
  } catch (err) {
    console.error('Failed to load teams:', err);
  }
}

function renderTeamsGrid(teams) {
  const container = document.getElementById('teamsGridContainer');
  if (!container) return;

  if (!teams || teams.length === 0) {
    container.innerHTML = `<div class="empty-state">No repair teams registered.</div>`;
    return;
  }

  container.innerHTML = teams.map(t => {
    const isAvail = t.status === 'AVAILABLE';
    return `
      <div class="team-card">
        <div class="team-card-header">
          <span class="team-name">👷 ${t.teamName}</span>
          <span class="badge ${isAvail ? 'badge-success' : 'badge-warning'}">${t.status}</span>
        </div>
        <div class="team-detail"><strong>Team ID:</strong> <code>${t.teamId}</code></div>
        <div class="team-detail"><strong>Contact:</strong> ${t.contactNumber || 'On Radio Dispatch'}</div>
        <div class="team-detail"><strong>Current Job:</strong> ${t.currentAssignmentId ? `<code>${t.currentAssignmentId}</code>` : '<span class="text-muted">Standing by</span>'}</div>
      </div>
    `;
  }).join('');
}

// ====================================================================
// 9. Approved Backup Connections
// ====================================================================

async function loadBackups() {
  try {
    const res = await fetch(`${API_BASE}/backups/routes`);
    const json = await res.json();
    if (!json.success) return;

    state.backups = json.data;
    renderBackupsTable(state.backups);
  } catch (err) {
    console.error('Failed to load backup routes:', err);
  }
}

function renderBackupsTable(backups) {
  const tbody = document.getElementById('backupsTableBody');
  if (!tbody) return;

  if (!backups || backups.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No approved backup connections configured.</td></tr>`;
    return;
  }

  tbody.innerHTML = backups.map(b => {
    let evalBadge = 'badge-secondary';
    let evalText = 'STANDBY';

    if (b.canRestoreAffectedLoad) {
      evalBadge = 'badge-success';
      evalText = 'READY FOR DISPATCH';
    } else if (!b.alternatePathAvailable) {
      evalBadge = 'badge-danger';
      evalText = 'BLOCKED / UNAVAILABLE';
    }

    return `
      <tr>
        <td><code>${b.backupId}</code></td>
        <td><strong>${b.name}</strong></td>
        <td><code>${b.primaryFeederId}</code></td>
        <td><span class="badge ${b.primaryFeederStatus === 'WORKING' ? 'badge-success' : 'badge-danger'}">${b.primaryFeederStatus}</span></td>
        <td><code>${b.alternateFeederId}</code></td>
        <td><span class="badge ${b.alternateFeederStatus === 'WORKING' ? 'badge-success' : 'badge-danger'}">${b.alternateFeederStatus}</span></td>
        <td>
          <span class="badge ${evalBadge}">${evalText}</span><br>
          <small class="text-muted">${b.pathAnalysis}</small>
        </td>
        <td>
          <button class="btn btn-outline btn-sm text-danger" onclick="deleteBackup('${b.backupId}')" title="Delete backup rule">
            🗑️
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

async function deleteBackup(backupId) {
  if (!confirm(`Delete approved backup route configuration ${backupId}?`)) return;

  try {
    const res = await fetch(`${API_BASE}/backups/${backupId}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast(`Backup ${backupId} deleted.`, 'success');
      loadBackups();
    } else {
      showToast(json.message, 'error');
    }
  } catch (err) {
    showToast('Failed to delete backup configuration.', 'error');
  }
}

// ====================================================================
// 10. Event History & Audit Log
// ====================================================================

async function loadHistory() {
  try {
    const res = await fetch(`${API_BASE}/history?limit=50`);
    const json = await res.json();
    if (!json.success) return;

    state.history = json.data;
    renderHistoryTable(state.history);
  } catch (err) {
    console.error('Failed to load history:', err);
  }
}

function renderHistoryTable(history) {
  const tbody = document.getElementById('historyTableBody');
  if (!tbody) return;

  if (!history || history.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No audit logs available.</td></tr>`;
    return;
  }

  tbody.innerHTML = history.map(h => {
    let typeBadge = 'badge-secondary';
    if (h.eventType.includes('FAULT')) typeBadge = 'badge-danger';
    if (h.eventType.includes('RESTORE')) typeBadge = 'badge-success';
    if (h.eventType.includes('TEAM')) typeBadge = 'badge-warning';

    return `
      <tr>
        <td><code>#${h.historyId}</code></td>
        <td>${formatDate(h.timestamp)}</td>
        <td><span class="badge ${typeBadge}">${h.eventType}</span></td>
        <td><code>${h.targetId}</code></td>
        <td>${h.details}</td>
        <td>${h.performedBy}</td>
      </tr>
    `;
  }).join('');
}

// ====================================================================
// Modals & Form Handlers
// ====================================================================

function setupModals() {
  document.querySelectorAll('[data-close]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modalId = btn.getAttribute('data-close');
      closeModal(modalId);
    });
  });

  const openFaultBtn = document.getElementById('openReportFaultModalBtn');
  if (openFaultBtn) openFaultBtn.addEventListener('click', () => openModal('reportFaultModal'));

  const openFaultBtn2 = document.getElementById('openAddFaultBtn2');
  if (openFaultBtn2) openFaultBtn2.addEventListener('click', () => openModal('reportFaultModal'));

  const openAddNodeBtn = document.getElementById('openAddNodeModalBtn');
  if (openAddNodeBtn) openAddNodeBtn.addEventListener('click', () => openModal('addNodeModal'));

  const openAddFeederBtn = document.getElementById('openAddFeederModalBtn');
  if (openAddFeederBtn) openAddFeederBtn.addEventListener('click', () => openModal('addFeederModal'));

  const openAddBackupBtn = document.getElementById('openAddBackupModalBtn');
  if (openAddBackupBtn) openAddBackupBtn.addEventListener('click', () => openModal('addBackupModal'));

  // Conditional Node modal inputs
  const nodeTypeSelect = document.getElementById('nodeTypeSelect');
  if (nodeTypeSelect) {
    nodeTypeSelect.addEventListener('change', (e) => {
      const isSub = e.target.value === 'SUBSTATION';
      document.getElementById('substationFields').classList.toggle('hidden', !isSub);
      document.getElementById('consumerAreaFields').classList.toggle('hidden', isSub);
    });
  }

  const consumerTypeSelect = document.getElementById('consumerTypeSelect');
  if (consumerTypeSelect) {
    consumerTypeSelect.addEventListener('change', (e) => {
      const isCrit = e.target.value === 'CRITICAL';
      document.getElementById('criticalFacilityGroup').classList.toggle('hidden', !isCrit);
    });
  }
}

function openModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.add('active');
}

function closeModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.remove('active');
}

function promptReportFault(feederId) {
  openModal('reportFaultModal');
  const sel = document.getElementById('faultFeederSelect');
  if (sel) sel.value = feederId;
}

function openAssignTeamModal(jobId, feederId, rank = 1, crit = 0, cons = 0) {
  document.getElementById('assignJobId').value = jobId;

  const summary = document.getElementById('assignJobSummary');
  summary.innerHTML = `
    <strong>Job:</strong> <code>${jobId}</code> (Rank #${rank})<br>
    <strong>Target Feeder:</strong> <code>${feederId}</code><br>
    <strong>Impact:</strong> ${crit} critical facilities, ${cons.toLocaleString()} consumers
  `;

  // Populate available teams
  const sel = document.getElementById('assignTeamSelect');
  sel.innerHTML = '<option value="">Select an available crew...</option>';
  const availTeams = state.teams.filter(t => t.status === 'AVAILABLE');
  if (availTeams.length === 0) {
    sel.innerHTML = '<option value="" disabled>No crews currently available (all BUSY or OFF_DUTY)</option>';
  } else {
    availTeams.forEach(t => {
      sel.innerHTML += `<option value="${t.teamId}">👷 ${t.teamName} (${t.teamId})</option>`;
    });
  }

  openModal('assignTeamModal');
}

function setupFormHandlers() {
  // 1. Report Fault Form
  const faultForm = document.getElementById('reportFaultForm');
  if (faultForm) {
    faultForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const feederId = document.getElementById('faultFeederSelect').value;
      const cause = document.getElementById('faultCauseInput').value;
      const severity = document.getElementById('faultSeveritySelect').value;
      const notes = document.getElementById('faultNotesInput').value;

      try {
        const res = await fetch(`${API_BASE}/faults`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            feederId,
            faultCause: cause,
            severity,
            notes
          })
        });

        const json = await res.json();
        if (json.success) {
          showToast(`Fault reported on feeder ${feederId}. BFS & PriorityQueue updated.`, 'success');
          closeModal('reportFaultModal');
          faultForm.reset();
          refreshAllData();
          switchTab('outages');
        } else {
          showToast(json.message, 'error');
        }
      } catch (err) {
        showToast('Error filing fault report.', 'error');
      }
    });
  }

  // 2. Assign Team Form
  const assignForm = document.getElementById('assignTeamForm');
  if (assignForm) {
    assignForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const jobId = document.getElementById('assignJobId').value;
      const teamId = document.getElementById('assignTeamSelect').value;

      try {
        const res = await fetch(`${API_BASE}/restoration/assign`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ identifier: jobId, teamId })
        });

        const json = await res.json();
        if (json.success) {
          showToast(`Team ${teamId} assigned to job ${jobId}.`, 'success');
          closeModal('assignTeamModal');
          refreshAllData();
        } else {
          showToast(json.message, 'error');
        }
      } catch (err) {
        showToast('Error assigning team.', 'error');
      }
    });
  }

  // 3. Add Node Form
  const nodeForm = document.getElementById('addNodeForm');
  if (nodeForm) {
    nodeForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const nodeId = document.getElementById('nodeIdInput').value.trim();
      const name = document.getElementById('nodeNameInput').value.trim();
      const nodeType = document.getElementById('nodeTypeSelect').value;
      const locX = parseFloat(document.getElementById('nodeLocX').value);
      const locY = parseFloat(document.getElementById('nodeLocY').value);

      const payload = {
        nodeId,
        name,
        nodeType,
        locationX: locX,
        locationY: locY,
        status: 'ACTIVE'
      };

      if (nodeType === 'SUBSTATION') {
        payload.capacityKva = parseFloat(document.getElementById('substationCapacityInput').value);
        payload.isSource = document.getElementById('substationIsSourceSelect').value === 'true';
      } else {
        payload.consumerType = document.getElementById('consumerTypeSelect').value;
        payload.consumerCount = parseInt(document.getElementById('consumerCountInput').value, 10);
        payload.criticalFacilityName = document.getElementById('criticalFacilityNameInput').value.trim();
      }

      try {
        const res = await fetch(`${API_BASE}/nodes`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        const json = await res.json();
        if (json.success) {
          showToast(`Node ${nodeId} created successfully.`, 'success');
          closeModal('addNodeModal');
          nodeForm.reset();
          refreshAllData();
        } else {
          showToast(json.message, 'error');
        }
      } catch (err) {
        showToast('Failed to create network node.', 'error');
      }
    });
  }

  // 4. Add Feeder Form
  const feederForm = document.getElementById('addFeederForm');
  if (feederForm) {
    feederForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const feederId = document.getElementById('feederIdInput').value.trim();
      const name = document.getElementById('feederNameInput').value.trim();
      const src = document.getElementById('feederSourceSelect').value;
      const tgt = document.getElementById('feederTargetSelect').value;
      const cap = parseFloat(document.getElementById('feederCapacityInput').value);
      const len = parseFloat(document.getElementById('feederLengthInput').value);
      const isBackup = document.getElementById('feederIsBackupSelect').value === 'true';

      if (src === tgt) {
        showToast('Self-connection invalid: Source and Target node cannot be identical.', 'error');
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/feeders`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            feederId,
            name,
            sourceNodeId: src,
            targetNodeId: tgt,
            capacityAmps: cap,
            lengthKm: len,
            isBackup,
            status: 'WORKING'
          })
        });

        const json = await res.json();
        if (json.success) {
          showToast(`Feeder line ${feederId} added.`, 'success');
          closeModal('addFeederModal');
          feederForm.reset();
          refreshAllData();
        } else {
          showToast(json.message, 'error');
        }
      } catch (err) {
        showToast('Failed to create feeder line.', 'error');
      }
    });
  }

  // 5. Add Backup Form
  const backupForm = document.getElementById('addBackupForm');
  if (backupForm) {
    backupForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const backupId = document.getElementById('backupIdInput').value.trim();
      const name = document.getElementById('backupNameInput').value.trim();
      const pri = document.getElementById('backupPrimarySelect').value;
      const alt = document.getElementById('backupAlternateSelect').value;
      const desc = document.getElementById('backupDescInput').value.trim();

      if (pri === alt) {
        showToast('Primary and Alternate feeders cannot be identical.', 'error');
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/backups`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            backupId,
            name,
            primaryFeederId: pri,
            alternateFeederId: alt,
            description: desc,
            isApproved: true
          })
        });

        const json = await res.json();
        if (json.success) {
          showToast(`Approved backup route ${backupId} registered.`, 'success');
          closeModal('addBackupModal');
          backupForm.reset();
          loadBackups();
        } else {
          showToast(json.message, 'error');
        }
      } catch (err) {
        showToast('Failed to create backup configuration.', 'error');
      }
    });
  }
}

function setupFilterHandlers() {
  const nodeSearch = document.getElementById('nodeSearchInput');
  const nodeType = document.getElementById('nodeTypeFilter');
  const nodeStatus = document.getElementById('nodeStatusFilter');

  const filterNodes = () => {
    let filtered = [...state.nodes];
    const s = nodeSearch ? nodeSearch.value.toLowerCase().trim() : '';
    const t = nodeType ? nodeType.value : '';
    const st = nodeStatus ? nodeStatus.value : '';

    if (s) filtered = filtered.filter(n => n.nodeId.toLowerCase().includes(s) || n.name.toLowerCase().includes(s));
    if (t) filtered = filtered.filter(n => n.nodeType === t);
    if (st) filtered = filtered.filter(n => n.status === st);
    renderNodesTable(filtered);
  };

  if (nodeSearch) nodeSearch.addEventListener('input', filterNodes);
  if (nodeType) nodeType.addEventListener('change', filterNodes);
  if (nodeStatus) nodeStatus.addEventListener('change', filterNodes);

  const feederSearch = document.getElementById('feederSearchInput');
  const feederStatus = document.getElementById('feederStatusFilter');
  const feederBackup = document.getElementById('feederBackupFilter');

  const filterFeeders = () => {
    let filtered = [...state.feeders];
    const s = feederSearch ? feederSearch.value.toLowerCase().trim() : '';
    const st = feederStatus ? feederStatus.value : '';
    const bk = feederBackup ? feederBackup.value : '';

    if (s) filtered = filtered.filter(f => f.feederId.toLowerCase().includes(s) || f.name.toLowerCase().includes(s) || f.sourceNodeId.toLowerCase().includes(s) || f.targetNodeId.toLowerCase().includes(s));
    if (st) filtered = filtered.filter(f => f.status === st);
    if (bk !== '') filtered = filtered.filter(f => String(f.backup) === bk);
    renderFeedersTable(filtered);
  };

  if (feederSearch) feederSearch.addEventListener('input', filterFeeders);
  if (feederStatus) feederStatus.addEventListener('change', filterFeeders);
  if (feederBackup) feederBackup.addEventListener('change', filterFeeders);
}

function populateNodeSelects(nodes) {
  const srcSel = document.getElementById('feederSourceSelect');
  const tgtSel = document.getElementById('feederTargetSelect');
  if (!srcSel || !tgtSel) return;

  const opts = nodes.map(n => `<option value="${n.nodeId}">${n.nodeId} — ${n.name} (${n.nodeType})</option>`).join('');
  srcSel.innerHTML = '<option value="">Select source...</option>' + opts;
  tgtSel.innerHTML = '<option value="">Select target...</option>' + opts;
}

function populateFeederSelects(feeders) {
  const faultSel = document.getElementById('faultFeederSelect');
  const priSel = document.getElementById('backupPrimarySelect');
  const altSel = document.getElementById('backupAlternateSelect');

  if (faultSel) {
    const working = feeders.filter(f => f.status === 'WORKING');
    faultSel.innerHTML = '<option value="">Select a working feeder line...</option>' +
      working.map(f => `<option value="${f.feederId}">${f.feederId} — ${f.name} (${f.sourceNodeId} <-> ${f.targetNodeId})</option>`).join('');
  }

  const allOpts = feeders.map(f => `<option value="${f.feederId}">${f.feederId} — ${f.name}</option>`).join('');
  if (priSel) priSel.innerHTML = '<option value="">Select primary feeder...</option>' + allOpts;
  if (altSel) altSel.innerHTML = '<option value="">Select alternate feeder...</option>' + allOpts;
}

// ====================================================================
// Utilities
// ====================================================================

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span>${message}</span><button style="background:none;border:none;color:#fff;cursor:pointer;">&times;</button>`;

  toast.querySelector('button').addEventListener('click', () => toast.remove());
  container.appendChild(toast);

  setTimeout(() => {
    if (toast.parentElement) toast.remove();
  }, 4000);
}

function formatDate(isoStr) {
  if (!isoStr) return '—';
  try {
    const d = new Date(isoStr);
    return d.toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' });
  } catch (e) {
    return isoStr;
  }
}
