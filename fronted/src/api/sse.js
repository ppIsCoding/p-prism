import { API_BASE } from './http'

/**
 * 解析单个 SSE 事件块（已按空行切分）。
 * 支持多行 data:，注释/心跳行会被忽略。
 */
function parseEventBlock(raw) {
  const dataLines = []
  let hasData = false

  for (const line of raw.split('\n')) {
    if (!line || line.startsWith(':')) continue
    if (line.startsWith('data:')) {
      hasData = true
      let value = line.slice(5)
      if (value.startsWith(' ')) value = value.slice(1)
      dataLines.push(value)
    }
  }

  return hasData ? dataLines.join('\n') : null
}

function buildUrl(path, params) {
  const base = API_BASE.replace(/\/$/, '')
  const query = new URLSearchParams()
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, value)
    }
  })
  const qs = query.toString()
  return `${base}${path}${qs ? `?${qs}` : ''}`
}

/**
 * 以 GET + fetch(ReadableStream) 消费后端 SSE 流。
 *
 * 不使用 EventSource：它会在服务端正常关闭连接后自动重连，
 * 导致一次性对话被重复触发。native fetch 更可控，并支持 AbortController。
 *
 * @param {object}   options
 * @param {string}   options.path                      接口路径（相对 API_BASE）
 * @param {object}   options.params                    查询参数
 * @param {AbortSignal} options.signal                 中断信号
 * @param {(chunk: string) => void} options.onMessage  每收到一个事件回调
 */
export async function streamChat({ path, params, signal, onMessage }) {
  const response = await fetch(buildUrl(path, params), {
    method: 'GET',
    headers: {
      Accept: 'text/event-stream',
      'Cache-Control': 'no-cache',
    },
    signal,
  })

  if (!response.ok) {
    throw new Error(`服务响应异常（HTTP ${response.status}）`)
  }
  if (!response.body) {
    throw new Error('当前浏览器不支持流式响应')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  const flush = (block) => {
    const chunk = parseEventBlock(block)
    if (chunk !== null) onMessage(chunk)
  }

  try {
    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      buffer = buffer.replace(/\r\n/g, '\n')

      let boundary = buffer.indexOf('\n\n')
      while (boundary !== -1) {
        flush(buffer.slice(0, boundary))
        buffer = buffer.slice(boundary + 2)
        boundary = buffer.indexOf('\n\n')
      }
    }

    buffer += decoder.decode()
    if (buffer.trim()) flush(buffer)
  } finally {
    reader.releaseLock?.()
  }
}
