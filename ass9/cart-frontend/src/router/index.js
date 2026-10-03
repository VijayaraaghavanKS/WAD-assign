import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../views/Login.vue'
import Shop from '../views/Shop.vue'
import Orders from '../views/Orders.vue'
import Admin from '../views/Admin.vue'
import Developer from '../views/Developer.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Credits from '../views/Credits.vue'

const HOME = { USER: '/', ADMIN: '/admin', DEVELOPER: '/dev' }

// `roles` lists who may open a page. No roles means any logged-in user.
const routes = [
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/', component: Shop, meta: { roles: ['USER'] } },
  { path: '/product/:id', component: ProductDetail },
  { path: '/credits', component: Credits, meta: { public: true } },
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
  if (to.meta.public) return true
  if (!auth.isLoggedIn) return { path: '/login', query: { redirect: to.fullPath } }
  // Each role lands on its own page; the shop is for shoppers only.
  if (to.meta.roles && !to.meta.roles.includes(auth.role)) return { path: HOME[auth.role] ?? '/login' }
  return true
})

export default router
