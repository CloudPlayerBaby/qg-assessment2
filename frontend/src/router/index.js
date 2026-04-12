import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/Index.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'publish',
        name: 'Publish',
        component: () => import('@/views/Publish.vue')
      },
      {
        path: 'edit-lost',
        name: 'EditLost',
        component: () => import('@/views/EditLost.vue')
      },
      {
        path: 'edit-found',
        name: 'EditFound',
        component: () => import('@/views/EditFound.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },

      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/admin/Index.vue'),
        redirect: '/admin/user-manage',
        meta: { requiresAdmin: true },
        children: [
          {
            path: 'user-manage',
            name: 'UserManage',
            component: () => import('@/views/admin/UserManage.vue'),
            meta: { requiresAdmin: true }
          },
          {
            path: 'post-manage',
            name: 'PostManage',
            component: () => import('@/views/admin/PostManage.vue'),
            meta: { requiresAdmin: true }
          },
          {
            path: 'top-manage',
            name: 'TopManage',
            component: () => import('@/views/admin/Posts.vue'),
            meta: { requiresAdmin: true }
          },
          {
            path: 'report',
            name: 'AdminReport',
            component: () => import('@/views/admin/Report.vue'),
            meta: { requiresAdmin: true }
          },
          {
            path: 'statistics',
            name: 'AdminStatistics',
            component: () => import('@/views/admin/Statistics.vue'),
            meta: { requiresAdmin: true }
          }
        ]
      },
      {
        path: 'detail/:itemType/:id',
        name: 'Detail',
        component: () => import('@/views/Detail.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.path !== '/login' && to.path !== '/register' && !userStore.token) {
    next('/login')
  } else if (to.meta.requiresAdmin && !userStore.isAdmin()) {
    next('/home')
  } else {
    next()
  }
})

export default router
