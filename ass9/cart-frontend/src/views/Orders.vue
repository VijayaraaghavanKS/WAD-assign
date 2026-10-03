<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Check, ChevronDown, Heart, Package, PackageCheck, Printer, RefreshCw, ShoppingCart, Truck } from 'lucide-vue-next'
import { api } from '../api/client'
import { useCartStore } from '../stores/cart'
import { useShopperStore } from '../stores/shopper'
import { getProductImage } from '../utils/productImage'

// The steps an order moves through. Admins move it along in the Admin page.
const STEPS = [
  { key: 'PLACED', label: 'Order placed', icon: Package },
  { key: 'PACKED', label: 'Packed', icon: Package },
  { key: 'SHIPPED', label: 'On the way', icon: Truck },
  { key: 'DELIVERED', label: 'Delivered', icon: PackageCheck },
]
const PAYMENT_LABEL = { UPI: 'UPI', CARD: 'Credit / debit card', COD: 'Cash on delivery' }
const DELIVERY_DAYS = 5

const cart = useCartStore()
const shopper = useShopperStore()
const orders = ref([])
const error = ref('')
const open = reactive({})
const notice = ref('')

const statusOf = (o) => o.status || 'PLACED'
const stepIndex = (o) => STEPS.findIndex((s) => s.key === statusOf(o))
const reference = (o) => o.id.slice(-6).toUpperCase()
const subtotalOf = (o) => o.subtotal ?? o.items.reduce((s, l) => s + l.price * l.quantity, 0)
const deliveryOf = (o) => o.deliveryFee ?? 0
const taxOf = (o) => o.tax ?? Math.round((o.total * 18) / 118)
const itemCount = (o) => o.items.reduce((n, l) => n + l.quantity, 0)
const total = computed(() => orders.value.length)

// Expected delivery is the order date plus a fixed number of days.
function expectedBy(o) {
  const d = new Date(o.placedAt)
  d.setDate(d.getDate() + DELIVERY_DAYS)
  return d.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric', month: 'short' })
}

function say(text) {
  notice.value = text
  setTimeout(() => (notice.value = ''), 2800)
}

// Adds one product to the cart. Used by Buy again and Reorder.
async function addLine(line) {
  for (let i = 0; i < line.quantity; i++) await cart.add(line.productId)
}

async function buyAgain(o) {
  try {
    for (const line of o.items) await addLine(line)
    say('All items from this order are in your cart')
  } catch (e) {
    say(e.message)
  }
}

function printInvoice() {
  window.print()
}

onMounted(async () => {
  try {
    orders.value = (await api.getOrders()) ?? []
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <section class="orders">
    <div class="orders-head">
      <h1>Your orders</h1>
      <span class="count">{{ total }} {{ total === 1 ? 'order' : 'orders' }}</span>
    </div>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="orders.length === 0" class="empty">No orders yet. Your purchases will show up here.</p>
    <p v-if="notice" class="notice no-print" role="status">{{ notice }}</p>

    <article v-for="o in orders" :key="o.id" class="order">
      <header>
        <div class="summary-cols">
          <div><span class="label">Order placed</span>{{ new Date(o.placedAt).toLocaleDateString() }}</div>
          <div><span class="label">Total</span>₹{{ o.total.toLocaleString() }}</div>
          <div><span class="label">Ship to</span>{{ o.shippingAddress?.name || '—' }}</div>
          <div><span class="label">Order</span>{{ reference(o) }}</div>
        </div>
        <div class="head-right no-print">
          <span class="pill pill-warn">{{ STEPS[stepIndex(o)]?.label }}</span>
          <button class="btn btn-outline details-btn" :aria-expanded="!!open[o.id]" @click="open[o.id] = !open[o.id]">
            {{ open[o.id] ? 'Hide details' : 'View details' }} <ChevronDown :size="15" :class="{ turned: open[o.id] }" />
          </button>
        </div>
      </header>

      <div v-if="open[o.id]" class="details">
        <div class="invoice-top">
          <div>
            <h2>Invoice {{ o.invoiceNumber || 'INV-' + reference(o) }}</h2>
            <p class="muted">Placed {{ new Date(o.placedAt).toLocaleString() }} &middot; {{ itemCount(o) }} items</p>
          </div>
          <div class="actions no-print">
            <button class="btn btn-outline" @click="buyAgain(o)"><RefreshCw :size="15" /> Buy again</button>
            <button class="btn btn-outline" @click="printInvoice"><Printer :size="15" /> Print invoice</button>
          </div>
        </div>

        <div class="tracker">
          <p class="eta">
            <template v-if="statusOf(o) === 'DELIVERED'">Delivered</template>
            <template v-else>Expected by <strong>{{ expectedBy(o) }}</strong></template>
          </p>
          <ol class="steps">
            <li v-for="(step, i) in STEPS" :key="step.key" :class="{ done: i <= stepIndex(o), now: i === stepIndex(o) }">
              <span class="dot"><Check v-if="i < stepIndex(o)" :size="13" /></span>
              <span>{{ step.label }}</span>
            </li>
          </ol>
        </div>

        <div class="addresses">
          <div class="box">
            <h3>Shipping address</h3>
            <template v-if="o.shippingAddress">
              <p><strong>{{ o.shippingAddress.name }}</strong><br />{{ o.shippingAddress.line1 }}<br />
                {{ o.shippingAddress.city }}, {{ o.shippingAddress.state }} {{ o.shippingAddress.pin }}<br />
                Phone: {{ o.shippingAddress.phone || '—' }}</p>
            </template>
            <p v-else class="muted">Address was not recorded for this order.</p>
          </div>
          <div class="box">
            <h3>Billing</h3>
            <p>Billed to the same address.<br />Payment: <strong>{{ PAYMENT_LABEL[o.paymentMethod] ?? 'UPI' }}</strong></p>
            <p class="muted">GST invoice. Tax of ₹{{ taxOf(o).toLocaleString() }} is included in the total.</p>
          </div>
        </div>

        <table class="lines">
          <thead><tr><th>Item</th><th class="num">Price</th><th class="num">Qty</th><th class="num">Amount</th><th class="no-print"></th></tr></thead>
          <tbody>
            <tr v-for="line in o.items" :key="line.productId">
              <td class="item">
                <img :src="getProductImage(line.productName)" :alt="line.productName" />
                <div>
                  <RouterLink :to="`/product/${line.productId}`" class="item-name">{{ line.productName }}</RouterLink>
                  <div class="item-actions no-print">
                    <button class="link" @click="shopper.toggleWishlist(line.productId)">
                      <Heart :size="13" /> {{ shopper.wishlist.includes(line.productId) ? 'Saved' : 'Save for later' }}
                    </button>
                    <RouterLink :to="`/product/${line.productId}`" class="link"><ShoppingCart :size="13" /> View</RouterLink>
                  </div>
                </div>
              </td>
              <td class="num">₹{{ line.price.toLocaleString() }}</td>
              <td class="num">{{ line.quantity }}</td>
              <td class="num">₹{{ (line.price * line.quantity).toLocaleString() }}</td>
              <td class="no-print"><button class="btn btn-outline small" @click="addLine(line).then(() => say('Added to your cart'))">Add to cart</button></td>
            </tr>
          </tbody>
        </table>

        <div class="totals">
          <div><span>Subtotal</span><span>₹{{ subtotalOf(o).toLocaleString() }}</span></div>
          <div v-if="o.discount > 0"><span>Coupon {{ o.couponCode }}</span><span>-₹{{ o.discount.toLocaleString() }}</span></div>
          <div><span>Delivery</span><span>{{ deliveryOf(o) ? '₹' + deliveryOf(o) : 'Free' }}</span></div>
          <div class="grand"><span>Total paid</span><span>₹{{ o.total.toLocaleString() }}</span></div>
        </div>
      </div>
    </article>
  </section>
</template>

<style scoped>
.orders-head { display: flex; align-items: baseline; gap: 12px; margin-bottom: 18px; }
h1 { margin: 0; font-size: 2rem; }
.count { color: var(--ink-soft); }
.order { background: var(--paper); border-radius: 14px; padding: 18px 20px; box-shadow: var(--shadow-card); margin-bottom: 16px; }
.order > header { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; border-bottom: 1px solid var(--border); padding-bottom: 12px; }
.summary-cols { display: flex; gap: 28px; flex-wrap: wrap; font-size: 0.92rem; }
.label { display: block; font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.08em; color: var(--ink-soft); }
.head-right { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.details-btn { padding: 6px 14px; font-size: 0.85rem; }
.turned { transform: rotate(180deg); transition: transform 0.2s; }

.details { margin-top: 16px; display: grid; gap: 18px; }
.invoice-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; flex-wrap: wrap; }
.invoice-top h2 { margin: 0; font-size: 1.2rem; }
.muted { color: var(--ink-soft); margin: 4px 0 0; font-size: 0.9rem; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.actions .btn { padding: 7px 14px; font-size: 0.85rem; }

.eta { margin: 0 0 10px; color: var(--ink-soft); font-size: 0.92rem; }
.steps { list-style: none; padding: 0; margin: 0; display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; }
.steps li { font-size: 0.82rem; color: var(--ink-soft); display: flex; flex-direction: column; align-items: flex-start; gap: 6px; position: relative; }
.steps li::before { content: ''; position: absolute; top: 9px; left: 22px; right: -6px; height: 3px; background: var(--border); }
.steps li:last-child::before { display: none; }
.steps li.done { color: var(--ink); }
.steps li.done::before { background: var(--olive); }
.dot {
  width: 20px; height: 20px; border-radius: 50%; border: 2px solid var(--border); background: #fff;
  display: inline-flex; align-items: center; justify-content: center; color: #fff; position: relative; z-index: 1;
}
.steps li.done .dot { background: var(--olive); border-color: var(--olive); }
.steps li.now .dot { border-color: var(--accent); box-shadow: 0 0 0 4px rgba(194, 86, 47, 0.18); }

.addresses { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.box { border: 1px solid var(--border); border-radius: 12px; padding: 12px 14px; font-size: 0.92rem; }
.box h3 { margin: 0 0 6px; font-size: 0.95rem; }
.box p { margin: 0; }

.lines { width: 100%; border-collapse: collapse; font-size: 0.92rem; }
.lines th, .lines td { padding: 10px 6px; border-bottom: 1px solid var(--border); text-align: left; vertical-align: middle; }
.lines th { font-size: 0.74rem; text-transform: uppercase; letter-spacing: 0.06em; color: var(--ink-soft); }
.item { display: flex; gap: 12px; align-items: center; }
.item img { width: 58px; height: 58px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.item-name { color: var(--ink); font-weight: 600; text-decoration: none; }
.item-name:hover { text-decoration: underline; color: var(--link); }
.item-actions { display: flex; gap: 12px; margin-top: 4px; }
.link { background: none; border: none; padding: 0; font: inherit; font-size: 0.8rem; color: var(--link); cursor: pointer; display: inline-flex; gap: 4px; align-items: center; text-decoration: none; }
.btn.small { padding: 5px 12px; font-size: 0.8rem; }
.num { text-align: right; white-space: nowrap; }

.totals { display: grid; gap: 6px; justify-self: end; min-width: 280px; font-size: 0.92rem; }
.totals div { display: flex; justify-content: space-between; }
.totals .grand { font-weight: 700; font-size: 1.02rem; border-top: 1px solid var(--border); padding-top: 8px; }

.empty, .error { color: var(--ink-soft); }
.error { color: var(--danger); }
.notice { background: var(--navy-dark); color: var(--paper); border-radius: 999px; padding: 8px 16px; display: inline-block; margin-bottom: 12px; font-weight: 600; }

@media (max-width: 700px) {
  .steps { grid-template-columns: 1fr 1fr; row-gap: 14px; }
  .steps li::before { display: none; }
  .addresses { grid-template-columns: 1fr; }
  .lines .no-print { display: none; }
}
</style>
