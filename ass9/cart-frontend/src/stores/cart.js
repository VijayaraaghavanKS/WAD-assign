import { defineStore } from 'pinia'
import { api } from '../api/client'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    total: 0,
  }),

  getters: {
    itemCount: (state) => state.items.reduce((sum, item) => sum + item.quantity, 0),
  },

  actions: {
    async load() {
      this.items = await api.getCart()
      this.total = this.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
    },

    async add(productId) {
      await api.addToCart(productId)
      await this.load()
    },

    async changeQuantity(item, delta) {
      const newQty = item.quantity + delta
      if (newQty <= 0) {
        await this.remove(item.id)
        return
      }
      await api.updateQuantity(item.id, newQty)
      await this.load()
    },

    async remove(id) {
      await api.removeItem(id)
      await this.load()
    },

    async clear() {
      await api.clearCart()
      await this.load()
    },
  },
})
