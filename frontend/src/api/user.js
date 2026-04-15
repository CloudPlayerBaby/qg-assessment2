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

  getUserInfoById(id) {
    return request({
      url: `/user/info/${id}`,
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
  },

  // 发送邮箱验证码
  sendVerificationCode(email) {
    return request({
      url: '/email/send',
      method: 'post',
      params: { email }
    })
  },

  // 重置密码
  resetPassword(data) {
    return request({
      url: '/user/reset-password',
      method: 'post',
      data
    })
  }
}
