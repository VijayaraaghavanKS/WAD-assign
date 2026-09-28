<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  ShoppingCart, Plus, Minus, X, CheckCircle2, PackageSearch, Star, Truck, ShieldCheck, Lock,
  Laptop, Headphones, Watch, Shirt, Camera, LayoutGrid, Sparkles, Tag,
} from 'lucide-vue-next'
import { api } from '../api/client'
import { useCartStore } from '../stores/cart'
import { getProductImage } from '../utils/productImage'
import { ratingFor, reviewCountFor, discountPctFor, wasPriceFor, categoryFor } from '../utils/productMeta'

const products = ref([])
const cart = useCartStore()
const toastText = ref('')
const selectedCategory = ref('All')
let toastTimer = null

onMounted(async () => {
  products.value = await api.getProducts()
  await cart.load()
})

const categoryIcons = {
  Electronics: Laptop,
  Audio: Headphones,
  Wearables: Watch,
  Fashion: Shirt,
  Cameras: Camera,
  More: LayoutGrid,
}

const categories = computed(() => {
  const seen = new Set()
  for (const p of products.value) seen.add(categoryFor(p.name))
  return Array.from(seen)
})

const visibleProducts = computed(() => {
  if (selectedCategory.value === 'All') return products.value
  return products.value.filter((p) => categoryFor(p.name) === selectedCategory.value)
})

function scrollToProducts() {
  document.getElementById('product-grid')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function handleAdd(product) {
  await cart.add(product.id)
  toastText.value = `Added "${product.name}" to cart`
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toastText.value = ''), 2200)
}

// There's no payment gateway in scope for this assignment, so "checkout"
// completes the order by clearing the cart and confirming it -- it still
// needs to actually do something and say something, rather than being a
// dead button.
async function checkout() {
  if (cart.items.length === 0) return
  await cart.clear()
  toastText.value = 'Order placed! Thanks for shopping with ShopCart.'
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toastText.value = ''), 2600)
}

// 3D tilt-on-hover, the classic "product card" interaction: rotate the card
// toward the cursor based on where inside it the mouse currently is.
function tilt(e) {
  const card = e.currentTarget
  const rect = card.getBoundingClientRect()
  const px = (e.clientX - rect.left) / rect.width
  const py = (e.clientY - rect.top) / rect.height
  const rotateY = (px - 0.5) * 12
  const rotateX = (0.5 - py) * 12
  card.style.transform = `perspective(900px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-4px)`
}

function resetTilt(e) {
  e.currentTarget.style.transform = ''
}

function starFill(rating, i) {
  if (rating >= i + 1) return 'full'
  if (rating >= i + 0.5) return 'half'
  return 'empty'
}
</script>

<template>
  <div class="shop">
    <transition name="toast">
      <div v-if="toastText" class="toast">
        <CheckCircle2 :size="18" />
        <span>{{ toastText }}</span>
      </div>
    </transition>

    <section class="hero">
      <div class="hero-copy">
        <h1>Everything you need, delivered fast.</h1>
        <p>Browse electronics, wearables, fashion and more &mdash; with free next-day delivery on eligible orders.</p>
        <button class="btn btn-buy hero-cta" @click="scrollToProducts">
          <Sparkles :size="16" /> Shop Best Sellers
        </button>
      </div>
      <div class="hero-visual">
        <img :src="getProductImage('laptop')" alt="Featured laptop deal" />
        <div class="hero-badge">
          <Tag :size="14" /> Up to 30% off Electronics
        </div>
      </div>
    </section>

    <nav class="category-rail" aria-label="Shop by category">
      <button
        class="category-chip"
        :class="{ active: selectedCategory === 'All' }"
        @click="selectedCategory = 'All'"
      >
        <span class="chip-icon"><LayoutGrid :size="20" /></span>
        All
      </button>
      <button
        v-for="cat in categories"
        :key="cat"
        class="category-chip"
        :class="{ active: selectedCategory === cat }"
        @click="selectedCategory = cat"
      >
        <span class="chip-icon">
          <component :is="categoryIcons[cat] || LayoutGrid" :size="20" />
        </span>
        {{ cat }}
      </button>
    </nav>

    <section class="promo-grid">
      <div class="promo-tile promo-electronics">
        <div class="promo-text">
          <span class="promo-kicker">Electronics</span>
          <h3>Blowout deals, up to 30% off</h3>
        </div>
      </div>
      <div class="promo-tile promo-wearables">
        <div class="promo-text">
          <span class="promo-kicker">Just dropped</span>
          <h3>New wearables collection</h3>
        </div>
      </div>
      <div class="promo-tile promo-delivery">
        <Truck :size="26" />
        <div class="promo-text">
          <h3>Free delivery over &#8377;999</h3>
          <span class="promo-sub">On every eligible order, every day</span>
        </div>
      </div>
    </section>

    <div class="layout">
      <section class="products" id="product-grid">
        <div class="products-head">
          <h2>{{ selectedCategory === 'All' ? 'All products' : selectedCategory }}</h2>
          <span class="count">{{ visibleProducts.length }} results</span>
        </div>

        <div class="grid">
          <div
            class="card"
            v-for="p in visibleProducts"
            :key="p.id"
            @mousemove="tilt"
            @mouseleave="resetTilt"
          >
            <div class="thumb-wrap">
              <img :src="getProductImage(p.name)" :alt="p.name" class="thumb-img" />
              <span v-if="discountPctFor(p.id || p.name) > 0" class="discount-badge">
                -{{ discountPctFor(p.id || p.name) }}%
              </span>
            </div>
            <div class="card-body">
              <div class="name">{{ p.name }}</div>

              <div class="rating-row">
                <span class="stars">
                  <Star
                    v-for="i in 5"
                    :key="i"
                    :size="14"
                    :class="starFill(ratingFor(p.id || p.name), i - 1)"
                    :fill="starFill(ratingFor(p.id || p.name), i - 1) === 'full' ? 'currentColor' : 'none'"
                  />
                </span>
                <span class="review-count">{{ reviewCountFor(p.id || p.name).toLocaleString() }}</span>
              </div>

              <div class="price-row">
                <span class="currency">₹</span>
                <span class="price">{{ p.price.toLocaleString() }}</span>
                <span v-if="wasPriceFor(p.price, p.id || p.name)" class="was-price">
                  ₹{{ wasPriceFor(p.price, p.id || p.name).toLocaleString() }}
                </span>
              </div>

              <div class="stock-line" v-if="p.quantity > 0 && p.quantity <= 5">
                Only {{ p.quantity }} left in stock
              </div>
              <div class="prime-line">
                <Truck :size="13" /> FREE Delivery by tomorrow
              </div>

              <button class="btn btn-cart add-btn" @click="handleAdd(p)">
                <ShoppingCart :size="16" />
                Add to Cart
              </button>
            </div>
          </div>
          <div v-if="visibleProducts.length === 0" class="empty-grid">
            <PackageSearch :size="36" />
            <p>No products in this category yet.</p>
          </div>
        </div>
      </section>

      <aside class="cart-panel" id="cart-panel">
        <h2><ShoppingCart :size="20" /> Cart ({{ cart.itemCount }})</h2>
        <div v-if="cart.items.length === 0" class="empty">
          <PackageSearch :size="36" />
          <p>Your cart is empty.</p>
        </div>
        <ul v-else class="cart-list">
          <li v-for="item in cart.items" :key="item.id">
            <img :src="getProductImage(item.productName)" :alt="item.productName" class="mini-thumb" />
            <div class="item-info">
              <div class="line1">
                <span class="pname">{{ item.productName }}</span>
                <button class="remove" @click="cart.remove(item.id)"><X :size="15" /></button>
              </div>
              <div class="eligible">Eligible for FREE Shipping</div>
              <div class="line2">
                <div class="qty">
                  <button @click="cart.changeQuantity(item, -1)"><Minus :size="13" /></button>
                  <span>{{ item.quantity }}</span>
                  <button @click="cart.changeQuantity(item, 1)"><Plus :size="13" /></button>
                </div>
                <span class="line-total">₹{{ (item.price * item.quantity).toLocaleString() }}</span>
              </div>
            </div>
          </li>
        </ul>
        <div v-if="cart.items.length > 0" class="summary">
          <div class="total-row">
            <span>Subtotal ({{ cart.itemCount }} items)</span>
            <span class="total-amount">₹{{ cart.total.toLocaleString() }}</span>
          </div>
          <button class="btn btn-buy checkout-btn" @click="checkout">
            <Lock :size="14" /> Proceed to Checkout
          </button>
          <button class="btn btn-outline clear-btn" @click="cart.clear()">Clear Cart</button>
        </div>
        <div class="trust-row">
          <ShieldCheck :size="15" /> Secure transaction
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.shop {
  position: relative;
}

/* ---------- Hero ---------- */

.hero {
  background: linear-gradient(120deg, var(--navy-dark), var(--navy) 60%, #1c4b4f);
  color: white;
  border-radius: var(--radius);
  padding: 40px;
  margin-bottom: 18px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  align-items: center;
  gap: 32px;
  min-height: 320px;
  box-shadow: var(--shadow-card);
}

.hero-copy h1 {
  font-size: 2.4rem;
  line-height: 1.15;
  margin: 0 0 14px;
  letter-spacing: -0.02em;
  max-width: 16ch;
}

.hero-copy p {
  color: #cbd5e1;
  font-size: 1.02rem;
  max-width: 44ch;
  margin: 0 0 24px;
}

.hero-cta {
  padding: 13px 24px;
  font-size: 1rem;
}

.hero-visual {
  position: relative;
  justify-self: end;
  width: 100%;
  max-width: 380px;
}

.hero-visual img {
  width: 100%;
  height: 260px;
  object-fit: cover;
  border-radius: var(--radius);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.45);
}

.hero-badge {
  position: absolute;
  bottom: -16px;
  left: -16px;
  background: white;
  color: var(--ink);
  padding: 10px 16px;
  border-radius: var(--radius);
  font-weight: 700;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: var(--shadow-raised);
}

/* ---------- Category rail ---------- */

.category-rail {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px 2px 18px;
  margin-bottom: 4px;
}

.category-chip {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  background: var(--paper);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 14px 20px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.85rem;
  color: var(--ink);
  transition: border-color 0.15s, box-shadow 0.15s, transform 0.15s;
  min-width: 92px;
}

.category-chip:hover {
  box-shadow: var(--shadow-card);
  transform: translateY(-2px);
}

.category-chip.active {
  border-color: var(--accent);
  background: #fff7ec;
  color: var(--accent-dark);
}

.chip-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--navy);
}

.category-chip.active .chip-icon {
  background: var(--accent);
  color: white;
}

/* ---------- Promo tiles ---------- */

.promo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.promo-tile {
  border-radius: var(--radius);
  padding: 22px;
  min-height: 120px;
  display: flex;
  align-items: flex-end;
  gap: 14px;
  box-shadow: var(--shadow-card);
  color: white;
  background-size: cover;
  background-position: center;
}

.promo-electronics {
  background: linear-gradient(0deg, rgba(15, 17, 17, 0.72), rgba(15, 17, 17, 0.15)), url('../assets/products/laptop.jpg') center/cover;
}

.promo-wearables {
  background: linear-gradient(0deg, rgba(15, 17, 17, 0.72), rgba(15, 17, 17, 0.15)), url('../assets/products/watch.jpg') center/cover;
}

.promo-delivery {
  background: var(--navy-dark);
  align-items: center;
}

.promo-kicker {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--accent);
  font-weight: 800;
}

.promo-text h3 {
  margin: 2px 0 0;
  font-size: 1.15rem;
  line-height: 1.3;
}

.promo-sub {
  color: #cbd2d9;
  font-size: 0.82rem;
}

/* ---------- Layout ---------- */

.layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
  align-items: start;
}

.products {
  background: var(--paper);
  border-radius: var(--radius);
  padding: 18px 20px 22px;
  box-shadow: var(--shadow-card);
  scroll-margin-top: 120px;
}

.products-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  border-bottom: 1px solid var(--border);
  padding-bottom: 12px;
  margin-bottom: 18px;
}

.products-head h2 {
  font-size: 1.25rem;
  margin: 0;
}

.count {
  color: var(--ink-soft);
  font-size: 0.85rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 20px;
}

.empty-grid {
  grid-column: 1 / -1;
  text-align: center;
  color: var(--ink-soft);
  padding: 40px 0;
}

.empty-grid svg {
  color: #cbd5e1;
  margin-bottom: 8px;
}

.card {
  background: white;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
  transition: transform 0.12s ease-out, box-shadow 0.2s, border-color 0.2s;
  will-change: transform;
  display: flex;
  flex-direction: column;
}

.card:hover {
  box-shadow: var(--shadow-raised);
  border-color: transparent;
}

.thumb-wrap {
  height: 210px;
  overflow: hidden;
  background: #f4f4f4;
  position: relative;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.discount-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: var(--danger);
  color: white;
  font-size: 0.74rem;
  font-weight: 800;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
}

.card-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.name {
  font-weight: 500;
  color: var(--link);
  line-height: 1.3;
  min-height: 2.6em;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stars svg.full { color: var(--star); }
.stars svg.half { color: var(--star); opacity: 0.5; }
.stars svg.empty { color: #d5d9d9; }

.review-count {
  color: var(--link);
  font-size: 0.78rem;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 2px;
}

.currency {
  font-size: 0.72rem;
  position: relative;
  top: -6px;
  color: var(--ink);
}

.price {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--ink);
}

.was-price {
  color: var(--ink-soft);
  text-decoration: line-through;
  font-size: 0.82rem;
}

.stock-line {
  color: var(--danger);
  font-size: 0.78rem;
  font-weight: 600;
}

.prime-line {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--ink-soft);
  font-size: 0.78rem;
}

.add-btn {
  margin-top: auto;
  padding-top: 10px;
  width: 100%;
}

/* Cart panel */

.cart-panel {
  background: var(--paper);
  border-radius: var(--radius);
  padding: 18px;
  box-shadow: var(--shadow-card);
  position: sticky;
  top: 108px;
}

.cart-panel h2 {
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
}

.empty {
  color: var(--ink-soft);
  text-align: center;
  padding: 24px 0;
}

.empty svg {
  margin-bottom: 8px;
  color: #cbd5e1;
}

.cart-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.cart-list li {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

.mini-thumb {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.line1 {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.pname {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.92rem;
}

.eligible {
  color: var(--success);
  font-size: 0.72rem;
  margin: 2px 0 6px;
}

.remove {
  border: none;
  background: none;
  color: var(--link);
  cursor: pointer;
  display: flex;
  flex-shrink: 0;
}

.remove:hover {
  color: var(--link-hover);
}

.line2 {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.qty {
  display: flex;
  align-items: center;
  gap: 6px;
}

.qty button {
  width: 24px;
  height: 24px;
  border: 1px solid var(--border);
  background: #f0f2f2;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qty button:hover {
  background: #e3e6e6;
}

.line-total {
  font-weight: 700;
}

.summary {
  margin-top: 8px;
  border-top: 1px solid var(--border);
  padding-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 0.95rem;
}

.total-amount {
  font-weight: 700;
  font-size: 1.15rem;
}

.checkout-btn, .clear-btn {
  width: 100%;
}

.trust-row {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--ink-soft);
  font-size: 0.78rem;
}

.toast {
  position: fixed;
  top: 118px;
  right: 32px;
  background: var(--success);
  color: white;
  padding: 12px 18px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  box-shadow: 0 10px 24px rgba(6, 125, 98, 0.35);
  z-index: 50;
}

.toast-enter-active { transition: all 0.25s ease-out; }
.toast-leave-active { transition: all 0.25s ease-in; }
.toast-enter-from, .toast-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 28px;
  }
  .hero-visual {
    justify-self: stretch;
    max-width: none;
  }
  .promo-grid {
    grid-template-columns: 1fr;
  }
  .layout {
    grid-template-columns: 1fr;
  }
  .cart-panel {
    position: static;
  }
}
</style>
