<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { Code2, Gauge, Timer, TriangleAlert, Clock, ScrollText, BookOpen } from 'lucide-vue-next'
import { api, ENDPOINTS } from '../api/client'

const logs = ref([])
const metrics = ref(null)
let timer = null

async function refresh() {
  logs.value = await api.getLogs()
  metrics.value = await api.getMetrics()
}

function statusClass(status) {
  if (status >= 500) return 'status-5xx'
  if (status >= 400) return 'status-4xx'
  return 'status-2xx'
}

onMounted(() => {
  refresh()
  // Poll every 3s so the log tail feels "live" without needing websockets.
  timer = setInterval(refresh, 3000)
})

onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="dev">
    <div class="dev-head">
      <h1><Code2 :size="22" /> Developer Console</h1>
      <span class="subtitle">Live observability for the ShopCart backend &mdash; port 8082</span>
    </div>

    <div class="metrics" v-if="metrics">
      <div class="metric-card">
        <Gauge :size="18" class="metric-icon" />
        <div class="value">{{ metrics.totalRequests }}</div>
        <div class="label">Total Requests</div>
      </div>
      <div class="metric-card">
        <Timer :size="18" class="metric-icon" />
        <div class="value">{{ metrics.avgLatencyMs.toFixed(1) }} ms</div>
        <div class="label">Avg Latency</div>
      </div>
      <div class="metric-card" :class="{ warn: metrics.errorRatePct > 0 }">
        <TriangleAlert :size="18" class="metric-icon" />
        <div class="value">{{ metrics.errorRatePct.toFixed(1) }}%</div>
        <div class="label">Error Rate ({{ metrics.errorCount }} errors)</div>
      </div>
      <div class="metric-card">
        <Clock :size="18" class="metric-icon" />
        <div class="value">{{ metrics.uptimeSeconds }}s</div>
        <div class="label">Uptime</div>
      </div>
    </div>

    <section class="panel">
      <h3><ScrollText :size="18" /> Live Request Log <span class="live-dot"></span></h3>
      <div class="table-scroll">
        <table>
          <thead>
            <tr><th>Time</th><th>Method</th><th>Path</th><th>Status</th><th>Duration</th></tr>
          </thead>
          <tbody>
            <tr v-for="log in logs" :key="log.id">
              <td>{{ new Date(log.timestamp).toLocaleTimeString() }}</td>
              <td><span class="method">{{ log.method }}</span></td>
              <td><code>{{ log.path }}</code></td>
              <td><span class="status" :class="statusClass(log.status)">{{ log.status }}</span></td>
              <td class="num">{{ log.durationMs }} ms</td>
            </tr>
            <tr v-if="logs.length === 0">
              <td colspan="5" class="empty-row">No requests logged yet.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel">
      <h3><BookOpen :size="18" /> API Reference</h3>
      <div class="table-scroll">
        <table>
          <thead>
            <tr><th>Method</th><th>Path</th><th>Description</th></tr>
          </thead>
          <tbody>
            <tr v-for="e in ENDPOINTS" :key="e.method + e.path">
              <td><span class="method">{{ e.method }}</span></td>
              <td><code>{{ e.path }}</code></td>
              <td>{{ e.desc }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<style scoped>
.dev-head {
  margin-bottom: 18px;
}

.dev-head h1 {
  display: flex;
  align-items: center;
  gap: 9px;
  color: var(--ink);
  margin: 0 0 4px;
  font-size: 1.35rem;
}

.subtitle {
  color: var(--ink-soft);
  font-size: 0.9rem;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}

.metric-card {
  background: var(--navy-dark);
  color: white;
  border-radius: var(--radius);
  padding: 16px;
  box-shadow: var(--shadow-card);
  position: relative;
}

.metric-icon {
  color: var(--accent);
  margin-bottom: 8px;
}

.metric-card.warn {
  background: #5c1a1a;
}

.metric-card.warn .metric-icon {
  color: #fca5a5;
}

.metric-card .value {
  font-size: 1.6rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.metric-card .label {
  font-size: 0.78rem;
  color: #a1a1aa;
  margin-top: 4px;
}

.panel {
  background: var(--navy-dark);
  border-radius: var(--radius);
  padding: 16px 18px 18px;
  margin-bottom: 18px;
  box-shadow: var(--shadow-card);
}

.panel h3 {
  color: #f1f5f9;
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}

th {
  text-align: left;
  padding: 8px 10px;
  color: #8ea0b8;
  font-weight: 600;
  font-size: 0.74rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  border-bottom: 1px solid #2a3947;
  white-space: nowrap;
}

td {
  padding: 8px 10px;
  border-bottom: 1px solid #1e293b;
  color: #cbd5e1;
  white-space: nowrap;
}

.num {
  font-variant-numeric: tabular-nums;
}

tbody tr:hover td {
  background: rgba(255, 255, 255, 0.03);
}

.status {
  padding: 2px 9px;
  border-radius: 100px;
  font-weight: 700;
  font-size: 0.78rem;
}

.status-2xx { background: #14532d; color: #86efac; }
.status-4xx { background: #78350f; color: #fde68a; }
.status-5xx { background: #7f1d1d; color: #fca5a5; }

.method {
  font-weight: 700;
  color: #7dd3fc;
  font-size: 0.78rem;
}

code {
  color: #f1f5f9;
  font-family: 'SF Mono', Menlo, monospace;
  font-size: 0.82rem;
}

.empty-row {
  text-align: center;
  color: #64748b;
  padding: 20px 0;
}
</style>
