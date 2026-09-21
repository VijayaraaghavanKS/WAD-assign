<script setup>
import { useCartStore } from '../stores/cart'

const cart = useCartStore()
</script>

<template>
  <div>
    <h3>🛒 Cart ({{ cart.totalItems }} items)</h3>

    <p v-if="cart.items.length === 0">Your cart is empty.</p>

    <ul v-else class="cart-list">
      <li v-for="item in cart.items" :key="item.id">
        <span class="name">{{ item.emoji }} {{ item.name }}</span>
        <button @click="cart.decreaseQuantity(item.id)">-</button>
        <span>{{ item.quantity }}</span>
        <button @click="cart.increaseQuantity(item.id)">+</button>
        <span class="line-total">₹{{ item.price * item.quantity }}</span>
        <button @click="cart.removeFromCart(item.id)">Remove</button>
      </li>
    </ul>

    <p v-if="cart.items.length > 0" class="total">Total: ₹{{ cart.totalAmount }}</p>

    <button v-if="cart.items.length > 0" @click="cart.clearCart()">Clear Cart</button>
  </div>
</template>

<style scoped>
.cart-list {
  list-style: none;
  padding: 0;
  max-width: 500px;
}

.cart-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.name {
  flex: 1;
}

.line-total {
  width: 70px;
  text-align: right;
}

.total {
  font-weight: bold;
  font-size: 1.1rem;
}
</style>
