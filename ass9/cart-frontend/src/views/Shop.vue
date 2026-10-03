<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ShoppingCart, Minus, Plus, Trash2, Heart, Timer, GitCompare, X, Truck, Tag, Eye } from 'lucide-vue-next'
import { api } from '../api/client'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { useShopperStore, COMPARE_LIMIT } from '../stores/shopper'
import { getProductImage } from '../utils/productImage'

const cart = useCartStore()
const auth = useAuthStore()
const shopper = useShopperStore()

// Same codes the Order Service accepts. Used here only to preview the saving.
const COUPONS = { SAVE10: 10, WELCOME20: 20 }
const FREE_DELIVERY_AT = 5000
const DELIVERY_FEE = 99
const GST_RATE = 18
const PAYMENTS = [['UPI', 'UPI'], ['CARD', 'Card'], ['COD', 'Cash on delivery']]

const products = ref([])
const selectedCategory = ref('All')
const savedOnly = ref(false)
const maxBudget = ref(null)
const couponInput = ref('')
const appliedCoupon = ref('')
const comparing = ref(false)
const toast = ref('')
const toastError = ref(false)
const busy = ref(false)
const checkoutOpen = ref(false)
const payment = ref('UPI')
const address = reactive({ name: '', phone: '', line1: '', city: '', state: '', pin: '' })
const addressError = ref('')
const now = ref(Date.now())
let toastTimer = null
let clock = null

const isShopper = computed(() => auth.role === 'USER')
const categories = computed(() => ['All', ...new Set(products.value.map((p) => p.category).filter(Boolean))])
const priceCeiling = computed(() => Math.max(0, ...products.value.map((p) => salePrice(p))))
const budget = computed(() => maxBudget.value ?? priceCeiling.value)
const bestDeal = computed(() => Math.max(0, ...products.value.map((p) => p.discountPercent || 0)))

const visible = computed(() =>
  products.value.filter((p) =>
    (selectedCategory.value === 'All' || p.category === selectedCategory.value) &&
    (!savedOnly.value || shopper.wishlist.includes(p.id)) &&
    salePrice(p) <= budget.value
  )
)
const recentlyViewed = computed(() => shopper.recent.map((id) => products.value.find((p) => p.id === id)).filter(Boolean))
const comparedProducts = computed(() => shopper.compare.map((id) => products.value.find((p) => p.id === id)).filter(Boolean))

const salePrice = (p) => Math.round(p.price * (100 - (p.discountPercent || 0)) / 100)
const couponPercent = computed(() => COUPONS[appliedCoupon.value] ?? 0)
const couponSaving = computed(() => Math.round(cart.total * couponPercent.value / 100))
const afterCoupon = computed(() => cart.total - couponSaving.value)
const deliveryFee = computed(() => (afterCoupon.value >= FREE_DELIVERY_AT ? 0 : DELIVERY_FEE))
const payable = computed(() => afterCoupon.value + deliveryFee.value)
const gstIncluded = computed(() => Math.round(payable.value * GST_RATE / (100 + GST_RATE)))
const toFreeDelivery = computed(() => Math.max(0, FREE_DELIVERY_AT - cart.total))
const freeDeliveryPct = computed(() => Math.min(100, (cart.total / FREE_DELIVERY_AT) * 100))

// Counts down to midnight, when the "this week" deal banner resets.
const dealEndsIn = computed(() => {
  const end = new Date(now.value)
  end.setHours(24, 0, 0, 0)
  const s = Math.max(0, Math.floor((end - now.value) / 1000))
  return [Math.floor(s / 3600), Math.floor((s % 3600) / 60), s % 60].map((n) => String(n).padStart(2, '0')).join(':')
})

function notify(text, error = false) {
  toast.value = text
  toastError.value = error
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 3000)
}

async function loadProducts() {
  products.value = await api.getProducts()
}

async function addToCart(product) {
  try {
    // Cards add the first size and colour; the product page lets the shopper choose.
    await cart.add(product.id, { size: product.sizes?.[0], colour: product.colors?.[0] })
    notify(`Added ${product.name} to your cart`)
  } catch (e) {
    notify(e.message, true)
  }
}

async function changeQty(item, delta) {
  try {
    await cart.changeQuantity(item, delta)
  } catch (e) {
    notify(e.message, true)
  }
}

function applyCoupon() {
  const code = couponInput.value.trim().toUpperCase()
  if (!COUPONS[code]) return notify('That code is not valid', true)
  appliedCoupon.value = code
  notify(`${code} applied: ${COUPONS[code]}% off`)
}

function toggleCompare(p) {
  if (!shopper.toggleCompare(p.id)) notify(`You can compare up to ${COMPARE_LIMIT} products`, true)
}

function view(p) {
  shopper.markViewed(p.id)
}

// Opens the checkout form, filled with the last address this shopper used.
function openCheckout() {
  Object.assign(address, shopper.address ?? {})
  addressError.value = ''
  checkoutOpen.value = true
}

// Sends the order with the address and payment method. Mirrors the rules the
// Order Service checks: a name, street, city and 6-digit PIN are required.
async function placeOrder() {
  if (!address.name || !address.line1 || !address.city || !/^\d{6}$/.test(address.pin)) {
    addressError.value = 'Enter a name, street, city and a 6-digit PIN.'
    return
  }
  busy.value = true
  try {
    const order = await api.placeOrder({ coupon: appliedCoupon.value, address: { ...address }, payment: payment.value })
    shopper.saveAddress({ ...address })
    await cart.load()
    await loadProducts()
    appliedCoupon.value = ''
    couponInput.value = ''
    checkoutOpen.value = false
    notify(`Order placed. Invoice ${order.invoiceNumber}`)
  } catch (e) {
    notify(e.message, true)
  } finally {
    busy.value = false
  }
}

function scrollTo(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(() => {
  loadProducts()
  clock = setInterval(() => (now.value = Date.now()), 1000)
})
onUnmounted(() => clearInterval(clock))
</script>

<template>
  <div class="shop">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">Autumn edit</p>
        <h1>Considered gear for <em>slower</em> mornings.</h1>
        <p>Laptops, listening, and everyday carry, picked for how they feel to use.</p>
        <button class="btn btn-primary" @click="scrollTo('catalog')">Shop the edit</button>
      </div>
      <div class="hero-art" aria-hidden="true">
        <img :src="getProductImage('laptop')" alt="" />
      </div>
    </section>

    <section v-if="bestDeal > 0" class="deal">
      <span><strong>Up to {{ bestDeal }}% off</strong> on selected pieces.</span>
      <span class="countdown"><Timer :size="15" /> Ends in {{ dealEndsIn }}</span>
    </section>

    <div class="shop-grid">
      <section id="catalog" class="catalog">
        <div class="toolbar">
          <div class="chips">
            <button v-for="c in categories" :key="c" class="chip" :class="{ on: selectedCategory === c }" @click="selectedCategory = c">{{ c }}</button>
            <button v-if="isShopper" class="chip saved" :class="{ on: savedOnly }" @click="savedOnly = !savedOnly">
              <Heart :size="14" /> Saved ({{ shopper.wishlist.length }})
            </button>
          </div>

          <label class="gift-finder">
            <span>Gift finder: under <strong>₹{{ budget.toLocaleString() }}</strong></span>
            <input v-model.number="maxBudget" type="range" min="0" :max="priceCeiling" step="500" />
          </label>
        </div>

        <section v-if="isShopper && recentlyViewed.length" class="recent">
          <h2><Eye :size="16" /> Recently viewed</h2>
          <div class="recent-row">
            <button v-for="p in recentlyViewed" :key="p.id" class="recent-item" @click="view(p)">
              <img :src="getProductImage(p.name)" :alt="p.name" />
              <span>{{ p.name }}</span>
            </button>
          </div>
        </section>

        <div class="cards">
          <article v-for="p in visible" :key="p.id" class="card">
            <div class="card-img">
              <RouterLink :to="`/product/${p.id}`" class="img-link" :aria-label="`View ${p.name}`" @click="view(p)">
                <img :src="getProductImage(p.name)" :alt="p.name" />
              </RouterLink>
              <span v-if="p.discountPercent" class="badge">-{{ p.discountPercent }}%</span>
              <button
                v-if="isShopper"
                class="fav"
                :class="{ on: shopper.wishlist.includes(p.id) }"
                :aria-label="shopper.wishlist.includes(p.id) ? 'Remove from saved' : 'Save for later'"
                @click.stop="shopper.toggleWishlist(p.id)"
              ><Heart :size="16" /></button>
            </div>
            <div class="card-body">
              <p class="card-cat">{{ p.category || 'More' }}</p>
              <h3><RouterLink :to="`/product/${p.id}`" @click="view(p)">{{ p.name }}</RouterLink></h3>
              <div class="price-row">
                <span class="price">₹{{ salePrice(p).toLocaleString() }}</span>
                <s v-if="p.discountPercent" class="was">₹{{ p.price.toLocaleString() }}</s>
              </div>
              <p v-if="p.quantity === 0" class="stock out">Out of stock</p>
              <p v-else-if="p.quantity <= 5" class="stock low">Only {{ p.quantity }} left</p>
              <label v-if="isShopper" class="compare-check">
                <input type="checkbox" :checked="shopper.compare.includes(p.id)" @change="toggleCompare(p)" />
                Compare
              </label>
              <button
                v-if="isShopper"
                class="btn btn-primary wide"
                :disabled="p.quantity === 0"
                @click="addToCart(p)"
              >Add to cart</button>
            </div>
          </article>
          <p v-if="visible.length === 0" class="empty">Nothing matches these filters. Try a higher budget or another category.</p>
        </div>
      </section>

      <aside v-if="isShopper" id="cart-panel" class="cart-panel">
        <h2><ShoppingCart :size="20" /> Your cart</h2>

        <p v-if="cart.items.length === 0" class="empty">Your cart is empty. Add something you love.</p>

        <ul v-else class="lines">
          <li v-for="item in cart.items" :key="item.id">
            <div class="line-info">
              <span class="line-name">{{ item.productName }}</span>
              <span v-if="item.size || item.colour" class="variant">{{ [item.size, item.colour].filter(Boolean).join(' · ') }}</span>
              <span class="line-price">₹{{ (item.price * item.quantity).toLocaleString() }}</span>
            </div>
            <div class="line-actions">
              <button class="icon" aria-label="Decrease" @click="changeQty(item, -1)"><Minus :size="14" /></button>
              <span>{{ item.quantity }}</span>
              <button class="icon" aria-label="Increase" @click="changeQty(item, 1)"><Plus :size="14" /></button>
              <button class="icon danger" aria-label="Remove" @click="cart.remove(item.id)"><Trash2 :size="14" /></button>
            </div>
          </li>
        </ul>

        <div v-if="cart.items.length" class="summary">
          <div class="delivery-meter">
            <p v-if="toFreeDelivery > 0"><Truck :size="14" /> Add ₹{{ toFreeDelivery.toLocaleString() }} more for free delivery</p>
            <p v-else><Truck :size="14" /> You unlocked free delivery</p>
            <div class="bar"><span :style="{ width: freeDeliveryPct + '%' }"></span></div>
          </div>

          <form class="coupon" @submit.prevent="applyCoupon">
            <Tag :size="15" />
            <input v-model="couponInput" placeholder="Coupon code" aria-label="Coupon code" />
            <button type="submit" class="btn btn-outline">Apply</button>
          </form>
          <p v-if="appliedCoupon" class="coupon-note">{{ appliedCoupon }} saves ₹{{ couponSaving.toLocaleString() }}</p>

          <div class="sum-row"><span>Subtotal ({{ cart.itemCount }} items)</span><strong>₹{{ cart.total.toLocaleString() }}</strong></div>
          <div v-if="couponSaving" class="sum-row save"><span>Coupon</span><span>-₹{{ couponSaving.toLocaleString() }}</span></div>
          <div class="sum-row"><span>Delivery</span><span>{{ deliveryFee ? '₹' + deliveryFee : 'Free' }}</span></div>
          <div class="sum-row total"><span>To pay</span><strong>₹{{ payable.toLocaleString() }}</strong></div>
          <p class="gst">Includes GST of ₹{{ gstIncluded.toLocaleString() }}</p>
          <button class="btn btn-primary wide" :disabled="busy" @click="openCheckout">Proceed to checkout</button>
        </div>
      </aside>
    </div>

    <div v-if="isShopper && comparedProducts.length" class="compare-tray">
      <span><GitCompare :size="16" /> Comparing {{ comparedProducts.length }} of {{ COMPARE_LIMIT }}</span>
      <button class="btn btn-outline" :disabled="comparedProducts.length < 2" @click="comparing = true">Compare</button>
      <button class="icon" aria-label="Clear comparison" @click="shopper.clearCompare()"><X :size="14" /></button>
    </div>

    <div v-if="comparing" class="modal-backdrop" @click.self="comparing = false">
      <div class="modal" role="dialog" aria-label="Compare products">
        <header>
          <h2>Compare</h2>
          <button class="icon" aria-label="Close" @click="comparing = false"><X :size="16" /></button>
        </header>
        <div class="table-scroll">
          <table class="compare">
            <thead>
              <tr><th></th><th v-for="p in comparedProducts" :key="p.id">{{ p.name }}</th></tr>
            </thead>
            <tbody>
              <tr><th>Photo</th><td v-for="p in comparedProducts" :key="p.id"><img :src="getProductImage(p.name)" :alt="p.name" /></td></tr>
              <tr><th>Category</th><td v-for="p in comparedProducts" :key="p.id">{{ p.category || 'More' }}</td></tr>
              <tr><th>You pay</th><td v-for="p in comparedProducts" :key="p.id" class="best-cell">₹{{ salePrice(p).toLocaleString() }}</td></tr>
              <tr><th>List price</th><td v-for="p in comparedProducts" :key="p.id">₹{{ p.price.toLocaleString() }}</td></tr>
              <tr><th>Sale</th><td v-for="p in comparedProducts" :key="p.id">{{ p.discountPercent ? p.discountPercent + '% off' : 'None' }}</td></tr>
              <tr><th>Stock</th><td v-for="p in comparedProducts" :key="p.id">{{ p.quantity === 0 ? 'Out of stock' : p.quantity + ' left' }}</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="checkoutOpen" class="modal-backdrop" @click.self="checkoutOpen = false">
      <form class="modal checkout" role="dialog" aria-label="Checkout" @submit.prevent="placeOrder">
        <header>
          <h2>Checkout</h2>
          <button type="button" class="icon" aria-label="Close" @click="checkoutOpen = false"><X :size="16" /></button>
        </header>

        <h3>Delivery address</h3>
        <div class="form-grid">
          <input v-model="address.name" placeholder="Full name" required />
          <input v-model="address.phone" placeholder="Phone" />
          <input v-model="address.line1" placeholder="House no., street" class="full" required />
          <input v-model="address.city" placeholder="City" required />
          <input v-model="address.state" placeholder="State" />
          <input v-model="address.pin" placeholder="PIN (6 digits)" maxlength="6" required />
        </div>
        <p v-if="addressError" class="form-error">{{ addressError }}</p>

        <h3>Payment</h3>
        <div class="payments">
          <label v-for="[value, label] in PAYMENTS" :key="value" :class="{ on: payment === value }">
            <input type="radio" name="payment" :value="value" v-model="payment" /> {{ label }}
          </label>
        </div>

        <div class="modal-total">
          <span>To pay</span><strong>₹{{ payable.toLocaleString() }}</strong>
        </div>
        <button class="btn btn-primary wide" :disabled="busy" type="submit">{{ busy ? 'Placing order...' : 'Place order' }}</button>
      </form>
    </div>

    <div v-if="toast" class="toast" :class="{ error: toastError }" role="status">{{ toast }}</div>
  </div>
</template>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  align-items: center;
  gap: 32px;
  background: linear-gradient(120deg, #ead9c2, #f6ece0);
  border-radius: 20px;
  padding: 44px 48px;
  margin-bottom: 18px;
}

.eyebrow { text-transform: uppercase; letter-spacing: 0.14em; font-size: 0.76rem; font-weight: 700; color: var(--olive); margin: 0 0 10px; }
.hero h1 { font-size: clamp(2rem, 4vw, 3.2rem); line-height: 1.05; margin: 0 0 14px; }
.hero h1 em { color: var(--accent); }
.hero p { color: var(--ink-soft); max-width: 440px; }
.hero-art img { width: 100%; max-height: 260px; object-fit: contain; border-radius: 14px; }

.deal {
  background: var(--navy-dark);
  color: var(--paper);
  border-radius: 12px;
  padding: 14px 20px;
  margin-bottom: 22px;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}
.deal strong { color: var(--accent); }
.countdown { display: inline-flex; align-items: center; gap: 6px; font-variant-numeric: tabular-nums; font-weight: 700; }

.shop-grid { display: grid; grid-template-columns: 1fr 320px; gap: 26px; align-items: start; }

.toolbar { display: grid; gap: 12px; margin-bottom: 16px; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  border: 1px solid var(--border); background: var(--paper); color: var(--ink);
  padding: 7px 16px; border-radius: 999px; font: inherit; font-weight: 600; cursor: pointer;
  display: inline-flex; align-items: center; gap: 6px;
}
.chip.on { background: var(--ink); color: var(--paper); border-color: var(--ink); }
.chip.saved.on { background: var(--accent); border-color: var(--accent); }

.gift-finder {
  background: var(--paper); border: 1px solid var(--border); border-radius: 12px;
  padding: 10px 14px; display: grid; gap: 6px; font-size: 0.88rem;
}
.gift-finder input { width: 100%; accent-color: var(--accent); }

.recent { margin-bottom: 18px; }
.recent h2 { display: flex; align-items: center; gap: 6px; font-size: 1rem; margin: 0 0 10px; }
.recent-row { display: flex; gap: 10px; overflow-x: auto; padding-bottom: 4px; }
.recent-item {
  display: flex; align-items: center; gap: 8px; min-width: 190px;
  background: var(--paper); border: 1px solid var(--border); border-radius: 10px; padding: 6px 10px 6px 6px;
  font: inherit; font-size: 0.85rem; cursor: pointer; text-align: left;
}
.recent-item img { width: 44px; height: 44px; object-fit: cover; border-radius: 6px; }

.cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 18px; }

.card { background: var(--paper); border-radius: 16px; overflow: hidden; box-shadow: var(--shadow-card); display: flex; flex-direction: column; }
.card-img { position: relative; background: #efe6d8; aspect-ratio: 4 / 3; cursor: zoom-in; }
.card-img img { width: 100%; height: 100%; object-fit: cover; }
.badge { position: absolute; top: 12px; left: 12px; background: var(--accent); color: #fff; font-weight: 700; font-size: 0.78rem; padding: 4px 10px; border-radius: 999px; }
.fav {
  position: absolute; top: 10px; right: 10px; width: 34px; height: 34px; border-radius: 50%;
  border: none; background: rgba(255, 255, 255, 0.9); color: var(--ink); cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center;
}
.fav.on { color: var(--accent); }
.fav.on svg { fill: currentColor; }

.card-body { padding: 14px 16px 16px; display: flex; flex-direction: column; gap: 6px; flex: 1; }
.card-cat { margin: 0; font-size: 0.74rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--olive); font-weight: 700; }
.card h3 { margin: 0; font-size: 1.1rem; }
.price-row { display: flex; align-items: baseline; gap: 8px; }
.price { font-weight: 700; font-size: 1.1rem; }
.was { color: var(--ink-soft); font-size: 0.86rem; }
.stock { margin: 0; font-size: 0.82rem; font-weight: 600; }
.stock.low { color: var(--accent-dark); }
.stock.out { color: var(--danger); }
.compare-check { font-size: 0.84rem; display: flex; align-items: center; gap: 6px; color: var(--ink-soft); cursor: pointer; }
.wide { width: 100%; margin-top: auto; }
.empty { color: var(--ink-soft); grid-column: 1 / -1; }

.cart-panel {
  position: sticky;
  top: 120px;
  background: var(--paper);
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--shadow-card);
}
.cart-panel h2 { display: flex; align-items: center; gap: 8px; margin: 0 0 12px; font-size: 1.3rem; }

.lines { list-style: none; padding: 0; margin: 0; display: grid; gap: 12px; }
.lines li { border-bottom: 1px solid var(--border); padding-bottom: 12px; }
.line-info { display: flex; justify-content: space-between; gap: 10px; font-size: 0.92rem; }
.line-name { font-weight: 600; }
.variant { display: block; font-size: 0.8rem; color: var(--ink-soft); }
.line-actions { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.icon {
  border: 1px solid var(--border); background: #fff; border-radius: 6px;
  width: 28px; height: 28px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer;
}
.icon.danger { margin-left: auto; color: var(--danger); }

.summary { margin-top: 16px; display: grid; gap: 10px; }
.delivery-meter p { display: flex; align-items: center; gap: 6px; margin: 0 0 6px; font-size: 0.84rem; color: var(--ink-soft); }
.bar { height: 6px; background: var(--border); border-radius: 999px; overflow: hidden; }
.bar span { display: block; height: 100%; background: var(--olive); transition: width 0.3s; }
.coupon { display: flex; align-items: center; gap: 6px; color: var(--ink-soft); }
.coupon input {
  flex: 1; min-width: 0; border: 1px solid var(--border); border-radius: var(--radius-sm);
  padding: 7px 9px; font: inherit; text-transform: uppercase;
}
.coupon-note { margin: 0; font-size: 0.82rem; color: var(--olive); font-weight: 600; }
.sum-row { display: flex; justify-content: space-between; font-size: 0.95rem; }
.sum-row.save { color: var(--olive); }
.sum-row.total { border-top: 1px solid var(--border); padding-top: 10px; font-size: 1.05rem; }

.compare-tray {
  position: fixed; bottom: 24px; left: 24px; z-index: 50;
  background: var(--navy-dark); color: var(--paper); border-radius: 999px;
  padding: 8px 10px 8px 18px; display: flex; align-items: center; gap: 12px;
  box-shadow: 0 10px 30px rgba(43, 33, 27, 0.3);
}
.compare-tray .btn-outline { background: var(--paper); }
.compare-tray .icon { background: transparent; border-color: rgba(251, 247, 240, 0.4); color: var(--paper); }

.modal-backdrop {
  position: fixed; inset: 0; background: rgba(43, 33, 27, 0.5); z-index: 70;
  display: flex; align-items: center; justify-content: center; padding: 16px;
}
.modal { background: var(--paper); border-radius: 16px; padding: 20px; width: min(760px, 100%); box-shadow: var(--shadow-card); }
.modal header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.modal h2 { margin: 0; font-size: 1.3rem; }
.compare { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.compare th, .compare td { padding: 10px; border-bottom: 1px solid var(--border); text-align: left; vertical-align: middle; }
.compare thead th { font-family: 'Fraunces', Georgia, serif; }
.compare td img { width: 90px; height: 70px; object-fit: cover; border-radius: 8px; }
.best-cell { font-weight: 700; color: var(--olive); }

.img-link { display: block; width: 100%; height: 100%; }
.card h3 a { color: inherit; text-decoration: none; }
.card h3 a:hover { text-decoration: underline; }
.gst { margin: -4px 0 0; font-size: 0.78rem; color: var(--ink-soft); text-align: right; }
.checkout { display: grid; gap: 12px; width: min(520px, 100%); }
.checkout h3 { margin: 6px 0 0; font-size: 0.95rem; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.form-grid .full { grid-column: 1 / -1; }
.checkout input { font: inherit; border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 9px 10px; min-width: 0; }
.form-error { color: var(--danger); margin: 0; font-size: 0.86rem; }
.payments { display: flex; gap: 8px; flex-wrap: wrap; }
.payments label { border: 1px solid var(--border); border-radius: 999px; padding: 7px 14px; cursor: pointer; font-weight: 600; font-size: 0.9rem; }
.payments label.on { border-color: var(--accent); background: #f6e7cf; }
.payments input { margin-right: 4px; accent-color: var(--accent); }
.modal-total { display: flex; justify-content: space-between; font-size: 1.05rem; border-top: 1px solid var(--border); padding-top: 10px; }

.toast {
  position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%);
  background: var(--navy-dark); color: var(--paper); padding: 12px 20px; border-radius: 999px;
  box-shadow: 0 10px 30px rgba(43, 33, 27, 0.3); font-weight: 600; z-index: 80;
}
.toast.error { background: var(--danger); }

@media (max-width: 900px) {
  .hero { grid-template-columns: 1fr; padding: 30px; }
  .shop-grid { grid-template-columns: 1fr; }
  .cart-panel { position: static; }
  .compare-tray { left: 12px; right: 12px; border-radius: 16px; }
  .form-grid { grid-template-columns: 1fr; }
}
</style>
