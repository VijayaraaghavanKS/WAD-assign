import { defineStore } from 'pinia'

// Pinia store holding the shopping cart state, shared by FoodMenu.vue and Cart.vue.
export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] // each item: { id, name, emoji, price, quantity }
  }),

  getters: {
    totalItems: (state) => state.items.reduce((sum, item) => sum + item.quantity, 0),
    totalAmount: (state) => state.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
  },

  actions: {
    // Adds a food item to the cart, or increases quantity if it's already there.
    addToCart(food) {
      const existing = this.items.find((item) => item.id === food.id)
      if (existing) {
        existing.quantity += 1
      } else {
        this.items.push({ ...food, quantity: 1 })
      }
    },

    increaseQuantity(id) {
      const item = this.items.find((item) => item.id === id)
      if (item) item.quantity += 1
    },

    decreaseQuantity(id) {
      const item = this.items.find((item) => item.id === id)
      if (item) {
        item.quantity -= 1
        if (item.quantity <= 0) this.removeFromCart(id)
      }
    },

    removeFromCart(id) {
      this.items = this.items.filter((item) => item.id !== id)
    },

    clearCart() {
      this.items = []
    }
  }
})
