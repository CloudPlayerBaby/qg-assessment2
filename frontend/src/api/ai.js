import request from '@/utils/request'

export const aiApi = {
  // 检查用户当天是否还能使用AI（返回true表示还可以使用，false表示已达20次上限）
  checkLimit() {
    return request({
      url: '/ai/limit',
      method: 'get'
    })
  }
}
