<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { Code2, Gauge, Timer, TriangleAlert, Clock, ScrollText, BookOpen } from 'lucide-vue-next'
import { api, ENDPOINTS } from '../api/client'
import ServiceMap from '../components/ServiceMap.vue'

// Each microservice has its own request_logs collection and its own
// /api/dev/metrics. There is no shared log store, so this view asks every
// service separately and shows each one in its own panel.
const SERVICES = [
  { name: 'User Service', port: 8085, logs: api.getUserServiceLogs, metrics: api.getUserServiceMetrics },
  { name: 'Product Service', port: 8083, logs: api.getProductServiceLogs, metrics: api.getProductServiceMetrics },
  { name: 'Cart Service', port: 8084, logs: api.getCartServiceLogs, metrics: api.getCartServiceMetrics },
  { name: 'Order Service', port: 8086, logs: api.getOrderServiceLogs, metrics: api.getOrderServiceMetrics },
]

const views = ref(SERVICES.map((s) => ({ ...s, logRows: [], stats: null })))
let timer = null

async function refresh() {
  const next = await Promise.all(SERVICES.map(async (s) => ({
    logRows: await s.logs().catch(() => []),
    stats: await s.metrics().catch(() => null),
  })))
  views.value = SERVICES.map((s, i) => ({ ...s, ...next[i] }))
}

function statusClass(status) {
  if (status >= 500) return 'status-5xx'
  if (status >= 400) return 'status-4xx'
  return 'status-2xx'
}

onMounted(() => {
  refresh()
  timer = setInterval(refresh, 3000)
})

onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="dev">
    <div class="dev-head">
      <h1><Code2 :size="22" /> Developer Console</h1>
      <span class="subtitle">Four independent Spring Boot services, each with its own MongoDB and its own request log</span>
    </div>

    <ServiceMap />

    <div class="service-grid">
      <section v-for="v in views" :key="v.name" class="service-col">
        <h2>{{ v.name }} <span class="port">:{{ v.port }}</span></h2>
        <div v-if="v.stats" class="metrics">
          <div class="metric-card"><Gauge :size="16" class="metric-icon" /><div class="value">{{ v.stats.totalRequests }}</div><div class="label">Requests</div></div>
          <div class="metric-card"><Timer :size="16" class="metric-icon" /><div class="value">{{ v.stats.avgLatencyMs.toFixed(1) }} ms</div><div class="label">Avg latency</div></div>
          <div class="metric-card" :class="{ warn: v.stats.errorRatePct > 0 }"><TriangleAlert :size="16" class="metric-icon" /><div class="value">{{ v.stats.errorRatePct.toFixed(1) }}%</div><div class="label">Errors</div></div>
          <div class="metric-card"><Clock :size="16" class="metric-icon" /><div class="value">{{ v.stats.uptimeSeconds }}s</div><div class="label">Uptime</div></div>
        </div>
        <p v-else class="empty-row">Service is not reachable.</p>

        <div class="table-scroll">
          <table>
            <thead><tr><th>Time</th><th>Method</th><th>Path</th><th>Status</th><th>Duration</th></tr></thead>
            <tbody>
              <tr v-for="log in v.logRows.slice(0, 12)" :key="log.id">
                <td>{{ new Date(log.timestamp).toLocaleTimeString() }}</td>
                <td><span class="method">{{ log.method }}</span></td>
                <td><code>{{ log.path }}</code></td>
                <td><span class="status" :class="statusClass(log.status)">{{ log.status }}</span></td>
                <td class="num">{{ log.durationMs }} ms</td>
              </tr>
              <tr v-if="v.logRows.length === 0"><td colspan="5" class="empty-row">No requests logged yet.</td></tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <section class="panel">
      <h3><BookOpen :size="18" /> API reference (all services)</h3>
      <div class="table-scroll">
        <table>
          <thead><tr><th>Service</th><th>Method</th><th>Path</th><th>Description</th></tr></thead>
          <tbody>
            <tr v-for="e in ENDPOINTS" :key="e.service + e.method + e.path">
              <td>{{ e.service }}</td>
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
.dev-head { margin-bottom: 18px; }
.dev-head h1 { display: flex; align-items: center; gap: 9px; margin: 0 0 4px; font-size: 1.6rem; }
.subtitle { color: var(--ink-soft); font-size: 0.9rem; }

.service-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin: 20px 0; }
.service-col, .panel { background: var(--paper); border-radius: var(--radius); box-shadow: var(--shadow-card); padding: 14px; }
.service-col h2, .panel h3 { display: flex; align-items: center; gap: 8px; margin: 0 0 10px; font-size: 1.05rem; }
.port { font-family: ui-monospace, monospace; font-size: 0.8rem; color: var(--ink-soft); }

.metrics { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; margin-bottom: 12px; }
.metric-card { background: #fff; border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 8px; }
.metric-card.warn { border-color: var(--danger); }
.metric-icon { color: var(--olive); }
.value { font-weight: 700; font-size: 1.05rem; }
.label { font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.06em; color: var(--ink-soft); }

.panel { margin-bottom: 16px; }
.table-scroll { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
th, td { padding: 7px 6px; border-bottom: 1px solid var(--border); text-align: left; }
th { font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.06em; color: var(--ink-soft); }
.num { text-align: right; white-space: nowrap; }
code { font-size: 0.8rem; }
.method { font-weight: 700; color: var(--olive); }
.status { font-weight: 700; }
.status-2xx { color: var(--success); }
.status-4xx { color: var(--accent-dark); }
.status-5xx { color: var(--danger); }
.empty-row { text-align: center; color: var(--ink-soft); }

@media (max-width: 900px) { .service-grid { grid-template-columns: 1fr; } }
</style>
