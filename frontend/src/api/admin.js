import request from '@/utils/request'

export const adminApi = {
  getLostPosts(params) {
    return request({
      url: '/admin/posts/lost',
      method: 'get',
      params: params
    })
  },

  getFoundPosts(params) {
    return request({
      url: '/admin/posts/found',
      method: 'get',
      params: params
    })
  },

  setLostTop(data) {
    return request({
      url: '/admin/posts/lost/top',
      method: 'put',
      data: data
    })
  },

  setFoundTop(data) {
    return request({
      url: '/admin/posts/found/top',
      method: 'put',
      data: data
    })
  },

  rejectLostTop(id) {
    return request({
      url: '/admin/posts/lost/reject',
      method: 'put',
      params: { id: id }
    })
  },

  rejectFoundTop(id) {
    return request({
      url: '/admin/posts/found/reject',
      method: 'put',
      params: { id: id }
    })
  },

  getReportList(params) {
    return request({
      url: '/admin/report/item',
      method: 'get',
      params: params
    })
  },

  acceptReport(id) {
    return request({
      url: '/admin/report/accept',
      method: 'patch',
      params: { id: id },
      data: {}
    })
  },

  rejectReport(id) {
    return request({
      url: '/admin/report/reject',
      method: 'patch',
      params: { id: id },
      data: {}
    })
  },

  getUserList(params) {
    return request({
      url: '/admin/admin/users',
      method: 'get',
      params: params
    })
  },

  banUser(id) {
    return request({
      url: `/admin/admin/users/ban/${id}`,
      method: 'patch'
    })
  },

  unBanUser(id) {
    return request({
      url: `/admin/admin/users/unBan/${id}`,
      method: 'patch'
    })
  },

  banPost(id, type) {
    return request({
      url: '/admin/admin/posts/ban',
      method: 'patch',
      params: { id: id, type: type }
    })
  },

  unBanPost(id, type) {
    return request({
      url: '/admin/admin/posts/unBan',
      method: 'patch',
      params: { id: id, type: type }
    })
  },

  deletePost(id, type) {
    return request({
      url: '/admin/admin/posts',
      method: 'delete',
      params: { id: id, type: type }
    })
  },

  getAllLostPosts(params) {
    return request({
      url: '/lost/home',
      method: 'get',
      params: params
    })
  },

  getAllFoundPosts(params) {
    return request({
      url: '/found/home',
      method: 'get',
      params: params
    })
  },

  getPostNumber() {
    return request({
      url: '/admin/admin/statistics/posts-count',
      method: 'get'
    })
  },

  getCompletedPostNumber() {
    return request({
      url: '/admin/admin/statistics/posts-completed-count',
      method: 'get'
    })
  },

  getActiveUsersNumber() {
    return request({
      url: '/admin/admin/statistics/active-users-count',
      method: 'get'
    })
  }
}
