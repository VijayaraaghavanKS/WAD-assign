<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const mode = ref('signin') // 'signin' or 'register'
const username = ref('')
const password = ref('')
const error = ref('')
const busy = ref(false)

// Demo accounts seeded by User Service. Every demo password is demo123.
const demoAccounts = [
  { role: 'Admin', username: 'admin', hint: 'Manage products and stock' },
  { role: 'Developer', username: 'developer', hint: 'Live service map and logs' },
]
const shopperAccounts = ['shopper', 'shopper_asha', 'shopper_ravi', 'shopper_meera', 'shopper_kiran', 'shopper_divya', 'shopper_arjun', 'shopper_priya', 'shopper_sahil']

function useDemo(name) {
  mode.value = 'signin'
  username.value = name
  password.value = 'demo123'
}

async function submit() {
  error.value = ''
  busy.value = true
  try {
    if (mode.value === 'register') await auth.register(username.value, password.value)
    else await auth.login(username.value, password.value)
    router.push(route.query.redirect || '/')
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="login">
    <div class="login-hero">
      <p class="eyebrow">Welcome to MaisonCart</p>
      <h1>Everyday things, <em>considered.</em></h1>
      <p class="lede">A small store built from four microservices. Sign in as a role to see what each one can do.</p>
    </div>

    <div class="login-card">
      <div class="tabs">
        <button :class="{ on: mode === 'signin' }" @click="mode = 'signin'">Sign in</button>
        <button :class="{ on: mode === 'register' }" @click="mode = 'register'">Create account</button>
      </div>

      <form @submit.prevent="submit">
        <label>Username <input v-model="username" autocomplete="username" required /></label>
        <label>Password <input v-model="password" type="password" autocomplete="current-password" required /></label>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="btn btn-primary wide" :disabled="busy">{{ mode === 'signin' ? 'Sign in' : 'Create account' }}</button>
      </form>

      <div class="demo">
        <span>Demo staff accounts</span>
        <button v-for="a in demoAccounts" :key="a.username" type="button" class="demo-chip" @click="useDemo(a.username)">
          <strong>{{ a.role }}</strong>
          <small>{{ a.hint }}</small>
        </button>
      </div>

      <div class="demo">
        <span>Demo shopper accounts · password demo123</span>
        <div class="shoppers">
          <button v-for="name in shopperAccounts" :key="name" type="button" class="shopper-chip" @click="useDemo(name)">{{ name }}</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.login {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 48px;
  align-items: center;
  min-height: 70vh;
}

.eyebrow { text-transform: uppercase; letter-spacing: 0.14em; font-size: 0.78rem; color: var(--olive); font-weight: 700; }

h1 { font-size: clamp(2.4rem, 5vw, 4rem); line-height: 1.02; margin: 8px 0 14px; }
h1 em { color: var(--accent); }

.lede { color: var(--ink-soft); font-size: 1.05rem; max-width: 460px; line-height: 1.6; }

.login-card {
  background: var(--paper);
  border-radius: 18px;
  padding: 28px;
  box-shadow: var(--shadow-card);
}

.tabs { display: flex; gap: 6px; margin-bottom: 18px; background: var(--bg); padding: 4px; border-radius: 999px; }
.tabs button { flex: 1; border: none; background: none; padding: 9px; border-radius: 999px; font: inherit; font-weight: 600; color: var(--ink-soft); cursor: pointer; }
.tabs button.on { background: #fff; color: var(--ink); box-shadow: var(--shadow-card); }

form { display: grid; gap: 14px; }
label { display: grid; gap: 6px; font-weight: 600; font-size: 0.88rem; }
input {
  font: inherit; padding: 11px 13px; border: 1px solid var(--border);
  border-radius: var(--radius-sm); background: #fff;
}

.error { color: var(--danger); font-size: 0.88rem; margin: 0; }
.wide { width: 100%; padding: 12px; }
button:disabled { opacity: 0.6; cursor: wait; }

.demo { margin-top: 22px; border-top: 1px dashed var(--border); padding-top: 18px; display: grid; gap: 8px; }
.demo > span { font-size: 0.8rem; color: var(--ink-soft); font-weight: 600; text-transform: uppercase; letter-spacing: 0.08em; }
.demo-chip {
  display: flex; flex-direction: column; text-align: left; gap: 2px;
  background: var(--bg); border: 1px solid transparent; border-radius: var(--radius-sm);
  padding: 10px 12px; font: inherit; cursor: pointer;
}
.demo-chip:hover { border-color: var(--accent); }
.demo-chip small { color: var(--ink-soft); }
.shoppers { display: flex; flex-wrap: wrap; gap: 6px; }
.shopper-chip {
  font: inherit; font-size: 0.82rem; background: #fff; border: 1px solid var(--border);
  border-radius: 999px; padding: 5px 11px; cursor: pointer;
}
.shopper-chip:hover { border-color: var(--accent); }

@media (max-width: 820px) {
  .login { grid-template-columns: 1fr; }
}
</style>
