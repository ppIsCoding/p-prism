import { marked } from 'marked'
import DOMPurify from 'dompurify'

marked.setOptions({
  gfm: true,
  breaks: true,
})

/**
 * 将 AI 返回的 Markdown 渲染为可安全插入的 HTML。
 */
export function renderMarkdown(text) {
  if (!text) return ''
  const html = marked.parse(text)
  return DOMPurify.sanitize(html, { USE_PROFILES: { html: true } })
}
