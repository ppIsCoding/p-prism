/**
 * 生成聊天室 / 会话 id，用于后端区分不同会话（对应 chatId 参数）。
 */
export function createChatId() {
  const stamp = Date.now().toString(36)
  const random = Math.random().toString(36).slice(2, 8)
  return `chat_${stamp}_${random}`
}

let counter = 0
export function createMessageId() {
  counter += 1
  return `msg_${Date.now().toString(36)}_${counter}`
}
