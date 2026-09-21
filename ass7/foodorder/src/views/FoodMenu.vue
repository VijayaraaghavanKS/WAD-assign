<script setup>
import { useCartStore } from '../stores/cart'

const cart = useCartStore()

// Static food list for the menu. In a real app this would come from an API.
const foods = [
  { id: 1, name: 'Pizza', emoji: '🍕', price: 250 },
  { id: 2, name: 'Burger', emoji: '🍔', price: 150 },
  { id: 3, name: 'Pasta', emoji: '🍝', price: 200 },
  { id: 4, name: 'Coke', emoji: '🥤', price: 80 }
]
</script>

<template>
  <div>
    <h3>Available Food</h3>
    <ul class="menu-list">
      <li v-for="food in foods" :key="food.id">
        <span>{{ food.emoji }} {{ food.name }}</span>
        <span>₹{{ food.price }}</span>
        <button @click="cart.addToCart(food)">Add to Cart</button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.menu-list {
  list-style: none;
  padding: 0;
  max-width: 400px;
}

.menu-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.menu-list button {
  padding: 4px 10px;
  cursor: pointer;
}
</style>
