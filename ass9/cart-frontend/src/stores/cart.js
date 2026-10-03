import { defineStore } from 'pinia'
import { api } from '../api/client'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
  }),

  getters: {
    itemCount: (state) => state.items.reduce((sum, item) => sum + item.quantity, 0),
    total: (state) => state.items.reduce((sum, item) => sum + item.price * item.quantity, 0),
  },

  actions: {
    async load() {
      this.items = (await api.getCart()) ?? []
    },

    async add(productId) {
      await api.addToCart(productId)
      await this.load()
    },

    async changeQuantity(item, delta) {
      const newQty = item.quantity + delta
      if (newQty <= 0) return this.remove(item.id)
      await api.updateQuantity(item.id, newQty)
      await this.load()
    },

    async remove(id) {
      await api.removeItem(id)
      await this.load()
    },
  },
})
