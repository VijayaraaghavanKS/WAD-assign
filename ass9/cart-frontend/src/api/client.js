// Two independent microservices, each on its own port with its own
// MongoDB database. There is no API Gateway in this minimal setup, so the
// frontend talks to both directly -- but Product Service and Cart Service
// remain fully independent Spring Boot apps that could be deployed and
// scaled separately.
const PRODUCT_API = 'http://localhost:8083/api'
const CART_API = 'http://localhost:8084/api'

export const ENDPOINTS = [
  { service: 'Product Service :8083', method: 'GET', path: '/api/products', desc: 'List all products' },
  { service: 'Product Service :8083', method: 'GET', path: '/api/products/{id}', desc: 'Get one product (called internally by Cart Service)' },
  { service: 'Product Service :8083', method: 'POST', path: '/api/products', desc: 'Create a product (admin)' },
  { service: 'Product Service :8083', method: 'PUT', path: '/api/products/{id}', desc: 'Update a product (admin)' },
  { service: 'Product Service :8083', method: 'DELETE', path: '/api/products/{id}', desc: 'Delete a product (admin)' },
  { service: 'Cart Service :8084', method: 'GET', path: '/api/cart', desc: 'Get current cart items' },
  { service: 'Cart Service :8084', method: 'GET', path: '/api/cart/total', desc: 'Get cart total amount' },
  { service: 'Cart Service :8084', method: 'POST', path: '/api/cart/add/{productId}', desc: 'Add to cart (calls Product Service internally)' },
  { service: 'Cart Service :8084', method: 'PUT', path: '/api/cart/{cartItemId}', desc: 'Update a cart item quantity' },
  { service: 'Cart Service :8084', method: 'DELETE', path: '/api/cart/{cartItemId}', desc: 'Remove one cart item' },
  { service: 'Cart Service :8084', method: 'DELETE', path: '/api/cart', desc: 'Clear the entire cart' },
]

async function request(base, path, options = {}) {
  const res = await fetch(`${base}${path}`, {
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
  getProducts: () => request(PRODUCT_API, '/products'),
  createProduct: (product) => request(PRODUCT_API, '/products', { method: 'POST', body: JSON.stringify(product) }),
  updateProduct: (id, product) => request(PRODUCT_API, `/products/${id}`, { method: 'PUT', body: JSON.stringify(product) }),
  deleteProduct: (id) => request(PRODUCT_API, `/products/${id}`, { method: 'DELETE' }),

  getCart: () => request(CART_API, '/cart'),
  addToCart: (productId) => request(CART_API, `/cart/add/${productId}`, { method: 'POST' }),
  updateQuantity: (cartItemId, quantity) => request(CART_API, `/cart/${cartItemId}`, { method: 'PUT', body: JSON.stringify({ quantity }) }),
  removeItem: (cartItemId) => request(CART_API, `/cart/${cartItemId}`, { method: 'DELETE' }),
  clearCart: () => request(CART_API, '/cart', { method: 'DELETE' }),

  // Developer interface pulls logs/metrics from BOTH services independently
  // and tags each row with which service it came from.
  getProductServiceLogs: () => request(PRODUCT_API, '/dev/logs'),
  getProductServiceMetrics: () => request(PRODUCT_API, '/dev/metrics'),
  getCartServiceLogs: () => request(CART_API, '/dev/logs'),
  getCartServiceMetrics: () => request(CART_API, '/dev/metrics'),
}
