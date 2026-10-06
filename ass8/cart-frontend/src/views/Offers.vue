<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Copy, Check, Ticket, Truck, Wallet, ArrowRight } from 'lucide-vue-next'

// Only codes the Order Service really accepts. Each one shows its terms in plain words.
const coupons = [
  { code: 'SAVE10', percent: 10, title: '10% off your order', terms: ['Works on any order', 'Taken off the subtotal, before delivery and GST'] },
  { code: 'WELCOME20', percent: 20, title: '20% off your order', terms: ['Works on any order', 'Taken off the subtotal, before delivery and GST'] },
]

// Always-on terms, so shoppers know what they pay before checkout.
const standing = [
  { icon: Truck, title: 'Free delivery over ₹5,000', text: 'Orders under ₹5,000 pay ₹99 delivery. The cart meter shows how far you are.' },
  { icon: Wallet, title: 'Cash on delivery', text: 'Pay UPI, card or cash when your order arrives.' },
  { icon: Ticket, title: 'GST shown inside the price', text: 'Every total shows how much of it is GST, so nothing is added at the end.' },
]

const copied = ref('')

// Copies the code. If the browser blocks clipboard access, the code is still on screen to copy by hand.
async function copy(code) {
  try {
    await navigator.clipboard.writeText(code)
    copied.value = code
    setTimeout(() => (copied.value = ''), 2000)
  } catch {
    copied.value = ''
  }
}
</script>

<template>
  <div class="offers">
    <section class="hero">
      <p class="eyebrow">Offers and coupons</p>
      <h1>Copy a code, <em>see the saving</em> before you pay.</h1>
      <p>Codes work at the cart. Paste one there and the new total appears before you check out.</p>
    </section>

    <h2>Coupon codes</h2>
    <div class="tickets">
      <article v-for="c in coupons" :key="c.code" class="ticket">
        <div class="stub">
          <span class="percent">{{ c.percent }}%</span>
          <span class="off">off</span>
        </div>
        <div class="body">
          <h3>{{ c.title }}</h3>
          <ul>
            <li v-for="t in c.terms" :key="t">{{ t }}</li>
          </ul>
          <div class="code-row">
            <code>{{ c.code }}</code>
            <button class="btn btn-outline" :aria-label="`Copy code ${c.code}`" @click="copy(c.code)">
              <Check v-if="copied === c.code" :size="15" /><Copy v-else :size="15" />
              {{ copied === c.code ? 'Copied' : 'Copy code' }}
            </button>
          </div>
          <RouterLink :to="{ path: '/cart', query: { coupon: c.code } }" class="use">
            Use at cart <ArrowRight :size="15" />
          </RouterLink>
        </div>
      </article>
    </div>

    <h2>Every order</h2>
    <div class="standing">
      <article v-for="s in standing" :key="s.title" class="term">
        <component :is="s.icon" :size="20" />
        <h3>{{ s.title }}</h3>
        <p>{{ s.text }}</p>
      </article>
    </div>

    <section class="steps">
      <h2>How a code works</h2>
      <ol>
        <li><strong>Copy</strong> a code from this page.</li>
        <li><strong>Paste</strong> it in your cart. Codes are not case-sensitive.</li>
        <li><strong>Check</strong> the saving in the summary. Then check out, which needs a sign-in.</li>
      </ol>
      <p class="note">One code applies per order. Sale prices on products are already in the price you see.</p>
    </section>
  </div>
</template>

<style scoped>
.offers { display: grid; gap: 18px; }
.hero {
  background: linear-gradient(120deg, #ead9c2, #f6ece0); border-radius: 20px; padding: 40px 44px;
}
.eyebrow { text-transform: uppercase; letter-spacing: 0.14em; font-size: 0.76rem; font-weight: 700; color: var(--olive); margin: 0 0 10px; }
.hero h1 { font-size: clamp(1.8rem, 3.6vw, 2.8rem); line-height: 1.08; margin: 0 0 12px; }
.hero h1 em { color: var(--accent); }
.hero p:last-child { color: var(--ink-soft); max-width: 560px; margin: 0; }
h2 { font-size: 1.3rem; margin: 10px 0 0; }

.tickets { display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 18px; }
.ticket {
  display: grid; grid-template-columns: 120px minmax(0, 1fr); background: var(--paper);
  border-radius: 16px; overflow: hidden; box-shadow: var(--shadow-card);
}
.stub {
  background: var(--navy-dark); color: var(--paper); display: flex; flex-direction: column;
  align-items: center; justify-content: center; border-right: 2px dashed var(--border);
}
.percent { font-family: 'Fraunces', Georgia, serif; font-size: 2.4rem; font-weight: 700; color: var(--accent); line-height: 1; }
.off { text-transform: uppercase; letter-spacing: 0.12em; font-size: 0.78rem; font-weight: 700; }
.body { padding: 18px; display: grid; gap: 10px; min-width: 0; }
.body h3 { margin: 0; font-size: 1.1rem; }
.body ul { margin: 0; padding-left: 18px; color: var(--ink-soft); font-size: 0.88rem; display: grid; gap: 3px; }
.code-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
code {
  font-family: ui-monospace, Menlo, monospace; font-weight: 700; letter-spacing: 0.08em;
  background: var(--bg); border: 1px dashed var(--accent); border-radius: var(--radius-sm); padding: 7px 12px;
}
.code-row .btn { padding: 7px 14px; font-size: 0.86rem; }
.use { display: inline-flex; align-items: center; gap: 6px; color: var(--link); font-weight: 600; text-decoration: none; }
.use:hover { text-decoration: underline; }

.standing { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 14px; }
.term { background: var(--paper); border-radius: 14px; padding: 18px; box-shadow: var(--shadow-card); display: grid; gap: 8px; align-content: start; }
.term svg { color: var(--olive); }
.term h3 { margin: 0; font-size: 1rem; }
.term p { margin: 0; color: var(--ink-soft); font-size: 0.9rem; }

.steps { background: var(--paper); border-radius: 16px; padding: 22px; box-shadow: var(--shadow-card); }
.steps h2 { margin-top: 0; }
.steps ol { margin: 0; padding-left: 20px; display: grid; gap: 6px; }
.note { color: var(--ink-soft); font-size: 0.88rem; margin: 12px 0 0; }
</style>
