import { defineStore } from 'pinia'
import { api } from '../api/client'
import { useAuthStore } from './auth'

// Guests keep their cart in this browser. It moves to the server cart on sign-in.
const GUEST_KEY = 'shopcart-guest-cart'

const readGuest = () => {
  try {
    return JSON.parse(localStorage.getItem(GUEST_KEY)) ?? []
  } catch {
    return []
  }
}

const writeGuest = (items) => {
  try {
    localStorage.setItem(GUEST_KEY, JSON.stringify(items))
  } catch {
    // Storage can be blocked; the cart still works until the page closes.
  }
}

const signedIn = () => useAuthStore().isLoggedIn

// Same rule the cart services apply, so guests and shoppers get the same message.
const requireStock = (product, wanted) => {
  if (wanted > product.quantity) {
    throw new Error(product.quantity === 0 ? 'Out of stock' : `Only ${product.quantity} left`)
  }
}

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
      this.items = signedIn() ? (await api.getCart()) ?? [] : readGuest()
    },

    // Adds one unit. Guests get the same line shape the server returns, with the sale price.
    async add(productId, variant) {
      if (signedIn()) {
        await api.addToCart(productId, variant)
        return this.load()
      }
      const product = await api.getProduct(productId)
      const size = variant?.size ?? null
      const colour = variant?.colour ?? null
      const line = this.items.find((i) => i.productId === productId && i.size === size && i.colour === colour)
      requireStock(product, (line?.quantity ?? 0) + 1)
      if (line) line.quantity += 1
      else {
        this.items.push({
          id: crypto.randomUUID(),
          productId,
          productName: product.name,
          price: Math.round(product.price * (100 - (product.discountPercent || 0)) / 100),
          quantity: 1,
          size,
          colour,
        })
      }
      writeGuest(this.items)
    },

    async changeQuantity(item, delta) {
      const newQty = item.quantity + delta
      if (newQty <= 0) return this.remove(item.id)
      if (!signedIn()) {
        if (delta > 0) requireStock(await api.getProduct(item.productId), newQty)
        item.quantity = newQty
        return writeGuest(this.items)
      }
      await api.updateQuantity(item.id, newQty)
      await this.load()
    },

    async remove(id) {
      if (!signedIn()) {
        this.items = this.items.filter((i) => i.id !== id)
        return writeGuest(this.items)
      }
      await api.removeItem(id)
      await this.load()
    },

    // Called on sign-in: replays the guest lines into the shopper's server cart.
    async mergeGuest() {
      const lines = readGuest()
      for (const line of lines) {
        // A line that sold out while the guest was away is skipped, not retried.
        for (let i = 0; i < line.quantity; i++) {
          try {
            await api.addToCart(line.productId, { size: line.size, colour: line.colour })
          } catch {
            break
          }
        }
      }
      localStorage.removeItem(GUEST_KEY)
    },
  },
})
