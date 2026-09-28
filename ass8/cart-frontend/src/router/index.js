import { createRouter, createWebHistory } from 'vue-router'
import Shop from '../views/Shop.vue'
import Admin from '../views/Admin.vue'
import Developer from '../views/Developer.vue'

// Three separate route-level interfaces sharing the same backend:
// "/" for shoppers, "/admin" for catalog management, "/dev" for observability.
const routes = [
  { path: '/', component: Shop },
  { path: '/admin', component: Admin },
  { path: '/dev', component: Developer },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
