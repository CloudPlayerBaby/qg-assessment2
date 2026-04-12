import request from '@/utils/request'

export const commentApi = {
  addComment: (data) => {
    return request.post('/comment/add', data)
  },
  getComments: (postId, postType) => {
    return request.get('/comment/get', {
      params: { postId, postType }
    })
  },
  getUnreadCount: () => {
    return request.get('/comment/unreadCount')
  },
  getMyMessages: () => {
    return request.get('/comment/myMessages')
  },
  markAsRead: (commentId) => {
    return request.post('/comment/read', null, {
      params: { commentId }
    })
  }
}
