import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../views/Login.vue'
import Shop from '../views/Shop.vue'
import Cart from '../views/Cart.vue'
import Orders from '../views/Orders.vue'
import Admin from '../views/Admin.vue'
import Developer from '../views/Developer.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Credits from '../views/Credits.vue'
import Offers from '../views/Offers.vue'

const HOME = { USER: '/', ADMIN: '/admin', DEVELOPER: '/dev' }

// `public` pages open without signing in. `shopper` pages are for shoppers and
// guests only, so staff are sent to their own home. `roles` lists who may open a
// page. Checkout asks for a sign-in itself, from the cart page.
const routes = [
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/', component: Shop, meta: { public: true, shopper: true } },
  { path: '/product/:id', component: ProductDetail, meta: { public: true } },
  { path: '/offers', component: Offers, meta: { public: true } },
  { path: '/credits', component: Credits, meta: { public: true } },
  { path: '/cart', component: Cart, meta: { public: true, shopper: true } },
  { path: '/orders', component: Orders, meta: { roles: ['USER'] } },
  { path: '/admin', component: Admin, meta: { roles: ['ADMIN'] } },
  { path: '/dev', component: Developer, meta: { roles: ['DEVELOPER'] } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.shopper && auth.isLoggedIn && auth.role !== 'USER') return { path: HOME[auth.role] }
  if (to.meta.public) return true
  if (!auth.isLoggedIn) return { path: '/login', query: { redirect: to.fullPath } }
  // Each role lands on its own page; orders and admin pages are role-only.
  if (to.meta.roles && !to.meta.roles.includes(auth.role)) return { path: HOME[auth.role] ?? '/login' }
  return true
})

export default router
