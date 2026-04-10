import request from '@/utils/request'

export const userApi = {
  register(data) {
    return request({
      url: '/user/register',
      method: 'post',
      data
    })
  },

  login(data) {
    return request({
      url: '/user/login',
      method: 'post',
      data
    })
  },

  getUserInfo() {
    return request({
      url: '/user/info',
      method: 'get'
    })
  },

  updateProfile(data) {
    return request({
      url: '/user/profile',
      method: 'patch',
      data
    })
  },

  updatePassword(data) {
    return request({
      url: '/user/password',
      method: 'patch',
      data
    })
  }
}
