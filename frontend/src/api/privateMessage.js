import request from '@/utils/request'

export const privateMessageApi = {
  send(data) {
    return request({
      url: '/privateMessage/send',
      method: 'post',
      data
    })
  },

  list(params) {
    return request({
      url: '/privateMessage/list',
      method: 'get',
      params
    })
  },

  markRead(params) {
    return request({
      url: '/privateMessage/read',
      method: 'post',
      params
    })
  },

  getUnreadCount() {
    return request({
      url: '/privateMessage/unreadCount',
      method: 'get'
    })
  },

  getConversations() {
    return request({
      url: '/privateMessage/conversations',
      method: 'get'
    })
  }
}
