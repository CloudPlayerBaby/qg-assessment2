import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

let isRefreshing = false

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401 && router.currentRoute.value.path !== '/login') {
        if (!isRefreshing) {
          isRefreshing = true
          ElMessage.error(res.message || '登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          setTimeout(() => {
            isRefreshing = false
          }, 2000)
        }
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response && error.response.status === 401 && router.currentRoute.value.path !== '/login') {
      if (!isRefreshing) {
        isRefreshing = true
        const message = error.response.data?.message || '登录已过期，请重新登录'
        ElMessage.error(message)
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
        setTimeout(() => {
          isRefreshing = false
        }, 2000)
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
