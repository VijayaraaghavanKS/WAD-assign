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
  getProduct: (id) => request(`/products/${id}`),
  addReview: (id, review) => request(`/products/${id}/reviews`, { method: 'POST', body: json(review) }),
  createProduct: (product) => request('/products', { method: 'POST', body: json(product) }),
  updateProduct: (id, product) => request(`/products/${id}`, { method: 'PUT', body: json(product) }),
  deleteProduct: (id) => request(`/products/${id}`, { method: 'DELETE' }),

  getCart: () => request('/cart'),
  // variant is { size, colour }; both are optional. Each size and colour is its own cart line.
  addToCart: (productId, variant) => request(`/cart/add/${productId}`, { method: 'POST', body: variant ? json(variant) : undefined }),
  updateQuantity: (cartItemId, quantity) => request(`/cart/${cartItemId}`, { method: 'PUT', body: json({ quantity }) }),
  removeItem: (cartItemId) => request(`/cart/${cartItemId}`, { method: 'DELETE' }),

  // body: { coupon, address: { name, phone, line1, city, state, pin }, payment: 'UPI' | 'CARD' | 'COD' }
  placeOrder: (body) => request('/orders', { method: 'POST', body: json(body) }),
  getOrders: () => request('/orders'),
  getAllOrders: () => request('/orders/all'),
  getOrderStats: () => request('/orders/stats'),
  setOrderStatus: (id, status) => request(`/orders/${id}/status`, { method: 'PUT', body: json({ status }) }),

  getCoupons: () => request('/orders/coupons'),
  addCoupon: (code, percent) => request('/orders/coupons', { method: 'POST', body: json({ code, percent }) }),
  deleteCoupon: (code) => request(`/orders/coupons/${encodeURIComponent(code)}`, { method: 'DELETE' }),

  // Shopper preferences (wishlist, compare, recently viewed, address) kept on the account.
  getPrefs: () => request('/auth/prefs'),
  savePrefs: (prefs) => request('/auth/prefs', { method: 'PUT', body: json(prefs) }),

  getLogs: () => request('/dev/logs'),
  getMetrics: () => request('/dev/metrics'),
}

// Shown on the Developer page as a reference of what the API exposes.
export const ENDPOINTS = [
  { service: 'Auth', method: 'POST', path: '/api/auth/login', desc: 'Log in, returns a session token' },
  { service: 'Auth', method: 'GET', path: '/api/auth/me', desc: 'Who owns this token' },
  { service: 'Products', method: 'GET', path: '/api/products?category=', desc: 'List products, optionally by category' },
  { service: 'Products', method: 'GET', path: '/api/products/{id}', desc: 'One product with sizes, colours, specs and reviews' },
  { service: 'Products', method: 'POST', path: '/api/products/{id}/reviews', desc: 'Post a review (logged-in shopper)' },
  { service: 'Products', method: 'POST', path: '/api/products', desc: 'Create a product (admin only)' },
  { service: 'Cart', method: 'GET', path: '/api/cart', desc: "Get the logged-in user's cart" },
  { service: 'Cart', method: 'POST', path: '/api/cart/add/{productId}', desc: 'Add to cart, applying any sale price' },
  { service: 'Orders', method: 'POST', path: '/api/orders', desc: 'Checkout: address, payment, coupon, invoice' },
  { service: 'Orders', method: 'GET', path: '/api/orders', desc: 'Order history for the logged-in user' },
  { service: 'Orders', method: 'GET', path: '/api/orders/stats', desc: 'Sales totals and chart data (admin only)' },
  { service: 'Orders', method: 'PUT', path: '/api/orders/{id}/status', desc: 'Move an order along (admin only)' },
]
