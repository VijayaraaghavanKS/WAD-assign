<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ShoppingCart, Minus, Plus, Trash2, Heart, Truck, Tag, X, ArrowLeft } from 'lucide-vue-next'
import { api } from '../api/client'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { useShopperStore } from '../stores/shopper'
import { getProductImage } from '../utils/productImage'

const cart = useCartStore()
const auth = useAuthStore()
const shopper = useShopperStore()
const router = useRouter()
const route = useRoute()

// Same codes and rules the Order Service uses. Used here only to preview the totals.
const COUPONS = { SAVE10: 10, WELCOME20: 20 }
const FREE_DELIVERY_AT = 5000
const DELIVERY_FEE = 99
const GST_RATE = 18
const PAYMENTS = [['UPI', 'UPI'], ['CARD', 'Card'], ['COD', 'Cash on delivery']]

const couponInput = ref('')
const appliedCoupon = ref('')
const couponError = ref('')
const error = ref('')
const busy = ref(false)
const checkoutOpen = ref(false)
const payment = ref('UPI')
const address = reactive({ name: '', phone: '', line1: '', city: '', state: '', pin: '' })
const addressError = ref('')

const couponSaving = computed(() => Math.round(cart.total * (COUPONS[appliedCoupon.value] ?? 0) / 100))
const afterCoupon = computed(() => cart.total - couponSaving.value)
const deliveryFee = computed(() => (afterCoupon.value >= FREE_DELIVERY_AT ? 0 : DELIVERY_FEE))
const payable = computed(() => afterCoupon.value + deliveryFee.value)
const gstIncluded = computed(() => Math.round(payable.value * GST_RATE / (100 + GST_RATE)))
const toFreeDelivery = computed(() => Math.max(0, FREE_DELIVERY_AT - cart.total))
const freeDeliveryPct = computed(() => Math.min(100, (cart.total / FREE_DELIVERY_AT) * 100))

// Runs a cart change and shows the server's message if it fails.
async function run(change) {
  error.value = ''
  try {
    await change()
  } catch (e) {
    error.value = e.message
  }
}

function applyCoupon() {
  const code = couponInput.value.trim().toUpperCase()
  couponError.value = ''
  if (!COUPONS[code]) return (couponError.value = 'That code is not valid')
  appliedCoupon.value = code
}

// A code from the Offers page arrives as ?coupon=CODE and is applied straight away.
if (route.query.coupon) {
  couponInput.value = String(route.query.coupon)
  applyCoupon()
}

// Checkout is the only step that needs an account. Guests are sent to sign in and come back here.
function openCheckout() {
  if (!auth.isLoggedIn) return router.push({ path: '/login', query: { redirect: '/cart' } })
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
    await api.placeOrder({ coupon: appliedCoupon.value, address: { ...address }, payment: payment.value })
    shopper.saveAddress({ ...address })
    await cart.load()
    router.push('/orders')
  } catch (e) {
    addressError.value = e.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <div class="cart-page">
    <div class="cart-head">
      <h1><ShoppingCart :size="24" /> Your cart <span class="count">{{ cart.itemCount }} {{ cart.itemCount === 1 ? 'item' : 'items' }}</span></h1>
      <RouterLink to="/" class="back"><ArrowLeft :size="15" /> Continue shopping</RouterLink>
    </div>
    <p v-if="error" class="error">{{ error }}</p>

    <section v-if="cart.items.length === 0" class="empty-card">
      <p>Your cart is empty. Add something you love.</p>
      <RouterLink to="/" class="btn btn-primary">Browse the shop</RouterLink>
    </section>

    <div v-else class="cart-grid">
      <ul class="lines">
        <li v-for="item in cart.items" :key="item.id" class="line">
          <RouterLink :to="`/product/${item.productId}`">
            <img :src="getProductImage(item.productName)" :alt="item.productName" />
          </RouterLink>

          <div class="line-main">
            <RouterLink :to="`/product/${item.productId}`" class="line-name">{{ item.productName }}</RouterLink>
            <p v-if="item.size || item.colour" class="meta">{{ [item.size, item.colour].filter(Boolean).join(' · ') }}</p>
            <p class="meta">₹{{ item.price.toLocaleString() }} each</p>

            <div class="line-actions">
              <div class="stepper">
                <button class="icon" aria-label="Decrease" @click="run(() => cart.changeQuantity(item, -1))"><Minus :size="14" /></button>
                <span>{{ item.quantity }}</span>
                <button class="icon" aria-label="Increase" @click="run(() => cart.changeQuantity(item, 1))"><Plus :size="14" /></button>
              </div>
              <button class="text-btn" @click="shopper.toggleWishlist(item.productId)">
                <Heart :size="14" /> {{ shopper.wishlist.includes(item.productId) ? 'Saved' : 'Save for later' }}
              </button>
              <button class="text-btn danger" @click="run(() => cart.remove(item.id))"><Trash2 :size="14" /> Remove</button>
            </div>
          </div>

          <p class="line-total">₹{{ (item.price * item.quantity).toLocaleString() }}</p>
        </li>
      </ul>

      <aside class="summary">
        <h2>Order summary</h2>

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
        <p v-if="couponError" class="error small">{{ couponError }}</p>
        <p v-else-if="appliedCoupon" class="coupon-note">{{ appliedCoupon }} saves ₹{{ couponSaving.toLocaleString() }}</p>

        <div class="sum-row"><span>Subtotal ({{ cart.itemCount }} items)</span><strong>₹{{ cart.total.toLocaleString() }}</strong></div>
        <div v-if="couponSaving" class="sum-row save"><span>Coupon</span><span>-₹{{ couponSaving.toLocaleString() }}</span></div>
        <div class="sum-row"><span>Delivery</span><span>{{ deliveryFee ? '₹' + deliveryFee : 'Free' }}</span></div>
        <div class="sum-row total"><span>To pay</span><strong>₹{{ payable.toLocaleString() }}</strong></div>
        <p class="gst">Includes GST of ₹{{ gstIncluded.toLocaleString() }}</p>
        <p v-if="!auth.isLoggedIn" class="guest-note">Your cart is saved in this browser. Sign in at checkout to place the order.</p>
        <button class="btn btn-primary wide" :disabled="busy" @click="openCheckout">
          {{ auth.isLoggedIn ? 'Proceed to checkout' : 'Sign in to check out' }}
        </button>
      </aside>
    </div>

    <div v-if="checkoutOpen" class="modal-backdrop" @click.self="checkoutOpen = false">
      <form class="modal" role="dialog" aria-label="Checkout" @submit.prevent="placeOrder">
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
        <p v-if="addressError" class="error small">{{ addressError }}</p>

        <h3>Payment</h3>
        <div class="payments">
          <label v-for="[value, label] in PAYMENTS" :key="value" :class="{ on: payment === value }">
            <input type="radio" name="payment" :value="value" v-model="payment" /> {{ label }}
          </label>
        </div>

        <div class="modal-total"><span>To pay</span><strong>₹{{ payable.toLocaleString() }}</strong></div>
        <button class="btn btn-primary wide" :disabled="busy" type="submit">{{ busy ? 'Placing order...' : 'Place order' }}</button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.cart-head { display: flex; justify-content: space-between; align-items: flex-end; flex-wrap: wrap; gap: 12px; margin-bottom: 18px; }
.cart-head h1 { display: flex; align-items: center; gap: 10px; margin: 0; font-size: 1.7rem; }
.count { font-family: 'Instrument Sans', system-ui, sans-serif; font-size: 0.9rem; color: var(--ink-soft); font-weight: 600; }
.back { display: inline-flex; align-items: center; gap: 6px; color: var(--link); font-weight: 600; text-decoration: none; }
.error { color: var(--danger); margin: 0; }
.error.small { font-size: 0.86rem; }

.empty-card {
  background: var(--paper); border-radius: 16px; padding: 40px; box-shadow: var(--shadow-card);
  display: grid; gap: 14px; justify-items: center; text-align: center;
}

.cart-grid { display: grid; grid-template-columns: minmax(0, 1fr) 340px; gap: 22px; align-items: start; }

.lines { list-style: none; margin: 0; padding: 0; display: grid; gap: 14px; }
.line {
  display: grid; grid-template-columns: 120px minmax(0, 1fr) auto; gap: 18px; align-items: start;
  background: var(--paper); border-radius: 16px; padding: 14px; box-shadow: var(--shadow-card);
}
.line img { width: 120px; height: 120px; object-fit: cover; border-radius: 12px; background: #efe6d8; display: block; }
.line-name { font-family: 'Fraunces', Georgia, serif; font-weight: 700; font-size: 1.1rem; color: var(--ink); text-decoration: none; }
.line-name:hover { text-decoration: underline; }
.meta { margin: 4px 0 0; font-size: 0.86rem; color: var(--ink-soft); }
.line-actions { display: flex; align-items: center; gap: 14px; margin-top: 12px; flex-wrap: wrap; }
.stepper { display: inline-flex; align-items: center; gap: 10px; font-weight: 600; }
.line-total { margin: 0; font-weight: 700; font-size: 1.1rem; white-space: nowrap; }

.icon {
  border: 1px solid var(--border); background: #fff; border-radius: 6px;
  width: 28px; height: 28px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer;
}
.text-btn {
  background: none; border: none; font: inherit; font-size: 0.84rem; color: var(--ink-soft); cursor: pointer;
  display: inline-flex; align-items: center; gap: 5px; padding: 4px 6px;
}
.text-btn:hover { color: var(--ink); }
.text-btn.danger { color: var(--danger); }

.summary {
  background: var(--paper); border-radius: 16px; padding: 20px; box-shadow: var(--shadow-card);
  display: grid; gap: 12px; position: sticky; top: 130px; min-width: 0;
}
.summary h2 { margin: 0; font-size: 1.2rem; }
.delivery-meter p { display: flex; align-items: center; gap: 6px; margin: 0 0 6px; font-size: 0.84rem; color: var(--ink-soft); }
.bar { height: 6px; background: var(--border); border-radius: 999px; overflow: hidden; }
.bar span { display: block; height: 100%; background: var(--olive); transition: width 0.3s; }

/* Fixed row: the input shrinks, the button keeps its size, so nothing spills past the card. */
.coupon { display: flex; align-items: center; gap: 6px; color: var(--ink-soft); min-width: 0; }
.coupon input {
  flex: 1; min-width: 0; width: 100%; border: 1px solid var(--border); border-radius: var(--radius-sm);
  padding: 8px 10px; font: inherit; text-transform: uppercase;
}
.coupon .btn { flex: none; padding: 8px 16px; }
.coupon-note { margin: 0; font-size: 0.82rem; color: var(--olive); font-weight: 600; }
.sum-row { display: flex; justify-content: space-between; gap: 10px; font-size: 0.95rem; }
.sum-row.save { color: var(--olive); }
.sum-row.total { border-top: 1px solid var(--border); padding-top: 10px; font-size: 1.05rem; }
.gst { margin: -4px 0 0; font-size: 0.78rem; color: var(--ink-soft); text-align: right; }
.guest-note { margin: 0; font-size: 0.84rem; color: var(--ink-soft); }
.wide { width: 100%; }

.modal-backdrop {
  position: fixed; inset: 0; background: rgba(43, 33, 27, 0.5); z-index: 70;
  display: flex; align-items: center; justify-content: center; padding: 16px;
}
.modal { background: var(--paper); border-radius: 16px; padding: 20px; width: min(520px, 100%); box-shadow: var(--shadow-card); display: grid; gap: 12px; }
.modal header { display: flex; justify-content: space-between; align-items: center; }
.modal h2 { margin: 0; font-size: 1.3rem; }
.modal h3 { margin: 6px 0 0; font-size: 0.95rem; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.form-grid .full { grid-column: 1 / -1; }
.modal input { font: inherit; border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 9px 10px; min-width: 0; }
.payments { display: flex; gap: 8px; flex-wrap: wrap; }
.payments label { border: 1px solid var(--border); border-radius: 999px; padding: 7px 14px; cursor: pointer; font-weight: 600; font-size: 0.9rem; }
.payments label.on { border-color: var(--accent); background: #f6e7cf; }
.payments input { margin-right: 4px; accent-color: var(--accent); }
.modal-total { display: flex; justify-content: space-between; font-size: 1.05rem; border-top: 1px solid var(--border); padding-top: 10px; }

@media (max-width: 900px) {
  .cart-grid { grid-template-columns: 1fr; }
  .summary { position: static; }
}
@media (max-width: 560px) {
  .line { grid-template-columns: 84px minmax(0, 1fr); }
  .line img { width: 84px; height: 84px; }
  .line-total { grid-column: 2; }
  .form-grid { grid-template-columns: 1fr; }
}
</style>
