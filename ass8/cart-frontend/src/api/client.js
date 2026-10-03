// One Spring Boot app, one port. The user, product, cart and order packages
// live in the same process, so every API path is served from the same base URL.
const API = 'http://localhost:8082/api'

const SESSION_KEY = 'shopcart-session'

export function savedToken() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY))?.token ?? null
  } catch {
    return null
  }
}

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = savedToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(`${API}${path}`, { ...options, headers: { ...headers, ...options.headers } })
  const text = await res.text()
  const data = text ? safeParse(text) : null
  if (!res.ok) throw new Error(data?.message || data || `Request failed (${res.status})`)
  return data
}

function safeParse(text) {
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

const json = (body) => JSON.stringify(body)

export const api = {
  login: (username, password) => request('/auth/login', { method: 'POST', body: json({ username, password }) }),
  register: (username, password) => request('/auth/register', { method: 'POST', body: json({ username, password }) }),

  getProducts: (category) => request(`/products${category ? `?category=${encodeURIComponent(category)}` : ''}`),
  createProduct: (product) => request('/products', { method: 'POST', body: json(product) }),
  updateProduct: (id, product) => request(`/products/${id}`, { method: 'PUT', body: json(product) }),
  deleteProduct: (id) => request(`/products/${id}`, { method: 'DELETE' }),

  getCart: () => request('/cart'),
  addToCart: (productId) => request(`/cart/add/${productId}`, { method: 'POST' }),
  updateQuantity: (cartItemId, quantity) => request(`/cart/${cartItemId}`, { method: 'PUT', body: json({ quantity }) }),
  removeItem: (cartItemId) => request(`/cart/${cartItemId}`, { method: 'DELETE' }),

  placeOrder: () => request('/orders', { method: 'POST' }),
  getOrders: () => request('/orders'),

  getLogs: () => request('/dev/logs'),
  getMetrics: () => request('/dev/metrics'),
}

// Shown on the Developer page as a reference of what the API exposes.
export const ENDPOINTS = [
  { service: 'Auth', method: 'POST', path: '/api/auth/login', desc: 'Log in, returns a session token' },
  { service: 'Auth', method: 'GET', path: '/api/auth/me', desc: 'Who owns this token' },
  { service: 'Products', method: 'GET', path: '/api/products?category=', desc: 'List products, optionally by category' },
  { service: 'Products', method: 'POST', path: '/api/products', desc: 'Create a product (admin only)' },
  { service: 'Products', method: 'PUT', path: '/api/products/{id}', desc: 'Update a product (admin only)' },
  { service: 'Cart', method: 'GET', path: '/api/cart', desc: 'Get the logged-in user\'s cart' },
  { service: 'Cart', method: 'POST', path: '/api/cart/add/{productId}', desc: 'Add to cart, applying any sale price' },
  { service: 'Cart', method: 'DELETE', path: '/api/cart', desc: 'Clear the cart' },
  { service: 'Orders', method: 'POST', path: '/api/orders', desc: 'Checkout: take stock, save order, clear cart' },
  { service: 'Orders', method: 'GET', path: '/api/orders', desc: 'Order history for the logged-in user' },
]
