<script setup>
import { onMounted, onUnmounted, reactive } from 'vue'
import { api } from '../api/client'

// Nodes are the services. Each status dot is a real reachability check:
// any HTTP reply (even 401) means the service is running.
const nodes = [
  { id: 'browser', label: 'Browser', x: 90, y: 150, url: null },
  { id: 'user', label: 'User :8085', x: 330, y: 60, url: api.services.USER_API },
  { id: 'product', label: 'Product :8083', x: 580, y: 60, url: api.services.PRODUCT_API },
  { id: 'cart', label: 'Cart :8084', x: 330, y: 240, url: api.services.CART_API },
  { id: 'order', label: 'Order :8086', x: 580, y: 240, url: api.services.ORDER_API },
]

// Who calls whom. Checkout runs Order -> Cart -> Product, and every call checks the User service.
const edges = [
  ['browser', 'user'], ['browser', 'product'], ['browser', 'cart'], ['browser', 'order'],
  ['cart', 'user'], ['cart', 'product'], ['order', 'cart'], ['order', 'product'], ['order', 'user'],
]

const status = reactive({})
let timer = null

const at = (id) => nodes.find((n) => n.id === id)

async function check() {
  await Promise.all(nodes.filter((n) => n.url).map(async (n) => {
    status[n.id] = await api.ping(n.url)
  }))
}

onMounted(() => {
  check()
  timer = setInterval(check, 5000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <section class="map-card">
    <header>
      <h2>Live service map</h2>
      <span class="hint">Reachability checked every 5 seconds</span>
    </header>

    <svg viewBox="0 0 680 320" role="img" aria-label="Service topology">
      <line
        v-for="([from, to], i) in edges"
        :key="i"
        :x1="at(from).x" :y1="at(from).y" :x2="at(to).x" :y2="at(to).y"
        class="edge"
        :style="{ animationDelay: `${i * 0.35}s` }"
      />
      <g v-for="n in nodes" :key="n.id" :transform="`translate(${n.x - 75}, ${n.y - 30})`">
        <rect width="150" height="60" rx="14" class="node" />
        <text x="22" y="36" class="node-label">{{ n.label }}</text>
        <circle v-if="n.url" cx="130" cy="16" r="6" :class="status[n.id] ? 'up' : 'down'" />
      </g>
    </svg>
  </section>
</template>

<style scoped>
.map-card { background: var(--paper); border-radius: 16px; padding: 20px 22px; box-shadow: var(--shadow-card); margin-bottom: 22px; }
header { display: flex; justify-content: space-between; align-items: baseline; gap: 12px; margin-bottom: 8px; }
h2 { margin: 0; font-size: 1.25rem; }
.hint { color: var(--ink-soft); font-size: 0.84rem; }
svg { width: 100%; height: auto; }

.edge { stroke: var(--border); stroke-width: 2; stroke-dasharray: 6 8; animation: flow 1.6s linear infinite; }
@keyframes flow { to { stroke-dashoffset: -28; } }

.node { fill: #fff; stroke: var(--border); stroke-width: 1.5; }
.node-label { font-family: 'Instrument Sans', sans-serif; font-weight: 600; font-size: 14px; fill: var(--ink); }
.up { fill: var(--olive); }
.down { fill: var(--danger); }
</style>
