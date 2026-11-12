import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '@/stores/auth'

const routes = [
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
  { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue'), meta: { public: true } },
  { path: '/', name: 'dashboard', component: () => import('@/views/DashboardView.vue') },
  { path: '/documents/:id', name: 'document', component: () => import('@/views/DocumentDetailView.vue'), props: true },
  { path: '/tags', name: 'tags', component: () => import('@/views/TagsView.vue') },
  { path: '/account', name: 'account', component: () => import('@/views/AccountView.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (!to.meta.public && !auth.user) return { name: 'login' }
  if (to.meta.public && auth.user) return { name: 'dashboard' }
})

export default router
