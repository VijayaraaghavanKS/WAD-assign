import { defineStore } from 'pinia'
import { api } from '../api/client'
import { useAuthStore } from './auth'

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

    // Signed in: the account's copy wins when it has one. Otherwise this browser's copy is uploaded.
    async sync() {
      const remote = await api.getPrefs()
      if (remote && Object.keys(remote).length) Object.assign(this, pick(remote))
      else await api.savePrefs(pick(this))
      save(this)
    },

    // Signed out: back to this browser's own copy.
    reset() {
      Object.assign(this, read())
    },
  },
})

export const COMPARE_LIMIT = MAX_COMPARE

const pick = ({ wishlist, recent, compare, address }) => ({
  wishlist: wishlist ?? [],
  recent: recent ?? [],
  compare: compare ?? [],
  address: address ?? null,
})

function read() {
  try {
    return pick(JSON.parse(localStorage.getItem(KEY)) ?? {})
  } catch {
    return pick({})
  }
}

// Writes this browser's copy, and the account's copy when someone is signed in.
function save(state) {
  const data = pick(state)
  try {
    localStorage.setItem(KEY, JSON.stringify(data))
  } catch {
    // Private windows can block storage. The features still work for this visit.
  }
  if (useAuthStore().isLoggedIn) api.savePrefs(data).catch(() => {})
}
