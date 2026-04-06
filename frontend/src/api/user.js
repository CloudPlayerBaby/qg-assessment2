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
  }
}
