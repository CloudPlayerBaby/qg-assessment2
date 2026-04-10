import request from '@/utils/request'

export const foundApi = {
  postFoundItem(data) {
    return request({
      url: '/found/item',
      method: 'post',
      data: data
    })
  },
  getMyFoundItems(params) {
    return request({
      url: '/found/mine',
      method: 'get',
      params: params
    })
  },
  getDetail(id) {
    return request({
      url: `/found/${id}`,
      method: 'get'
    })
  },
  getFoundItemDetail(id) {
    return request({
      url: `/found/${id}`,
      method: 'get'
    })
  },
  updateFoundItem(id, data) {
    return request({
      url: `/found/${id}`,
      method: 'patch',
      data: data
    })
  },
  deleteFoundItem(id) {
    return request({
      url: `/found/${id}`,
      method: 'delete'
    })
  },
  getHomeFoundItems(params) {
    return request({
      url: '/found/home',
      method: 'get',
      params: params
    })
  },
  report(data) {
    return request({
      url: '/found/report/item',
      method: 'put',
      data: data
    })
  }
}
