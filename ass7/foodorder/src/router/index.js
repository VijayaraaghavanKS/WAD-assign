import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Order from '../views/Order.vue'
import FoodMenu from '../views/FoodMenu.vue'
import Cart from '../views/Cart.vue'
import About from '../views/About.vue'

// /order is the parent route; /order/menu and /order/cart are its nested
// children and render inside Order.vue's <RouterView>.
const routes = [
  { path: '/', component: Home },
  {
    path: '/order',
    component: Order,
    children: [
      { path: 'menu', component: FoodMenu },
      { path: 'cart', component: Cart }
    ]
  },
  { path: '/about', component: About }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
