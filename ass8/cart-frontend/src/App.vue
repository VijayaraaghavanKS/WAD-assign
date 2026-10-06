<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { ShoppingCart, ShoppingBag, LogOut, Package, ShieldCheck, Activity, Tag } from 'lucide-vue-next'
import { useCartStore } from './stores/cart'
import { useAuthStore } from './stores/auth'
import { useShopperStore } from './stores/shopper'

const cart = useCartStore()
const auth = useAuthStore()
const shopper = useShopperStore()
const router = useRouter()
const route = useRoute()
const floatOpen = ref(false)

// Guests and shoppers see the cart. Admins and developers do not.
const isShopper = computed(() => !auth.isLoggedIn || auth.role === 'USER')
// The floating cart shows on every shopper page except the cart page itself.
const showFloatingCart = computed(() => isShopper.value && route.path !== '/cart')

// On sign-in, guest lines move into the server cart. Then the cart reloads for either state.
watch(() => auth.isLoggedIn, async (loggedIn) => {
  if (loggedIn) await cart.mergeGuest().catch(() => {})
  cart.load()
  if (loggedIn) await shopper.sync().catch(() => {})
  else shopper.reset()
}, { immediate: true })

// Links each role can see. Shop and Offers are open to everyone; the rest need a role.
const shopLinks = [['/', ShoppingBag, 'Shop'], ['/offers', Tag, 'Offers']]
const links = computed(() => {
  const role = auth.role
  const extra = { USER: [['/orders', Package, 'My orders']], ADMIN: [['/admin', ShieldCheck, 'Admin']], DEVELOPER: [['/dev', Activity, 'Developer']] }
  if (!auth.isLoggedIn) return shopLinks
  return role === 'USER' ? [...shopLinks, ...extra.USER] : (extra[role] ?? [])
})

function logout() {
  auth.logout()
  cart.items = []
  router.push('/login')
}
</script>

<template>
  <div id="shell">
    <header class="masthead">
      <div class="masthead-row">
        <RouterLink to="/" class="brand">
          <ShoppingBag :size="24" />
          <span>Maison<em>Cart</em></span>
        </RouterLink>

        <div class="spacer" />

        <RouterLink v-if="isShopper" to="/cart" class="cart-link">
          <ShoppingCart :size="22" />
          <span>Cart</span>
          <span class="cart-badge">{{ cart.itemCount }}</span>
        </RouterLink>

        <template v-if="auth.isLoggedIn">
          <div class="session">
            <span class="avatar">{{ auth.session.username[0].toUpperCase() }}</span>
            <div>
              <div class="session-name">{{ auth.session.username }}</div>
              <div class="session-role">{{ auth.role.toLowerCase() }}</div>
            </div>
            <button type="button" class="logout" title="Log out" @click="logout"><LogOut :size="16" /></button>
          </div>
        </template>
        <RouterLink v-else to="/login" class="sign-in">Sign in</RouterLink>
      </div>

      <nav class="subnav">
        <RouterLink v-for="[to, Icon, label] in links" :key="to" :to="to" exact-active-class="active">
          <component :is="Icon" :size="15" /> {{ label }}
        </RouterLink>
      </nav>
    </header>

    <main>
      <RouterView />
    </main>

    <aside v-if="showFloatingCart" class="float-cart">
      <div v-if="floatOpen" class="float-panel" role="dialog" aria-label="Quick cart">
        <p v-if="cart.items.length === 0" class="float-empty">Your cart is empty.</p>
        <ul v-else>
          <li v-for="item in cart.items" :key="item.id">
            <span>{{ item.productName }} × {{ item.quantity }}</span>
            <span>₹{{ (item.price * item.quantity).toLocaleString() }}</span>
          </li>
        </ul>
        <div class="float-total"><span>Total</span><strong>₹{{ cart.total.toLocaleString() }}</strong></div>
        <RouterLink to="/cart" class="btn btn-primary" @click="floatOpen = false">View cart</RouterLink>
      </div>
      <button class="float-btn" :aria-expanded="floatOpen" @click="floatOpen = !floatOpen">
        <ShoppingCart :size="18" /> {{ cart.itemCount }} · ₹{{ cart.total.toLocaleString() }}
      </button>
    </aside>

    <footer class="site-footer">
      MaisonCart &mdash; ICS1511 Web Application Development Laboratory
      &middot; <RouterLink to="/credits">Photo credits</RouterLink>
    </footer>
  </div>
</template>

<style>
:root {
  --ink: #2b211b;
  --ink-soft: #6e5f55;
  --paper: #fbf7f0;
  --bg: #f4ede3;
  --navy-dark: #2b211b;
  --navy: #3a2d25;
  --navy-light: #4d3e34;
  --accent: #c2562f;
  --accent-dark: #a8431f;
  --olive: #6b7144;
  --link: #8a3b1e;
  --price: #2b211b;
  --star: #d9822b;
  --success: #4f5b2e;
  --success-bg: #e6ebd6;
  --danger: #b3261e;
  --danger-bg: #f9e3df;
  --border: #dccfbe;
  --radius: 12px;
  --radius-sm: 6px;
  --shadow-card: 0 1px 2px rgba(43, 33, 27, 0.08), 0 4px 14px rgba(43, 33, 27, 0.06);
}

* { box-sizing: border-box; }

html, body { margin: 0; }

body {
  font-family: 'Instrument Sans', system-ui, sans-serif;
  background: var(--bg);
  color: var(--ink);
  -webkit-font-smoothing: antialiased;
}

h1, h2, h3, .brand { font-family: 'Fraunces', Georgia, serif; }

#shell { min-height: 100vh; display: flex; flex-direction: column; }

::selection { background: var(--accent); color: #fff; }

a:focus-visible, button:focus-visible, input:focus-visible, select:focus-visible {
  outline: 3px solid var(--olive);
  outline-offset: 2px;
}

.masthead { position: sticky; top: 0; z-index: 40; }

.masthead-row {
  background: var(--navy-dark);
  color: var(--paper);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 28px;
}

.spacer { flex: 1; }

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--paper);
  text-decoration: none;
  font-size: 1.4rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.brand em { font-style: italic; color: var(--accent); }

.cart-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  color: var(--paper);
  text-decoration: none;
  font: inherit;
  font-weight: 600;
  padding: 8px 12px;
}

/* Bottom-right cart pill, with a quick preview that opens above it. */
.float-cart { position: fixed; right: 24px; bottom: 24px; z-index: 60; display: grid; justify-items: end; gap: 10px; }
.float-btn {
  display: inline-flex; align-items: center; gap: 8px; border: none; cursor: pointer;
  background: var(--navy-dark); color: var(--paper); font: inherit; font-weight: 700;
  padding: 12px 18px; border-radius: 999px; box-shadow: 0 10px 30px rgba(43, 33, 27, 0.3);
}
.float-panel {
  width: min(320px, calc(100vw - 32px)); background: var(--paper); border-radius: 16px; padding: 16px;
  box-shadow: 0 10px 30px rgba(43, 33, 27, 0.25); display: grid; gap: 10px;
}
.float-panel ul { list-style: none; margin: 0; padding: 0; display: grid; gap: 6px; font-size: 0.9rem; max-height: 240px; overflow-y: auto; }
.float-panel li { display: flex; justify-content: space-between; gap: 10px; }
.float-total { display: flex; justify-content: space-between; border-top: 1px solid var(--border); padding-top: 10px; font-weight: 700; }
.float-empty { margin: 0; color: var(--ink-soft); }


.cart-link:hover { border-color: rgba(251, 247, 240, 0.4); }
.cart-link.router-link-active { border-color: var(--accent); }
.sign-in {
  color: var(--navy-dark); background: var(--paper); text-decoration: none; font-weight: 700;
  padding: 8px 18px; border-radius: 999px;
}

.cart-badge {
  background: var(--accent);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 700;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
}

.session { display: flex; align-items: center; gap: 10px; padding-left: 14px; border-left: 1px solid rgba(251, 247, 240, 0.18); }

.avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: var(--accent); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700;
}

.session-name { font-weight: 600; font-size: 0.92rem; line-height: 1.1; }
.session-role { font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.08em; color: #d8c7b3; }

.logout {
  background: none; border: 1px solid rgba(251, 247, 240, 0.3); color: var(--paper);
  border-radius: var(--radius-sm); padding: 6px; cursor: pointer; display: flex;
}
.logout:hover { background: var(--navy-light); }

.subnav {
  background: var(--navy);
  display: flex;
  gap: 4px;
  padding: 0 22px;
}

.subnav a {
  color: #e9dccb;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 600;
  padding: 11px 14px;
  display: flex;
  align-items: center;
  gap: 7px;
  border-bottom: 3px solid transparent;
}

.subnav a:hover { background: var(--navy-light); }
.subnav a.active { color: #fff; border-bottom-color: var(--accent); }

main {
  flex: 1;
  max-width: 1320px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 24px 56px;
}

.site-footer a { color: #e9dccb; }

.site-footer {
  background: var(--navy-dark);
  color: #cbb9a4;
  text-align: center;
  padding: 22px;
  font-size: 0.82rem;
  margin-top: auto;
}

/* Shared buttons and pills used by every page */
.btn {
  border: 1px solid transparent;
  border-radius: 999px;
  font: inherit;
  font-weight: 600;
  font-size: 0.92rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 10px 20px;
  transition: background 0.15s, border-color 0.15s;
}

.btn-primary { background: var(--accent); color: #fff; }
.btn-primary:hover { background: var(--accent-dark); }

.btn-outline { background: #fff; border-color: var(--border); color: var(--ink); }
.btn-outline:hover { border-color: var(--ink-soft); }

.btn-danger-outline { background: #fff; border-color: var(--danger); color: var(--danger); }
.btn-danger-outline:hover { background: var(--danger-bg); }

.pill {
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 3px 10px;
  display: inline-flex;
  align-items: center;
}

.pill-success { background: var(--success-bg); color: var(--success); }
.pill-danger { background: var(--danger-bg); color: var(--danger); }
.pill-warn { background: #f6e7cf; color: var(--accent-dark); }

@media print { .masthead, .site-footer, .no-print { display: none !important; } body { background: #fff; } }

.stars { color: var(--star); display: inline-flex; align-items: center; gap: 2px; }
</style>
