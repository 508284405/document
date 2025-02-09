import { createRouter, createWebHistory } from 'vue-router'
import ShortUrlHome from '../components/ShortUrlHome.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: ShortUrlHome
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
