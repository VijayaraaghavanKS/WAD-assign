<script setup>
import { nextTick, onMounted } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { ShoppingCart, ShieldCheck, Code2, ShoppingBag, MapPin, Search } from 'lucide-vue-next'
import { useCartStore } from './stores/cart'

const cart = useCartStore()
const router = useRouter()
onMounted(() => cart.load())

// A RouterLink to the current route is a no-op, so clicking the cart icon
// while already on Shop did nothing. Navigate there first if needed, then
// scroll the cart panel into view either way.
async function goToCart() {
  if (router.currentRoute.value.path !== '/') {
    await router.push('/')
  }
  await nextTick()
  document.getElementById('cart-panel')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
</script>

<template>
  <div id="shell">
    <header class="masthead">
      <div class="masthead-row">
        <RouterLink to="/" class="brand">
          <ShoppingBag :size="26" />
          <span>ShopCart</span>
        </RouterLink>

        <div class="deliver">
          <MapPin :size="18" />
          <div class="deliver-text">
            <span class="deliver-label">Deliver to</span>
            <span class="deliver-value">Chennai 600119</span>
          </div>
        </div>

        <div class="search">
          <input type="text" placeholder="Search ShopCart" />
          <button type="button" aria-label="Search"><Search :size="18" /></button>
        </div>

        <button type="button" class="cart-link" @click="goToCart">
          <span class="cart-icon-wrap">
            <ShoppingCart :size="26" />
            <span class="cart-badge">{{ cart.itemCount }}</span>
          </span>
          <span class="cart-label">Cart</span>
        </button>
      </div>

      <nav class="subnav">
        <RouterLink to="/" exact-active-class="active"><ShoppingBag :size="15" /> Shop</RouterLink>
        <RouterLink to="/admin" exact-active-class="active"><ShieldCheck :size="15" /> Admin</RouterLink>
        <RouterLink to="/dev" exact-active-class="active"><Code2 :size="15" /> Developer</RouterLink>
      </nav>
    </header>

    <main>
      <RouterView />
    </main>

    <footer class="site-footer">
      <div class="footer-back-to-top">Back to top</div>
      <div class="footer-body">
        <span>ShopCart &mdash; ICS1511 Web Application Development Laboratory</span>
      </div>
    </footer>
  </div>
</template>

<style>
:root {
  --ink: #0f1111;
  --ink-soft: #565959;
  --paper: #ffffff;
  --bg: #eaeded;
  --navy-dark: #131921;
  --navy: #232f3e;
  --navy-light: #37475a;
  --accent: #ff9900;
  --accent-dark: #e88a00;
  --link: #007185;
  --link-hover: #c7511f;
  --price: #b12704;
  --star: #ffa41c;
  --success: #067d62;
  --success-bg: #d5f5e8;
  --danger: #cc0c39;
  --danger-bg: #fdeceb;
  --border: #d5d9d9;
  --radius: 8px;
  --radius-sm: 4px;
  --shadow-card: 0 2px 5px rgba(15, 17, 17, 0.15);
  --shadow-raised: 0 8px 24px rgba(15, 17, 17, 0.18);
}

* { box-sizing: border-box; }

html, body {
  margin: 0;
  height: 100%;
}

body {
  font-family: 'Amazon Ember', 'Segoe UI', system-ui, -apple-system, sans-serif;
  background: var(--bg);
  color: var(--ink);
  -webkit-font-smoothing: antialiased;
}

#shell {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

::selection {
  background: var(--accent);
  color: var(--navy-dark);
}

a:focus-visible, button:focus-visible, input:focus-visible {
  outline: 3px solid #4c9aff;
  outline-offset: 2px;
}

/* ---------- Header ---------- */

.masthead {
  position: sticky;
  top: 0;
  z-index: 40;
}

.masthead-row {
  background: var(--navy-dark);
  color: white;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 20px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  text-decoration: none;
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  flex-shrink: 0;
  border: 1px solid transparent;
  padding: 6px 8px;
  border-radius: var(--radius-sm);
}

.brand:hover {
  border-color: white;
}

.deliver {
  display: none;
  align-items: center;
  gap: 6px;
  color: white;
  flex-shrink: 0;
  padding: 6px 8px;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  cursor: default;
}

.deliver:hover {
  border-color: white;
}

.deliver-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.deliver-label {
  font-size: 0.72rem;
  color: #cbd2d9;
}

.deliver-value {
  font-size: 0.85rem;
  font-weight: 700;
}

.search {
  flex: 1;
  display: flex;
  height: 40px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  max-width: 780px;
}

.search input {
  flex: 1;
  border: none;
  padding: 0 14px;
  font-size: 0.95rem;
  min-width: 0;
}

.search input:focus {
  outline: none;
}

.search button {
  width: 46px;
  border: none;
  background: var(--accent);
  color: var(--navy-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s;
}

.search button:hover {
  background: var(--accent-dark);
}

.cart-link {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  color: white;
  text-decoration: none;
  flex-shrink: 0;
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  background: none;
  font: inherit;
  cursor: pointer;
}

.cart-link:hover {
  border-color: white;
}

.cart-icon-wrap {
  position: relative;
  display: flex;
}

.cart-badge {
  position: absolute;
  top: -8px;
  left: 14px;
  background: var(--accent);
  color: var(--navy-dark);
  font-size: 0.72rem;
  font-weight: 800;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.cart-label {
  font-weight: 700;
  font-size: 0.95rem;
}

.subnav {
  background: var(--navy);
  display: flex;
  gap: 4px;
  padding: 0 16px;
}

.subnav a {
  color: #e7e9ea;
  text-decoration: none;
  font-size: 0.88rem;
  font-weight: 600;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 7px;
  border: 1px solid transparent;
}

.subnav a:hover {
  border-color: rgba(255, 255, 255, 0.5);
  background: var(--navy-light);
}

.subnav a.active {
  color: var(--accent);
}

@media (min-width: 720px) {
  .deliver { display: flex; }
}

/* ---------- Shared page chrome ---------- */

main {
  flex: 1;
  max-width: 1500px;
  width: 100%;
  margin: 0 auto;
  padding: 18px 18px 48px;
}

.site-footer {
  background: var(--navy);
  color: #cbd2d9;
  margin-top: auto;
}

.footer-back-to-top {
  background: var(--navy-light);
  text-align: center;
  padding: 14px;
  font-size: 0.85rem;
  cursor: pointer;
}

.footer-back-to-top:hover {
  background: #485769;
}

.footer-body {
  text-align: center;
  padding: 20px;
  font-size: 0.8rem;
}

/* ---------- Shared component primitives ---------- */

.btn {
  border: 1px solid transparent;
  border-radius: 100px;
  font-weight: 600;
  font-size: 0.92rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 9px 18px;
  transition: background 0.15s, border-color 0.15s, box-shadow 0.15s;
}

.btn-cart {
  background: linear-gradient(to bottom, #f7dfa5, #f0c14b);
  border-color: #a88734;
  color: #0f1111;
}

.btn-cart:hover {
  background: linear-gradient(to bottom, #f5d78e, #eeb933);
}

.btn-buy {
  background: linear-gradient(to bottom, #f5a04a, #ff9900);
  border-color: #cc7b00;
  color: #0f1111;
}

.btn-buy:hover {
  background: linear-gradient(to bottom, #f0952f, #e88a00);
}

.btn-outline {
  background: white;
  border-color: var(--border);
  color: var(--ink);
}

.btn-outline:hover {
  background: #f7f8f8;
  border-color: #b0b4b4;
}

.btn-danger-outline {
  background: white;
  border-color: var(--danger);
  color: var(--danger);
}

.btn-danger-outline:hover {
  background: var(--danger-bg);
}

.pill {
  border-radius: 100px;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 2px 10px;
  display: inline-flex;
  align-items: center;
}

.pill-success { background: var(--success-bg); color: var(--success); }
.pill-danger { background: var(--danger-bg); color: var(--danger); }

.stars {
  color: var(--star);
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
</style>
