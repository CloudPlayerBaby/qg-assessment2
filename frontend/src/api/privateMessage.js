import request from '@/utils/request'

export const privateMessageApi = {
  ensureSession: (data) => request.post('/privateMessage/session', data),
  getHistory: (sessionId) =>
    request.get('/privateMessage/history', { params: { sessionId } }),
  markRead: (sessionId) =>
    request.post('/privateMessage/read', null, { params: { sessionId } }),
  getUnreadCount: () => request.get('/privateMessage/unreadCount'),
  getSessions: () => request.get('/privateMessage/sessions')
}
