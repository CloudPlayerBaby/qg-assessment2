import { createRouter, createWebHistory } from 'vue-router'

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
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/Messages.vue')
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/admin/Index.vue'),
        redirect: '/admin/users',
        children: [
          {
            path: 'users',
            name: 'AdminUsers',
            component: () => import('@/views/admin/Users.vue')
          },
          {
            path: 'posts',
            name: 'AdminPosts',
            component: () => import('@/views/admin/Posts.vue')
          },
          {
            path: 'statistics',
            name: 'AdminStatistics',
            component: () => import('@/views/admin/Statistics.vue')
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
