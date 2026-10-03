<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'

const orders = ref([])
const error = ref('')

onMounted(async () => {
  try {
    orders.value = (await api.getOrders()) ?? []
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <section class="orders">
    <h1>Your orders</h1>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="orders.length === 0" class="empty">No orders yet. Your purchases will show up here.</p>

    <article v-for="o in orders" :key="o.id" class="order">
      <header>
        <div>
          <strong>Order {{ o.id.slice(-6).toUpperCase() }}</strong>
          <span class="when">{{ new Date(o.placedAt).toLocaleString() }}</span>
        </div>
        <span class="total">₹{{ o.total.toLocaleString() }}</span>
      </header>
      <ul>
        <li v-for="line in o.items" :key="line.productId">
          <span>{{ line.productName }} &times; {{ line.quantity }}</span>
          <span>₹{{ (line.price * line.quantity).toLocaleString() }}</span>
        </li>
      </ul>
    </article>
  </section>
</template>

<style scoped>
h1 { margin: 0 0 18px; font-size: 2rem; }
.order { background: var(--paper); border-radius: 14px; padding: 18px 20px; box-shadow: var(--shadow-card); margin-bottom: 14px; }
.order header { display: flex; justify-content: space-between; align-items: baseline; gap: 12px; border-bottom: 1px solid var(--border); padding-bottom: 10px; }
.when { margin-left: 12px; color: var(--ink-soft); font-size: 0.86rem; }
.total { font-weight: 700; }
ul { list-style: none; padding: 0; margin: 10px 0 0; display: grid; gap: 6px; }
li { display: flex; justify-content: space-between; font-size: 0.94rem; }
.empty, .error { color: var(--ink-soft); }
.error { color: var(--danger); }
</style>
