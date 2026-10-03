import { defineStore } from 'pinia'
import { api } from '../api/client'

const SESSION_KEY = 'shopcart-session'

// The logged-in session. Saved to localStorage so a refresh keeps you signed in.
export const useAuthStore = defineStore('auth', {
  state: () => ({
    session: readSession(),
  }),

  getters: {
    isLoggedIn: (state) => !!state.session,
    role: (state) => state.session?.role ?? null,
  },

  actions: {
    async login(username, password) {
      this.session = await api.login(username, password)
      localStorage.setItem(SESSION_KEY, JSON.stringify(this.session))
    },

    async register(username, password) {
      await api.register(username, password)
      await this.login(username, password)
    },

    logout() {
      this.session = null
      localStorage.removeItem(SESSION_KEY)
    },
  },
})

function readSession() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY))
  } catch {
    return null
  }
}
