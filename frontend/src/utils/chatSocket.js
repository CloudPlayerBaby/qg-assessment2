let socket = null

export function setChatSocket(ws) {
  socket = ws
}

export function getChatSocket() {
  return socket
}

export function sendChatPayload(obj) {
  const s = socket
  if (!s || s.readyState !== WebSocket.OPEN) {
    return false
  }
  s.send(typeof obj === 'string' ? obj : JSON.stringify(obj))
  return true
}
