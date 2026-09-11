/**
 * 应用注册表：主页与聊天页都由这份配置驱动。
 * accent 等颜色 token 通过 data-app 属性注入，见 styles/tokens.css。
 */
export const APPS = {
  love: {
    id: 'love',
    index: '01',
    route: '/love',
    name: 'AI 恋爱大师',
    latin: 'Love Mentor',
    eyebrow: '温暖的 · 关系洞察',
    tagline: '把心事说给懂它的人听，从单身到相守，逐层拆解每一个恋爱难题。',
    blurb: '恋爱心理专家，围绕单身、恋爱、已婚三种状态给出专属建议。',
    accentHint: '情感陪伴',
    // 后端 GET /ai/love_app/chat/sse?message=&chatId=
    endpoint: '/ai/love_app/chat/sse',
    streamMode: 'append', // 逐字追加到一个气泡
    sessionId: true, // 进入页面自动生成会话 id
    placeholder: '说说你遇到的恋爱难题…',
    intro: '你好，我是深耕恋爱心理的 AI 大师。可以先告诉我，你现在的状态是单身、恋爱中，还是已婚？',
    suggestions: [
      '我喜欢的人总是对我忽冷忽热，我该怎么办？',
      '和伴侣因为生活习惯差异频繁争吵，如何沟通？',
      '婚后和长辈相处压力很大，怎么平衡家庭关系？',
    ],
  },
  manus: {
    id: 'manus',
    index: '02',
    route: '/manus',
    name: 'AI 超级智能体',
    latin: 'Manus Agent',
    eyebrow: '自主的 · 任务执行',
    tagline: '会思考、会调用工具的智能体，把复杂目标拆成一步步可执行的行动。',
    blurb: '自主 ReAct 智能体，可联网搜索、抓取网页、读写文件并生成交付物。',
    accentHint: '任务执行',
    // 后端 GET /ai/manus/chat?message=
    endpoint: '/ai/manus/chat',
    streamMode: 'segments', // 每个 SSE 事件是一步，独立成段
    sessionId: false,
    placeholder: '交给我一个任务，例如：帮我调研 2026 年 AI 编程工具并写成报告…',
    intro: '我是 PManus 超级智能体。给我一个目标，我会自行规划、调用工具并执行到底。',
    suggestions: [
      '帮我调研 2026 年主流的 AI 编程工具，并对比优缺点',
      '搜索今天的科技新闻，整理成一份简要日报',
      '帮我写一份旅行计划，并生成 PDF 文件',
    ],
  },
}

export const APP_LIST = [APPS.love, APPS.manus]
