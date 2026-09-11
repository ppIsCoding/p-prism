import { onBeforeUnmount, ref } from 'vue'
import { streamChat } from '@/api/sse'
import { createChatId, createMessageId } from '@/utils/id'

/**
 * 单个 AI 应用的聊天状态机。
 *
 * @param {import('@/config/apps').APPS[keyof typeof APPS]} app
 */
export function useChat(app) {
  const messages = ref([])
  const isStreaming = ref(false)
  const errorMessage = ref('')
  const chatId = ref(app.sessionId ? createChatId() : '')

  let controller = null

  function newSession() {
    stop()
    messages.value = []
    errorMessage.value = ''
    if (app.sessionId) chatId.value = createChatId()
  }

  function stop() {
    if (controller) {
      controller.abort()
      controller = null
    }
    isStreaming.value = false
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') last.streaming = false
  }

  async function send(text) {
    const content = (text || '').trim()
    if (!content || isStreaming.value) return

    errorMessage.value = ''

    messages.value.push({
      id: createMessageId(),
      role: 'user',
      text: content,
    })

    messages.value.push({
      id: createMessageId(),
      role: 'assistant',
      segments: [],
      streaming: true,
      error: '',
    })

    // 取数组下标拿到的才是 Vue 的响应式代理，直接改它才能触发更新
    const assistant = messages.value[messages.value.length - 1]
    const current = new AbortController()
    controller = current
    isStreaming.value = true

    const params = { message: content }
    if (app.sessionId) params.chatId = chatId.value

    try {
      await streamChat({
        path: app.endpoint,
        params,
        signal: current.signal,
        onMessage(chunk) {
          if (app.streamMode === 'append') {
            if (!assistant.segments.length) assistant.segments.push('')
            assistant.segments[0] += chunk
          } else {
            assistant.segments.push(chunk)
          }
        },
      })
    } catch (error) {
      if (!current.signal.aborted && error?.name !== 'AbortError') {
        assistant.error = error?.message || '连接中断，请稍后重试'
        errorMessage.value = assistant.error
      }
    } finally {
      assistant.streaming = false
      isStreaming.value = false
      if (controller === current) controller = null
    }
  }

  onBeforeUnmount(stop)

  return {
    messages,
    isStreaming,
    errorMessage,
    chatId,
    send,
    stop,
    newSession,
  }
}
