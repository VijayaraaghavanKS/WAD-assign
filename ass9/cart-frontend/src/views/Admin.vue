<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { PackagePlus, Pencil, Trash2, Check, X, LayoutGrid, Boxes, TrendingUp, ShoppingBag, Wallet, TriangleAlert, ChevronDown } from 'lucide-vue-next'
import { api } from '../api/client'
import { getProductImage } from '../utils/productImage'

const STATUSES = ['PLACED', 'PACKED', 'SHIPPED', 'DELIVERED']
const LOW_STOCK = 5

const products = ref([])
const orders = ref([])
const coupons = ref([])
const newCoupon = reactive({ code: '', percent: null })
const stats = ref(null)
const error = ref('')
const newProduct = reactive({ name: '', price: null, quantity: null, category: '', discountPercent: 0 })
const editingId = ref(null)
const editDraft = reactive({ name: '', price: null, quantity: null, category: '', discountPercent: 0 })
const openId = ref(null)

const money = (n) => `₹${Math.round(n || 0).toLocaleString()}`
const toggleOpen = (id) => (openId.value = openId.value === id ? null : id)

// Per-product performance, worked out from every order. Units and revenue
// come from the order lines, so a product's numbers follow its real sales.
const performance = computed(() => {
  const byId = new Map()
  for (const o of orders.value) {
    for (const line of o.items) {
      const row = byId.get(line.productId) ?? { id: line.productId, name: line.productName, units: 0, revenue: 0, orderIds: new Set() }
      row.units += line.quantity
      row.revenue += line.price * line.quantity
      row.orderIds.add(o.id)
      byId.set(line.productId, row)
    }
  }
  const rows = products.value.map((p) => {
    const s = byId.get(p.id)
    return { ...p, units: s?.units ?? 0, revenue: s?.revenue ?? 0, orders: s?.orderIds.size ?? 0 }
  })
  return rows.sort((a, b) => b.revenue - a.revenue)
})
const totalRevenue = computed(() => performance.value.reduce((n, r) => n + r.revenue, 0))
const share = (r) => Math.round((r.revenue / Math.max(1, totalRevenue.value)) * 100)
const lowStock = computed(() => products.value.filter((p) => p.quantity <= LOW_STOCK).sort((a, b) => a.quantity - b.quantity))

// Bars for the last 14 days, scaled to the busiest day.
const days = computed(() => stats.value?.lastFourteenDays ?? [])
const dayMax = computed(() => Math.max(1, ...days.value.map((d) => d.revenue)))
const chartBars = computed(() =>
  days.value.map((d, i) => {
    const h = (d.revenue / dayMax.value) * 160
    return { x: i * 40 + 6, y: 180 - h, h, label: d.day.slice(5), revenue: d.revenue, orders: d.orders }
  })
)

async function refresh() {
  products.value = await api.getProducts()
  coupons.value = await api.getCoupons().catch(() => [])
  try {
    ;[orders.value, stats.value] = await Promise.all([api.getAllOrders(), api.getOrderStats()])
  } catch (e) {
    error.value = e.message
  }
}

async function setStatus(order, status) {
  try {
    await api.setOrderStatus(order.id, status)
    await refresh()
  } catch (e) {
    error.value = e.message
  }
}

async function addCoupon() {
  if (!newCoupon.code || !newCoupon.percent) return
  try {
    await api.addCoupon(newCoupon.code, Number(newCoupon.percent))
    Object.assign(newCoupon, { code: '', percent: null })
    coupons.value = await api.getCoupons()
  } catch (e) {
    error.value = e.message
  }
}

async function removeCoupon(code) {
  await api.deleteCoupon(code)
  coupons.value = await api.getCoupons()
}

async function addProduct() {
  if (!newProduct.name || newProduct.price == null) return
  await api.createProduct({
    name: newProduct.name,
    price: Number(newProduct.price),
    quantity: Number(newProduct.quantity || 0),
    category: newProduct.category || 'More',
    discountPercent: Number(newProduct.discountPercent || 0),
  })
  Object.assign(newProduct, { name: '', price: null, quantity: null, category: '', discountPercent: 0 })
  await refresh()
}

// Category and sale stay as they were; only name, price and stock are edited inline.
function startEdit(p) {
  editingId.value = p.id
  Object.assign(editDraft, { name: p.name, price: p.price, quantity: p.quantity, category: p.category, discountPercent: p.discountPercent })
}

async function saveEdit(id) {
  await api.updateProduct(id, {
    name: editDraft.name,
    price: Number(editDraft.price),
    quantity: Number(editDraft.quantity),
    category: editDraft.category,
    discountPercent: editDraft.discountPercent,
  })
  editingId.value = null
  await refresh()
}

async function remove(id) {
  await api.deleteProduct(id)
  await refresh()
}

function stockClass(qty) {
  if (qty === undefined || qty === null) return ''
  if (qty === 0) return 'pill-danger'
  if (qty <= LOW_STOCK) return 'pill-warn'
  return 'pill-success'
}

function stockLabel(qty) {
  if (qty === undefined || qty === null) return '—'
  if (qty === 0) return 'Out of stock'
  return `${qty} in stock`
}

onMounted(refresh)
</script>

<template>
  <div class="admin">
    <div class="admin-head">
      <h1><LayoutGrid :size="22" /> Seller dashboard</h1>
      <span class="subtitle">Sales, orders and inventory in one place</span>
    </div>
    <p v-if="error" class="error">{{ error }}</p>

    <div class="kpis">
      <div class="kpi"><Wallet :size="18" /><div><div class="kpi-value">{{ money(stats?.revenue) }}</div><div class="kpi-label">Revenue (all time)</div></div></div>
      <div class="kpi"><ShoppingBag :size="18" /><div><div class="kpi-value">{{ stats?.orderCount ?? 0 }}</div><div class="kpi-label">Orders</div></div></div>
      <div class="kpi"><TrendingUp :size="18" /><div><div class="kpi-value">{{ money(stats?.averageOrderValue) }}</div><div class="kpi-label">Average order</div></div></div>
      <div class="kpi" :class="{ warn: lowStock.length }"><TriangleAlert :size="18" /><div><div class="kpi-value">{{ lowStock.length }}</div><div class="kpi-label">Low-stock products</div></div></div>
    </div>

    <div class="panels">
      <section class="panel wide-panel">
        <h2>Revenue, last 14 days</h2>
        <svg viewBox="0 0 560 210" class="chart" role="img" aria-label="Revenue per day for the last 14 days">
          <line x1="0" y1="180" x2="560" y2="180" class="axis" />
          <g v-for="b in chartBars" :key="b.label">
            <rect :x="b.x" :y="b.y" width="28" :height="b.h" rx="4" class="bar"><title>{{ b.label }}: {{ money(b.revenue) }} from {{ b.orders }} orders</title></rect>
            <text :x="b.x + 14" y="198" class="tick">{{ b.label }}</text>
          </g>
        </svg>
      </section>

      <section class="panel">
        <h2>Order status</h2>
        <div v-for="s in STATUSES" :key="s" class="status-row">
          <span>{{ s.toLowerCase() }}</span>
          <div class="status-bar"><span :style="{ width: ((stats?.byStatus?.[s] ?? 0) / Math.max(1, stats?.orderCount ?? 0)) * 100 + '%' }"></span></div>
          <strong>{{ stats?.byStatus?.[s] ?? 0 }}</strong>
        </div>
      </section>
    </div>

    <section class="panel">
      <h2>Orders</h2>
      <div class="table-scroll">
        <table>
          <thead><tr><th>Order</th><th>Customer</th><th>Placed</th><th>Items</th><th class="num">Paid</th><th>Status</th></tr></thead>
          <tbody>
            <tr v-for="o in orders.slice(0, 40)" :key="o.id">
              <td><code>{{ o.id.slice(-6).toUpperCase() }}</code></td>
              <td>{{ o.username || '—' }}</td>
              <td>{{ new Date(o.placedAt).toLocaleDateString() }}</td>
              <td>{{ o.items.reduce((n, l) => n + l.quantity, 0) }}</td>
              <td class="num">{{ money(o.total) }}<span v-if="o.couponCode" class="coupon-tag"> {{ o.couponCode }}</span></td>
              <td>
                <select :value="o.status || 'PLACED'" @change="setStatus(o, $event.target.value)">
                  <option v-for="s in STATUSES" :key="s" :value="s">{{ s.toLowerCase() }}</option>
                </select>
              </td>
            </tr>
            <tr v-if="orders.length === 0"><td colspan="6" class="empty-row">No orders yet.</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel">
      <h2><TriangleAlert :size="17" /> Low stock</h2>
      <p v-if="lowStock.length === 0" class="empty-row">Everything is well stocked.</p>
      <ul v-else class="low-list">
        <li v-for="p in lowStock" :key="p.id"><span>{{ p.name }}</span><span class="pill" :class="stockClass(p.quantity)">{{ stockLabel(p.quantity) }}</span></li>
      </ul>
    </section>

    <section class="panel">
      <h2>Coupon codes</h2>
      <form class="coupon-form" @submit.prevent="addCoupon">
        <input v-model="newCoupon.code" placeholder="Code, e.g. DIWALI15" aria-label="Coupon code" />
        <input v-model.number="newCoupon.percent" type="number" min="1" max="90" placeholder="% off" aria-label="Percent off" />
        <button class="btn btn-primary" type="submit">Add code</button>
      </form>
      <ul class="low-list">
        <li v-for="c in coupons" :key="c.code">
          <span><code>{{ c.code }}</code> &middot; {{ c.percent }}% off</span>
          <button class="btn btn-outline" :aria-label="`Remove ${c.code}`" @click="removeCoupon(c.code)"><Trash2 :size="15" /></button>
        </li>
      </ul>
    </section>

    <section class="panel">
      <h2><Boxes :size="17" /> Products, stock and performance</h2>
      <form class="add-form" @submit.prevent="addProduct">
        <input v-model="newProduct.name" placeholder="Product name" required />
        <input v-model="newProduct.price" type="number" min="0" placeholder="Price (₹)" required />
        <input v-model="newProduct.quantity" type="number" min="0" placeholder="Stock quantity" />
        <input v-model="newProduct.category" placeholder="Category (e.g. Audio)" />
        <input v-model="newProduct.discountPercent" type="number" min="0" max="90" placeholder="Sale %" />
        <button type="submit" class="btn btn-primary"><PackagePlus :size="16" /> Add product</button>
      </form>

      <div class="table-scroll">
        <table>
          <thead>
            <tr><th></th><th></th><th>Product</th><th>Category</th><th class="num">Price</th><th>Sale</th><th>Stock</th><th class="num">Units sold</th><th class="num">Revenue</th><th></th></tr>
          </thead>
          <tbody>
            <template v-for="r in performance" :key="r.id">
              <tr>
                <td><button class="chev" :aria-expanded="openId === r.id" aria-label="Show performance" @click="toggleOpen(r.id)"><ChevronDown :size="16" :class="{ turned: openId === r.id }" /></button></td>
                <td><img :src="getProductImage(r.name)" :alt="r.name" class="thumb" /></td>
                <template v-if="editingId === r.id">
                  <td><input v-model="editDraft.name" /></td>
                  <td><input v-model="editDraft.category" /></td>
                  <td><input v-model="editDraft.price" type="number" min="0" /></td>
                  <td><input v-model="editDraft.discountPercent" type="number" min="0" max="90" /></td>
                  <td><input v-model="editDraft.quantity" type="number" min="0" /></td>
                </template>
                <template v-else>
                  <td class="pname">{{ r.name }}</td>
                  <td>{{ r.category || 'More' }}</td>
                  <td class="num">{{ money(r.price) }}</td>
                  <td>{{ r.discountPercent ? r.discountPercent + '%' : '—' }}</td>
                  <td><span class="pill" :class="stockClass(r.quantity)">{{ stockLabel(r.quantity) }}</span></td>
                </template>
                <td class="num">{{ r.units }}</td>
                <td class="num">{{ money(r.revenue) }}</td>
                <td class="actions">
                  <template v-if="editingId === r.id">
                    <button class="btn btn-outline" @click="saveEdit(r.id)"><Check :size="15" /> Save</button>
                    <button class="btn btn-outline" aria-label="Cancel" @click="editingId = null"><X :size="15" /></button>
                  </template>
                  <template v-else>
                    <button class="btn btn-outline" @click="startEdit(r)"><Pencil :size="14" /> Edit</button>
                    <button class="btn btn-danger-outline" @click="remove(r.id)"><Trash2 :size="14" /> Delete</button>
                  </template>
                </td>
              </tr>
              <tr v-if="openId === r.id" class="detail">
                <td colspan="10">
                  <div class="detail-grid">
                    <div><span class="k">Orders</span><strong>{{ r.orders }}</strong></div>
                    <div><span class="k">Units sold</span><strong>{{ r.units }}</strong></div>
                    <div><span class="k">Average per order</span><strong>{{ money(r.orders ? r.revenue / r.orders : 0) }}</strong></div>
                    <div class="share-cell">
                      <span class="k">Share of revenue · {{ share(r) }}%</span>
                      <div class="share"><span :style="{ width: share(r) + '%' }"></span></div>
                    </div>
                  </div>
                </td>
              </tr>
            </template>
            <tr v-if="products.length === 0"><td colspan="10" class="empty-row">No products yet.</td></tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<style scoped>
.coupon-form { display: flex; gap: 8px; margin-bottom: 10px; }
.coupon-form input { flex: 1; min-width: 0; padding: 8px; }
.admin-head { margin-bottom: 16px; }
.admin-head h1 { display: flex; align-items: center; gap: 9px; margin: 0 0 4px; font-size: 1.6rem; }
.subtitle { color: var(--ink-soft); font-size: 0.9rem; }
.error { color: var(--danger); }

.kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 16px; }
.kpi { background: var(--navy-dark); color: var(--paper); border-radius: var(--radius); padding: 16px 18px; display: flex; gap: 12px; align-items: center; }
.kpi.warn { background: var(--danger); }
.kpi-value { font-family: 'Fraunces', Georgia, serif; font-size: 1.6rem; font-weight: 700; line-height: 1.1; }
.kpi-label { font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em; opacity: 0.8; }

.panels { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-bottom: 16px; }
.panel { background: var(--paper); border-radius: var(--radius); box-shadow: var(--shadow-card); padding: 18px; margin-bottom: 16px; }
.panels .panel { margin-bottom: 0; }
.panel h2 { display: flex; align-items: center; gap: 7px; margin: 0 0 12px; font-size: 1.1rem; }

.chart { width: 100%; height: auto; }
.chart .axis { stroke: var(--border); }
.chart .bar { fill: var(--accent); }
.chart .bar:hover { fill: var(--accent-dark); }
.chart .tick { font-size: 11px; fill: var(--ink-soft); text-anchor: middle; }

.status-row { display: grid; grid-template-columns: 80px 1fr 32px; gap: 10px; align-items: center; font-size: 0.88rem; margin-bottom: 10px; text-transform: capitalize; }
.status-bar, .share { height: 8px; background: var(--border); border-radius: 999px; overflow: hidden; }
.status-bar span, .share span { display: block; height: 100%; background: var(--olive); }
.share { min-width: 90px; }

.table-scroll { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
th, td { padding: 9px 8px; border-bottom: 1px solid var(--border); text-align: left; vertical-align: middle; }
th { font-size: 0.74rem; text-transform: uppercase; letter-spacing: 0.06em; color: var(--ink-soft); }
.num { text-align: right; white-space: nowrap; }
.pname { font-weight: 600; }
.thumb { width: 44px; height: 44px; object-fit: cover; border-radius: 8px; }
.actions { white-space: nowrap; }
.actions .btn { padding: 6px 12px; font-size: 0.82rem; margin-right: 4px; }
.coupon-tag { font-size: 0.7rem; color: var(--olive); font-weight: 700; }
.chev { border: 1px solid var(--border); background: #fff; border-radius: 6px; width: 28px; height: 28px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; }
.chev svg { transition: transform 0.2s; }
.chev svg.turned { transform: rotate(180deg); }
.detail td { background: #f7f0e6; }
.detail-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)) 1.6fr; gap: 14px; align-items: end; }
.detail .k { display: block; font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.06em; color: var(--ink-soft); }
.detail-grid strong { font-family: 'Fraunces', Georgia, serif; font-size: 1.15rem; }
.share-cell .share { margin-top: 6px; min-width: 0; }
.empty-row { text-align: center; color: var(--ink-soft); }
select, input { font: inherit; border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 6px 8px; background: #fff; }

.add-form { display: grid; grid-template-columns: 2fr 1fr 1fr 1.2fr 0.8fr auto; gap: 8px; margin-bottom: 14px; }
.low-list { list-style: none; padding: 0; margin: 0; display: grid; gap: 8px; }
.low-list li { display: flex; justify-content: space-between; align-items: center; font-size: 0.92rem; }

.pill { border-radius: 999px; font-size: 0.72rem; font-weight: 700; padding: 3px 10px; display: inline-flex; }
.pill-success { background: var(--success-bg); color: var(--success); }
.pill-danger { background: var(--danger-bg); color: var(--danger); }
.pill-warn { background: #f6e7cf; color: var(--accent-dark); }

@media (max-width: 1000px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
  .panels { grid-template-columns: 1fr; }
  .add-form { grid-template-columns: 1fr 1fr; }
  .detail-grid { grid-template-columns: 1fr 1fr; }
}
</style>
