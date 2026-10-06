// Each microservice has its own port and database. The browser calls them
// directly. Every request carries the logged-in user's token, which each
// service checks with the User Service.
const USER_API = 'http://localhost:8085/api'
const PRODUCT_API = 'http://localhost:8083/api'
const CART_API = 'http://localhost:8084/api'
const ORDER_API = 'http://localhost:8086/api'
// The browser's one address. The gateway forwards each call to the service that owns the path.
const GATEWAY = 'http://localhost:8080/api'

const SESSION_KEY = 'shopcart-session'

export function savedToken() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY))?.token ?? null
  } catch {
    return null
  }
}

async function request(base, path, options = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = savedToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(`${base}${path}`, { ...options, headers: { ...headers, ...options.headers } })
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
  login: (username, password) => request(GATEWAY, '/auth/login', { method: 'POST', body: json({ username, password }) }),
  register: (username, password) => request(GATEWAY, '/auth/register', { method: 'POST', body: json({ username, password }) }),

  getProduct: (id) => request(GATEWAY, `/products/${id}`),
  addReview: (id, review) => request(GATEWAY, `/products/${id}/reviews`, { method: 'POST', body: json(review) }),
  getProducts: (category) => request(GATEWAY, `/products${category ? `?category=${encodeURIComponent(category)}` : ''}`),
  createProduct: (product) => request(GATEWAY, '/products', { method: 'POST', body: json(product) }),
  updateProduct: (id, product) => request(GATEWAY, `/products/${id}`, { method: 'PUT', body: json(product) }),
  deleteProduct: (id) => request(GATEWAY, `/products/${id}`, { method: 'DELETE' }),

  getCart: () => request(GATEWAY, '/cart'),
  // variant is { size, colour }; both are optional. Each size and colour is its own cart line.
  addToCart: (productId, variant) => request(GATEWAY, `/cart/add/${productId}`, { method: 'POST', body: variant ? json(variant) : undefined }),
  updateQuantity: (cartItemId, quantity) => request(GATEWAY, `/cart/${cartItemId}`, { method: 'PUT', body: json({ quantity }) }),
  removeItem: (cartItemId) => request(GATEWAY, `/cart/${cartItemId}`, { method: 'DELETE' }),

  // body: { coupon, address: { name, phone, line1, city, state, pin }, payment: 'UPI' | 'CARD' | 'COD' }
  placeOrder: (body) => request(GATEWAY, '/orders', { method: 'POST', body: json(body) }),
  getOrders: () => request(GATEWAY, '/orders'),
  getAllOrders: () => request(GATEWAY, '/orders/all'),
  getOrderStats: () => request(GATEWAY, '/orders/stats'),
  setOrderStatus: (id, status) => request(GATEWAY, `/orders/${id}/status`, { method: 'PUT', body: json({ status }) }),

  getCoupons: () => request(GATEWAY, '/orders/coupons'),
  addCoupon: (code, percent) => request(GATEWAY, '/orders/coupons', { method: 'POST', body: json({ code, percent }) }),
  deleteCoupon: (code) => request(GATEWAY, `/orders/coupons/${encodeURIComponent(code)}`, { method: 'DELETE' }),

  // Shopper preferences (wishlist, compare, recently viewed, address) kept on the account.
  getPrefs: () => request(GATEWAY, '/auth/prefs'),
  savePrefs: (prefs) => request(GATEWAY, '/auth/prefs', { method: 'PUT', body: json(prefs) }),

  getUserServiceLogs: () => request(USER_API, '/dev/logs'),
  getUserServiceMetrics: () => request(USER_API, '/dev/metrics'),
  getProductServiceLogs: () => request(PRODUCT_API, '/dev/logs'),
  getProductServiceMetrics: () => request(PRODUCT_API, '/dev/metrics'),
  getCartServiceLogs: () => request(CART_API, '/dev/logs'),
  getCartServiceMetrics: () => request(CART_API, '/dev/metrics'),
  getOrderServiceLogs: () => request(ORDER_API, '/dev/logs'),
  getOrderServiceMetrics: () => request(ORDER_API, '/dev/metrics'),

  // Used by the service map: any HTTP answer means the service is up. no-cors lets the
  // reply through even when the service sends no CORS headers for that path.
  ping: (base) => fetch(`${base}/dev/metrics`, { mode: 'no-cors', cache: 'no-store' }).then(() => true, () => false),
  services: { USER_API, PRODUCT_API, CART_API, ORDER_API },
}

// Shown on the Developer page as a reference of what each service exposes.
export const ENDPOINTS = [
  { service: 'User Service :8085', method: 'POST', path: '/api/auth/login', desc: 'Log in, returns a session token' },
  { service: 'User Service :8085', method: 'GET', path: '/api/auth/me', desc: 'Who owns this token (used by every other service)' },
  { service: 'Product Service :8083', method: 'GET', path: '/api/products?category=', desc: 'List products, optionally by category' },
  { service: 'Product Service :8083', method: 'POST', path: '/api/products', desc: 'Create a product (admin only)' },
  { service: 'Product Service :8083', method: 'PUT', path: '/api/products/{id}', desc: 'Update a product (admin only)' },
  { service: 'Product Service :8083', method: 'POST', path: '/api/products/{id}/reserve', desc: 'Reserve stock at checkout (internal)' },
  { service: 'Cart Service :8084', method: 'GET', path: '/api/cart', desc: 'Get the logged-in user\'s cart' },
  { service: 'Cart Service :8084', method: 'POST', path: '/api/cart/add/{productId}', desc: 'Add to cart, applying any sale price' },
  { service: 'Cart Service :8084', method: 'DELETE', path: '/api/cart', desc: 'Clear the cart' },
  { service: 'Order Service :8086', method: 'POST', path: '/api/orders', desc: 'Checkout: reserve stock, apply coupon, save order, clear cart' },
  { service: 'Order Service :8086', method: 'GET', path: '/api/orders', desc: 'Order history for the logged-in user' },
  { service: 'Product Service :8083', method: 'GET', path: '/api/products/{id}', desc: 'One product with sizes, colours, specs and reviews' },
  { service: 'Product Service :8083', method: 'POST', path: '/api/products/{id}/reviews', desc: 'Post a review (logged-in shopper)' },
  { service: 'Order Service :8086', method: 'GET', path: '/api/orders/stats', desc: 'Sales totals and chart data (admin only)' },
  { service: 'API Gateway :8080', method: 'ANY', path: '/api/*', desc: 'Forwards each call to the service that owns the path' },
  { service: 'Order Service :8086', method: 'GET', path: '/api/orders/coupons', desc: 'Coupon codes the shop accepts' },
  { service: 'Order Service :8086', method: 'POST', path: '/api/orders/coupons', desc: 'Add a coupon code (admin only)' },
  { service: 'Order Service :8086', method: 'DELETE', path: '/api/orders/coupons/{code}', desc: 'Remove a coupon code (admin only)' },
  { service: 'User Service :8085', method: 'GET', path: '/api/auth/prefs', desc: 'Shopper wishlist, compare, recently viewed and address' },
  { service: 'User Service :8085', method: 'PUT', path: '/api/auth/prefs', desc: 'Save those shopper preferences' },
  { service: 'Order Service :8086', method: 'PUT', path: '/api/orders/{id}/status', desc: 'Move an order PLACED, PACKED, SHIPPED, DELIVERED (admin only)' },
]
