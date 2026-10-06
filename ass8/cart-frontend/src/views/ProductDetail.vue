<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { Heart, Star, ShoppingCart, Truck, ShieldCheck, RotateCcw } from 'lucide-vue-next'
import { api } from '../api/client'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { useShopperStore } from '../stores/shopper'
import { getProductImage } from '../utils/productImage'

const route = useRoute()
const cart = useCartStore()
const auth = useAuthStore()
const shopper = useShopperStore()

const COLOUR_HEX = { Black: '#2b211b', Sand: '#d8c3a5', Terracotta: '#c2562f', Olive: '#6b7144', Navy: '#2e3a59', Silver: '#b8bcc2' }

const product = ref(null)
const all = ref([])
const error = ref('')
const size = ref('')
const colour = ref('')
const qty = ref(1)
const toast = ref('')
const review = ref({ rating: 5, title: '', body: '' })

const isShopper = computed(() => auth.role === 'USER')
// Guests can buy too: their cart is kept in the browser until they sign in to check out.
const canBuy = computed(() => !auth.isLoggedIn || isShopper.value)
const salePrice = computed(() => Math.round(product.value.price * (100 - (product.value.discountPercent || 0)) / 100))
const reviews = computed(() => product.value?.reviews ?? [])
const avgRating = computed(() => (reviews.value.length ? reviews.value.reduce((s, r) => s + r.rating, 0) / reviews.value.length : 0))
const ratingBars = computed(() => [5, 4, 3, 2, 1].map((star) => ({
  star, count: reviews.value.filter((r) => r.rating === star).length,
  pct: reviews.value.length ? (reviews.value.filter((r) => r.rating === star).length / reviews.value.length) * 100 : 0,
})))
const related = computed(() => all.value.filter((p) => p.category === product.value?.category && p.id !== product.value?.id).slice(0, 4))
const saved = computed(() => shopper.wishlist.includes(product.value?.id))
const outOfStock = computed(() => product.value?.quantity === 0)

function notify(text) {
  toast.value = text
  setTimeout(() => (toast.value = ''), 2800)
}

async function load() {
  error.value = ''
  try {
    product.value = await api.getProduct(route.params.id)
    size.value = product.value.sizes?.[0] ?? ''
    colour.value = product.value.colors?.[0] ?? ''
    qty.value = 1
    shopper.markViewed(product.value.id)
    all.value = await api.getProducts()
  } catch (e) {
    error.value = e.message
  }
}

// Size and colour go with the cart line, so the order keeps the choice.
async function addToCart() {
  try {
    for (let i = 0; i < qty.value; i++) await cart.add(product.value.id, { size: size.value, colour: colour.value })
    notify(`Added ${qty.value} to your cart`)
  } catch (e) {
    notify(e.message)
  }
}

async function postReview() {
  try {
    product.value = await api.addReview(product.value.id, review.value)
    review.value = { rating: 5, title: '', body: '' }
    notify('Thanks for your review')
  } catch (e) {
    notify(e.message)
  }
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<template>
  <div v-if="product" class="pdp">
    <nav class="crumbs"><RouterLink to="/">Shop</RouterLink> / {{ product.category || 'More' }} / {{ product.name }}</nav>

    <div class="top">
      <div class="gallery">
        <img :src="getProductImage(product.name)" :alt="product.name" />
        <span v-if="product.discountPercent" class="badge">-{{ product.discountPercent }}%</span>
      </div>

      <div class="buybox">
        <p class="cat">{{ product.category || 'More' }}</p>
        <h1>{{ product.name }}</h1>
        <p class="rating">
          <Star :size="15" class="star" /> {{ avgRating.toFixed(1) }}
          <span>({{ reviews.length }} ratings)</span>
        </p>

        <div class="price-row">
          <span class="price">₹{{ salePrice.toLocaleString() }}</span>
          <s v-if="product.discountPercent" class="was">₹{{ product.price.toLocaleString() }}</s>
          <span v-if="product.discountPercent" class="save">Save {{ product.discountPercent }}%</span>
        </div>
        <p class="tax">Inclusive of all taxes</p>

        <div v-if="product.sizes?.length" class="opt">
          <span class="opt-label">Size: <strong>{{ size }}</strong></span>
          <div class="chips">
            <button v-for="s in product.sizes" :key="s" class="chip" :class="{ on: size === s }" @click="size = s">{{ s }}</button>
          </div>
        </div>

        <div v-if="product.colors?.length" class="opt">
          <span class="opt-label">Colour: <strong>{{ colour }}</strong></span>
          <div class="chips">
            <button
              v-for="c in product.colors" :key="c" class="swatch" :class="{ on: colour === c }"
              :style="{ background: COLOUR_HEX[c] ?? '#ccc' }" :aria-label="c" :title="c" @click="colour = c"
            />
          </div>
        </div>

        <p v-if="outOfStock" class="stock out">Currently unavailable</p>
        <p v-else-if="product.quantity <= 5" class="stock low">Only {{ product.quantity }} left in stock</p>
        <p v-else class="stock">In stock</p>

        <template v-if="canBuy">
          <div class="buy-actions">
            <label class="qty">Qty
              <select v-model.number="qty"><option v-for="n in 5" :key="n" :value="n">{{ n }}</option></select>
            </label>
            <button class="btn btn-primary" :disabled="outOfStock" @click="addToCart"><ShoppingCart :size="16" /> Add to cart</button>
            <button class="btn btn-outline" :class="{ on: saved }" @click="shopper.toggleWishlist(product.id)">
              <Heart :size="16" :class="{ fill: saved }" /> {{ saved ? 'Saved' : 'Save for later' }}
            </button>
          </div>
        </template>

        <ul class="promises">
          <li><Truck :size="15" /> Free delivery over ₹5,000, otherwise ₹99</li>
          <li><RotateCcw :size="15" /> 30-day easy returns</li>
          <li><ShieldCheck :size="15" /> {{ product.specs?.Warranty || 'Brand warranty' }}</li>
        </ul>
        <p v-if="toast" class="toast-inline" role="status">{{ toast }}</p>
      </div>
    </div>

    <section class="block">
      <h2>About this item</h2>
      <p>{{ product.description }}</p>
    </section>

    <section class="block">
      <h2>Specifications</h2>
      <table class="specs">
        <tbody>
          <tr v-for="(value, key) in product.specs" :key="key"><th>{{ key }}</th><td>{{ value }}</td></tr>
        </tbody>
      </table>
    </section>

    <section class="block">
      <h2>Customer reviews</h2>
      <div class="review-summary">
        <div class="big">
          <span class="big-num">{{ avgRating.toFixed(1) }}</span>
          <span class="rating"><Star v-for="n in 5" :key="n" :size="14" class="star" :class="{ off: n > Math.round(avgRating) }" /></span>
          <span class="muted">{{ reviews.length }} ratings</span>
        </div>
        <div class="bars">
          <div v-for="b in ratingBars" :key="b.star" class="bar-row">
            <span>{{ b.star }} star</span>
            <div class="bar"><span :style="{ width: b.pct + '%' }"></span></div>
            <span class="muted">{{ b.count }}</span>
          </div>
        </div>
      </div>

      <form v-if="isShopper" class="write" @submit.prevent="postReview">
        <h3>Write a review</h3>
        <select v-model.number="review.rating" aria-label="Rating">
          <option v-for="n in 5" :key="n" :value="n">{{ n }} star{{ n > 1 ? 's' : '' }}</option>
        </select>
        <input v-model="review.title" placeholder="Headline" required />
        <textarea v-model="review.body" placeholder="What did you like or dislike?" rows="3" required></textarea>
        <button class="btn btn-primary" type="submit">Post review</button>
      </form>

      <article v-for="(r, i) in reviews" :key="i" class="review">
        <div class="review-head">
          <span class="rating"><Star v-for="n in 5" :key="n" :size="13" class="star" :class="{ off: n > r.rating }" /></span>
          <strong>{{ r.title }}</strong>
        </div>
        <p class="meta">{{ r.username }} &middot; {{ new Date(r.postedAt).toLocaleDateString() }}<span v-if="r.verified" class="verified"> &middot; Verified purchase</span></p>
        <p>{{ r.body }}</p>
      </article>
    </section>

    <section v-if="related.length" class="block">
      <h2>Customers also viewed</h2>
      <div class="related">
        <RouterLink v-for="p in related" :key="p.id" :to="`/product/${p.id}`" class="rel-card">
          <img :src="getProductImage(p.name)" :alt="p.name" />
          <span class="rel-name">{{ p.name }}</span>
          <span class="rel-price">₹{{ Math.round(p.price * (100 - (p.discountPercent || 0)) / 100).toLocaleString() }}</span>
        </RouterLink>
      </div>
    </section>
  </div>
  <p v-else-if="error" class="error">{{ error }}</p>
</template>

<style scoped>
.crumbs { font-size: 0.86rem; color: var(--ink-soft); margin-bottom: 14px; }
.crumbs a { color: var(--link); }
.top { display: grid; grid-template-columns: 1fr 1fr; gap: 32px; background: var(--paper); border-radius: 18px; padding: 24px; box-shadow: var(--shadow-card); }
.gallery { position: relative; background: #efe6d8; border-radius: 14px; overflow: hidden; aspect-ratio: 4 / 3; }
.gallery img { width: 100%; height: 100%; object-fit: cover; }
.badge { position: absolute; top: 12px; left: 12px; background: var(--accent); color: #fff; font-weight: 700; font-size: 0.8rem; padding: 4px 10px; border-radius: 999px; }
.cat { margin: 0; text-transform: uppercase; letter-spacing: 0.1em; font-size: 0.74rem; color: var(--olive); font-weight: 700; }
.buybox h1 { font-size: clamp(1.6rem, 3vw, 2.2rem); margin: 6px 0 10px; line-height: 1.15; }
.rating { display: inline-flex; align-items: center; gap: 4px; font-size: 0.92rem; }
.rating span, .muted { color: var(--ink-soft); }
.star { color: var(--star); fill: var(--star); }
.star.off { fill: none; color: var(--border); }
.price-row { display: flex; align-items: baseline; gap: 10px; margin-top: 14px; flex-wrap: wrap; }
.price { font-size: 1.8rem; font-weight: 700; font-family: 'Fraunces', Georgia, serif; }
.was { color: var(--ink-soft); }
.save { color: var(--olive); font-weight: 700; font-size: 0.9rem; }
.tax { margin: 2px 0 14px; font-size: 0.8rem; color: var(--ink-soft); }
.opt { margin-bottom: 14px; }
.opt-label { font-size: 0.88rem; color: var(--ink-soft); }
.chips { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 6px; }
.chip { border: 1px solid var(--border); background: #fff; border-radius: 8px; padding: 6px 14px; font: inherit; font-weight: 600; cursor: pointer; }
.chip.on { border-color: var(--ink); box-shadow: inset 0 0 0 1px var(--ink); }
.swatch { width: 30px; height: 30px; border-radius: 50%; border: 2px solid #fff; box-shadow: 0 0 0 1px var(--border); cursor: pointer; }
.swatch.on { box-shadow: 0 0 0 2px var(--ink); }
.stock { font-weight: 700; font-size: 0.9rem; margin: 6px 0 12px; color: var(--olive); }
.stock.low { color: var(--accent-dark); }
.stock.out { color: var(--danger); }
.buy-actions { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; margin-bottom: 16px; }
.qty select { font: inherit; padding: 8px; border-radius: var(--radius-sm); border: 1px solid var(--border); margin-left: 6px; }
.btn-outline.on { border-color: var(--accent); color: var(--accent); }
.fill { fill: currentColor; }
.promises { list-style: none; padding: 14px 0 0; margin: 0; display: grid; gap: 8px; font-size: 0.88rem; color: var(--ink-soft); border-top: 1px solid var(--border); }
.promises li { display: flex; gap: 8px; align-items: center; }
.toast-inline { color: var(--olive); font-weight: 600; margin-top: 10px; }

.block { background: var(--paper); border-radius: 18px; padding: 24px; box-shadow: var(--shadow-card); margin-top: 18px; }
.block h2 { margin: 0 0 12px; font-size: 1.3rem; }
.specs { width: 100%; border-collapse: collapse; font-size: 0.92rem; }
.specs th, .specs td { text-align: left; padding: 9px 6px; border-bottom: 1px solid var(--border); }
.specs th { width: 36%; color: var(--ink-soft); font-weight: 600; }

.review-summary { display: grid; grid-template-columns: 220px 1fr; gap: 24px; margin-bottom: 16px; }
.big { display: grid; gap: 4px; }
.big-num { font-family: 'Fraunces', Georgia, serif; font-size: 2.6rem; font-weight: 700; line-height: 1; }
.bars { display: grid; gap: 6px; font-size: 0.85rem; }
.bar-row { display: grid; grid-template-columns: 60px 1fr 28px; gap: 8px; align-items: center; }
.bar { height: 8px; background: var(--border); border-radius: 999px; overflow: hidden; }
.bar span { display: block; height: 100%; background: var(--star); }
.write { display: grid; gap: 8px; max-width: 540px; border: 1px solid var(--border); border-radius: 12px; padding: 14px; margin-bottom: 18px; }
.write h3 { margin: 0; font-size: 1rem; }
.write input, .write textarea, .write select { font: inherit; border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 8px; }
.review { border-top: 1px solid var(--border); padding: 14px 0; }
.review-head { display: flex; gap: 10px; align-items: center; }
.review .meta { font-size: 0.8rem; color: var(--ink-soft); margin: 4px 0 6px; }
.verified { color: var(--olive); font-weight: 600; }
.review p { margin: 0; }

.related { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 14px; }
.rel-card { text-decoration: none; color: var(--ink); background: #fff; border: 1px solid var(--border); border-radius: 12px; overflow: hidden; display: grid; }
.rel-card img { width: 100%; aspect-ratio: 4 / 3; object-fit: cover; }
.rel-name { font-weight: 600; padding: 8px 10px 0; font-size: 0.92rem; }
.rel-price { padding: 2px 10px 10px; color: var(--ink-soft); font-size: 0.88rem; }
.error { color: var(--danger); }

@media (max-width: 900px) {
  .top { grid-template-columns: 1fr; }
  .review-summary { grid-template-columns: 1fr; }
}
</style>
