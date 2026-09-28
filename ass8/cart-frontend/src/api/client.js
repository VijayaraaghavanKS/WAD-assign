// Single place that knows the backend's base URL and every endpoint it
// exposes. Keeping this centralized (instead of fetch() calls scattered
// across components) is what lets the Developer Interface document the
// exact same endpoints the User/Admin interfaces actually call.
const BASE = 'http://localhost:8082/api'

export const ENDPOINTS = [
  { method: 'GET', path: '/api/products', desc: 'List all products' },
  { method: 'POST', path: '/api/products', desc: 'Create a product (admin)' },
  { method: 'PUT', path: '/api/products/{id}', desc: 'Update a product (admin)' },
  { method: 'DELETE', path: '/api/products/{id}', desc: 'Delete a product (admin)' },
  { method: 'GET', path: '/api/cart', desc: 'Get current cart items' },
  { method: 'GET', path: '/api/cart/total', desc: 'Get cart total amount' },
  { method: 'POST', path: '/api/cart/add/{productId}', desc: 'Add a product to the cart' },
  { method: 'PUT', path: '/api/cart/{cartItemId}', desc: 'Update a cart item quantity' },
  { method: 'DELETE', path: '/api/cart/{cartItemId}', desc: 'Remove one cart item' },
  { method: 'DELETE', path: '/api/cart', desc: 'Clear the entire cart' },
  { method: 'GET', path: '/api/dev/logs', desc: 'Last 100 request logs' },
  { method: 'GET', path: '/api/dev/metrics', desc: 'Aggregate request metrics' },
]

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!res.ok) throw new Error(`${options.method || 'GET'} ${path} failed: ${res.status}`)
  const text = await res.text()
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch {
    // Some endpoints (e.g. DELETE) return a plain-text confirmation, not JSON.
    return text
  }
}

export const api = {
  getProducts: () => request('/products'),
  createProduct: (product) => request('/products', { method: 'POST', body: JSON.stringify(product) }),
  updateProduct: (id, product) => request(`/products/${id}`, { method: 'PUT', body: JSON.stringify(product) }),
  deleteProduct: (id) => request(`/products/${id}`, { method: 'DELETE' }),

  getCart: () => request('/cart'),
  addToCart: (productId) => request(`/cart/add/${productId}`, { method: 'POST' }),
  updateQuantity: (cartItemId, quantity) => request(`/cart/${cartItemId}`, { method: 'PUT', body: JSON.stringify({ quantity }) }),
  removeItem: (cartItemId) => request(`/cart/${cartItemId}`, { method: 'DELETE' }),
  clearCart: () => request('/cart', { method: 'DELETE' }),

  getLogs: () => request('/dev/logs'),
  getMetrics: () => request('/dev/metrics'),
}
