<script setup>
import { computed, onMounted, ref } from 'vue'
import { ShoppingCart, Minus, Plus, Trash2 } from 'lucide-vue-next'
import { api } from '../api/client'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { getProductImage } from '../utils/productImage'

const cart = useCartStore()
const auth = useAuthStore()

const products = ref([])
const selectedCategory = ref('All')
const toast = ref('')
const toastError = ref(false)
const busy = ref(false)
let toastTimer = null

const isShopper = computed(() => auth.role === 'USER')
const categories = computed(() => ['All', ...new Set(products.value.map((p) => p.category).filter(Boolean))])
const visible = computed(() =>
  selectedCategory.value === 'All' ? products.value : products.value.filter((p) => p.category === selectedCategory.value)
)
const bestDeal = computed(() => Math.max(0, ...products.value.map((p) => p.discountPercent || 0)))

const salePrice = (p) => Math.round(p.price * (100 - (p.discountPercent || 0)) / 100)

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
    await cart.add(product.id)
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

async function checkout() {
  busy.value = true
  try {
    const order = await api.placeOrder()
    await cart.load()
    await loadProducts()
    notify(`Order placed. Reference ${order.id.slice(-6).toUpperCase()}`)
  } catch (e) {
    notify(e.message, true)
  } finally {
    busy.value = false
  }
}

function scrollTo(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(loadProducts)
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
      <strong>Up to {{ bestDeal }}% off</strong> on selected pieces this week.
    </section>

    <div class="shop-grid">
      <section id="catalog" class="catalog">
        <div class="chips">
          <button
            v-for="c in categories"
            :key="c"
            class="chip"
            :class="{ on: selectedCategory === c }"
            @click="selectedCategory = c"
          >{{ c }}</button>
        </div>

        <div class="cards">
          <article v-for="p in visible" :key="p.id" class="card">
            <div class="card-img">
              <img :src="getProductImage(p.name)" :alt="p.name" />
              <span v-if="p.discountPercent" class="badge">-{{ p.discountPercent }}%</span>
            </div>
            <div class="card-body">
              <p class="card-cat">{{ p.category || 'More' }}</p>
              <h3>{{ p.name }}</h3>
              <div class="price-row">
                <span class="price">₹{{ salePrice(p).toLocaleString() }}</span>
                <s v-if="p.discountPercent" class="was">₹{{ p.price.toLocaleString() }}</s>
              </div>
              <p v-if="p.quantity === 0" class="stock out">Out of stock</p>
              <p v-else-if="p.quantity <= 5" class="stock low">Only {{ p.quantity }} left</p>
              <button
                v-if="isShopper"
                class="btn btn-primary wide"
                :disabled="p.quantity === 0"
                @click="addToCart(p)"
              >Add to cart</button>
            </div>
          </article>
          <p v-if="visible.length === 0" class="empty">Nothing in this category yet.</p>
        </div>
      </section>

      <aside v-if="isShopper" id="cart-panel" class="cart-panel">
        <h2><ShoppingCart :size="20" /> Your cart</h2>

        <p v-if="cart.items.length === 0" class="empty">Your cart is empty. Add something you love.</p>

        <ul v-else class="lines">
          <li v-for="item in cart.items" :key="item.id">
            <div class="line-info">
              <span class="line-name">{{ item.productName }}</span>
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
          <div class="sum-row"><span>Subtotal ({{ cart.itemCount }} items)</span><strong>₹{{ cart.total.toLocaleString() }}</strong></div>
          <div class="sum-row muted"><span>Delivery</span><span>Free</span></div>
          <button class="btn btn-primary wide" :disabled="busy" @click="checkout">
            {{ busy ? 'Placing order...' : 'Proceed to checkout' }}
          </button>
        </div>
      </aside>
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
}
.deal strong { color: var(--accent); }

.shop-grid { display: grid; grid-template-columns: 1fr 320px; gap: 26px; align-items: start; }

.chips { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 18px; }
.chip {
  border: 1px solid var(--border); background: var(--paper); color: var(--ink);
  padding: 7px 16px; border-radius: 999px; font: inherit; font-weight: 600; cursor: pointer;
}
.chip.on { background: var(--ink); color: var(--paper); border-color: var(--ink); }

.cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 18px; }

.card { background: var(--paper); border-radius: 16px; overflow: hidden; box-shadow: var(--shadow-card); display: flex; flex-direction: column; }
.card-img { position: relative; background: #efe6d8; aspect-ratio: 4 / 3; }
.card-img img { width: 100%; height: 100%; object-fit: cover; }
.badge { position: absolute; top: 12px; left: 12px; background: var(--accent); color: #fff; font-weight: 700; font-size: 0.78rem; padding: 4px 10px; border-radius: 999px; }

.card-body { padding: 14px 16px 16px; display: flex; flex-direction: column; gap: 6px; flex: 1; }
.card-cat { margin: 0; font-size: 0.74rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--olive); font-weight: 700; }
.card h3 { margin: 0; font-size: 1.1rem; }
.price-row { display: flex; align-items: baseline; gap: 8px; }
.price { font-weight: 700; font-size: 1.1rem; }
.was { color: var(--ink-soft); font-size: 0.86rem; }
.stock { margin: 0; font-size: 0.82rem; font-weight: 600; }
.stock.low { color: var(--accent-dark); }
.stock.out { color: var(--danger); }
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
.line-actions { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.icon {
  border: 1px solid var(--border); background: #fff; border-radius: 6px;
  width: 28px; height: 28px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer;
}
.icon.danger { margin-left: auto; color: var(--danger); }

.summary { margin-top: 16px; display: grid; gap: 10px; }
.sum-row { display: flex; justify-content: space-between; font-size: 0.95rem; }
.sum-row.muted { color: var(--ink-soft); font-size: 0.88rem; }

.toast {
  position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%);
  background: var(--navy-dark); color: var(--paper); padding: 12px 20px; border-radius: 999px;
  box-shadow: 0 10px 30px rgba(43, 33, 27, 0.3); font-weight: 600; z-index: 60;
}
.toast.error { background: var(--danger); }

@media (max-width: 900px) {
  .hero { grid-template-columns: 1fr; padding: 30px; }
  .shop-grid { grid-template-columns: 1fr; }
  .cart-panel { position: static; }
}
</style>
