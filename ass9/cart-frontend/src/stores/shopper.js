import { defineStore } from 'pinia'

const KEY = 'shopcart-shopper'
const MAX_COMPARE = 3
const MAX_RECENT = 4

// Shopper-only extras kept in this browser: saved items, recently viewed, and
// the compare tray. Stored as product ids so a price change is always fresh.
export const useShopperStore = defineStore('shopper', {
  state: () => ({ ...read() }),

  actions: {
    toggleWishlist(id) {
      this.wishlist = this.wishlist.includes(id) ? this.wishlist.filter((x) => x !== id) : [...this.wishlist, id]
      save(this)
    },

    markViewed(id) {
      this.recent = [id, ...this.recent.filter((x) => x !== id)].slice(0, MAX_RECENT)
      save(this)
    },

    // Returns false when the tray is full, so the caller can say why.
    toggleCompare(id) {
      if (this.compare.includes(id)) this.compare = this.compare.filter((x) => x !== id)
      else if (this.compare.length < MAX_COMPARE) this.compare = [...this.compare, id]
      else return false
      save(this)
      return true
    },

    saveAddress(address) {
      this.address = address
      save(this)
    },

    clearCompare() {
      this.compare = []
      save(this)
    },
  },
})

export const COMPARE_LIMIT = MAX_COMPARE

function read() {
  try {
    const saved = JSON.parse(localStorage.getItem(KEY)) ?? {}
    return { wishlist: saved.wishlist ?? [], recent: saved.recent ?? [], compare: saved.compare ?? [], address: saved.address ?? null }
  } catch {
    return { wishlist: [], recent: [], compare: [], address: null }
  }
}

function save(state) {
  try {
    localStorage.setItem(KEY, JSON.stringify({ wishlist: state.wishlist, recent: state.recent, compare: state.compare, address: state.address }))
  } catch {
    // Private windows can block storage. The features still work for this visit.
  }
}
