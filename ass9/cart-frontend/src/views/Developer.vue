<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { Code2, Gauge, Timer, TriangleAlert, Clock, ScrollText, BookOpen } from 'lucide-vue-next'
import { api, ENDPOINTS } from '../api/client'

// Each microservice has its own request_logs collection and its own
// /api/dev/metrics -- there is no shared log store, so this view fetches
// both independently and tags each row with its origin service. That
// separation is itself the thing worth showing: in a real microservice
// deployment you'd need a log aggregator (e.g. ELK) to merge these; here
// we merge them client-side for a single dashboard.
const productLogs = ref([])
const cartLogs = ref([])
const productMetrics = ref(null)
const cartMetrics = ref(null)
let timer = null

async function refresh() {
  const [pLogs, cLogs, pMetrics, cMetrics] = await Promise.all([
    api.getProductServiceLogs().catch(() => []),
    api.getCartServiceLogs().catch(() => []),
    api.getProductServiceMetrics().catch(() => null),
    api.getCartServiceMetrics().catch(() => null),
  ])
  productLogs.value = pLogs
  cartLogs.value = cLogs
  productMetrics.value = pMetrics
  cartMetrics.value = cMetrics
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
      <span class="subtitle">Two independent Spring Boot services, each with its own MongoDB and its own request log</span>
    </div>

    <div class="service-row">
      <div class="service-col" v-if="productMetrics">
        <h2>Product Service <span class="port">:8083</span></h2>
        <div class="metrics">
          <div class="metric-card">
            <Gauge :size="16" class="metric-icon" />
            <div class="value">{{ productMetrics.totalRequests }}</div>
            <div class="label">Requests</div>
          </div>
          <div class="metric-card">
            <Timer :size="16" class="metric-icon" />
            <div class="value">{{ productMetrics.avgLatencyMs.toFixed(1) }} ms</div>
            <div class="label">Avg Latency</div>
          </div>
          <div class="metric-card" :class="{ warn: productMetrics.errorRatePct > 0 }">
            <TriangleAlert :size="16" class="metric-icon" />
            <div class="value">{{ productMetrics.errorRatePct.toFixed(1) }}%</div>
            <div class="label">Errors</div>
          </div>
          <div class="metric-card">
            <Clock :size="16" class="metric-icon" />
            <div class="value">{{ productMetrics.uptimeSeconds }}s</div>
            <div class="label">Uptime</div>
          </div>
        </div>
      </div>
      <div class="service-col" v-if="cartMetrics">
        <h2>Cart Service <span class="port">:8084</span></h2>
        <div class="metrics">
          <div class="metric-card">
            <Gauge :size="16" class="metric-icon" />
            <div class="value">{{ cartMetrics.totalRequests }}</div>
            <div class="label">Requests</div>
          </div>
          <div class="metric-card">
            <Timer :size="16" class="metric-icon" />
            <div class="value">{{ cartMetrics.avgLatencyMs.toFixed(1) }} ms</div>
            <div class="label">Avg Latency</div>
          </div>
          <div class="metric-card" :class="{ warn: cartMetrics.errorRatePct > 0 }">
            <TriangleAlert :size="16" class="metric-icon" />
            <div class="value">{{ cartMetrics.errorRatePct.toFixed(1) }}%</div>
            <div class="label">Errors</div>
          </div>
          <div class="metric-card">
            <Clock :size="16" class="metric-icon" />
            <div class="value">{{ cartMetrics.uptimeSeconds }}s</div>
            <div class="label">Uptime</div>
          </div>
        </div>
      </div>
    </div>

    <section class="panel">
      <h3><ScrollText :size="18" /> Product Service &ndash; Live Request Log <span class="live-dot"></span></h3>
      <div class="table-scroll">
        <table>
          <thead><tr><th>Time</th><th>Method</th><th>Path</th><th>Status</th><th>Duration</th></tr></thead>
          <tbody>
            <tr v-for="log in productLogs" :key="log.id">
              <td>{{ new Date(log.timestamp).toLocaleTimeString() }}</td>
              <td><span class="method">{{ log.method }}</span></td>
              <td><code>{{ log.path }}</code></td>
              <td><span class="status" :class="statusClass(log.status)">{{ log.status }}</span></td>
              <td class="num">{{ log.durationMs }} ms</td>
            </tr>
            <tr v-if="productLogs.length === 0"><td colspan="5" class="empty-row">No requests logged yet.</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel">
      <h3><ScrollText :size="18" /> Cart Service &ndash; Live Request Log <span class="live-dot"></span></h3>
      <div class="table-scroll">
        <table>
          <thead><tr><th>Time</th><th>Method</th><th>Path</th><th>Status</th><th>Duration</th></tr></thead>
          <tbody>
            <tr v-for="log in cartLogs" :key="log.id">
              <td>{{ new Date(log.timestamp).toLocaleTimeString() }}</td>
              <td><span class="method">{{ log.method }}</span></td>
              <td><code>{{ log.path }}</code></td>
              <td><span class="status" :class="statusClass(log.status)">{{ log.status }}</span></td>
              <td class="num">{{ log.durationMs }} ms</td>
            </tr>
            <tr v-if="cartLogs.length === 0"><td colspan="5" class="empty-row">No requests logged yet.</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel">
      <h3><BookOpen :size="18" /> API Reference (both services)</h3>
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

.service-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.service-col {
  background: var(--paper);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 14px;
}

.service-col h2 {
  margin: 0 0 10px;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.port {
  color: var(--link);
  font-weight: 700;
  font-size: 0.82rem;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.metric-card {
  background: var(--navy-dark);
  color: white;
  border-radius: var(--radius-sm);
  padding: 12px;
  position: relative;
}

.metric-icon {
  color: var(--accent);
  margin-bottom: 6px;
}

.metric-card.warn { background: #5c1a1a; }
.metric-card.warn .metric-icon { color: #fca5a5; }

.metric-card .value {
  font-size: 1.2rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.metric-card .label {
  font-size: 0.74rem;
  color: #a1a1aa;
  margin-top: 2px;
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

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }

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
