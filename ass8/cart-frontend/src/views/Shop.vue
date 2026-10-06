<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Heart, Timer, GitCompare, X, Eye } from 'lucide-vue-next'
import { api } from '../api/client'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { useShopperStore, COMPARE_LIMIT } from '../stores/shopper'
import { getProductImage } from '../utils/productImage'

const cart = useCartStore()
const auth = useAuthStore()
const shopper = useShopperStore()

const products = ref([])
const selectedCategory = ref('All')
const savedOnly = ref(false)
const maxBudget = ref(null)
const comparing = ref(false)
const toast = ref('')
const toastError = ref(false)
const now = ref(Date.now())
let toastTimer = null
let clock = null

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

function toggleCompare(p) {
  if (!shopper.toggleCompare(p.id)) notify(`You can compare up to ${COMPARE_LIMIT} products`, true)
}

function view(p) {
  shopper.markViewed(p.id)
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

    <section id="catalog" class="catalog">
      <div class="toolbar">
        <div class="chips">
          <button v-for="c in categories" :key="c" class="chip" :class="{ on: selectedCategory === c }" @click="selectedCategory = c">{{ c }}</button>
          <button class="chip saved" :class="{ on: savedOnly }" @click="savedOnly = !savedOnly">
            <Heart :size="14" /> Saved ({{ shopper.wishlist.length }})
          </button>
        </div>

        <label class="gift-finder">
          <span>Gift finder: under <strong>₹{{ budget.toLocaleString() }}</strong></span>
          <input v-model.number="maxBudget" type="range" min="0" :max="priceCeiling" step="500" />
        </label>
      </div>

      <section v-if="recentlyViewed.length" class="recent">
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
            <label class="compare-check">
              <input type="checkbox" :checked="shopper.compare.includes(p.id)" @change="toggleCompare(p)" />
              Compare
            </label>
            <button class="btn btn-primary wide" :disabled="p.quantity === 0" @click="addToCart(p)">Add to cart</button>
          </div>
        </article>
        <p v-if="visible.length === 0" class="empty">Nothing matches these filters. Try a higher budget or another category.</p>
      </div>
    </section>

    <div v-if="comparedProducts.length" class="compare-tray">
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
.card-img { position: relative; background: #efe6d8; aspect-ratio: 4 / 3; }
.card-img img { width: 100%; height: 100%; object-fit: cover; }
.img-link { display: block; width: 100%; height: 100%; }
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
.card h3 a { color: inherit; text-decoration: none; }
.card h3 a:hover { text-decoration: underline; }
.price-row { display: flex; align-items: baseline; gap: 8px; }
.price { font-weight: 700; font-size: 1.1rem; }
.was { color: var(--ink-soft); font-size: 0.86rem; }
.stock { margin: 0; font-size: 0.82rem; font-weight: 600; }
.stock.low { color: var(--accent-dark); }
.stock.out { color: var(--danger); }
.compare-check { font-size: 0.84rem; display: flex; align-items: center; gap: 6px; color: var(--ink-soft); cursor: pointer; }
.wide { width: 100%; margin-top: auto; }
.empty { color: var(--ink-soft); grid-column: 1 / -1; }

.icon {
  border: 1px solid var(--border); background: #fff; border-radius: 6px;
  width: 28px; height: 28px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer;
}

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

.toast {
  position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%);
  background: var(--navy-dark); color: var(--paper); padding: 12px 20px; border-radius: 999px;
  box-shadow: 0 10px 30px rgba(43, 33, 27, 0.3); font-weight: 600; z-index: 80;
}
.toast.error { background: var(--danger); }

@media (max-width: 900px) {
  .hero { grid-template-columns: 1fr; padding: 30px; }
  .compare-tray { left: 12px; right: 12px; border-radius: 16px; }
}
</style>
