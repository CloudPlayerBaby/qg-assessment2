import request from '@/utils/request'

export const lostApi = {
  postLostItem(data) {
    return request({
      url: '/lost/item',
      method: 'post',
      data: data
    })
  },
  getMyLostItems(params) {
    return request({
      url: '/lost/mine',
      method: 'get',
      params: params
    })
  },
  getDetail(id) {
    return request({
      url: `/lost/${id}`,
      method: 'get'
    })
  },
  getLostItemDetail(id) {
    return request({
      url: `/lost/${id}`,
      method: 'get'
    })
  },
  updateLostItem(id, data) {
    return request({
      url: `/lost/${id}`,
      method: 'patch',
      data: data
    })
  },
  deleteLostItem(id) {
    return request({
      url: `/lost/${id}`,
      method: 'delete'
    })
  },
  getHomeLostItems(params) {
    return request({
      url: '/lost/home',
      method: 'get',
      params: params
    })
  },
  report(data) {
    return request({
      url: '/lost/report/item',
      method: 'put',
      data: data
    })
  }
}
