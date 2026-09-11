<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import ChatBubble from '@/components/ChatBubble.vue'
import ChatComposer from '@/components/ChatComposer.vue'
import { APPS } from '@/config/apps'
import { useChat } from '@/composables/useChat'

const props = defineProps({
  appId: { type: String, required: true },
})

const router = useRouter()
const app = APPS[props.appId]

if (!app) {
  router.replace('/')
}

const { messages, isStreaming, errorMessage, chatId, send, stop, newSession } =
  useChat(app)

const draft = ref('')
const scroller = ref(null)
const pinned = ref(true)
const sessionCopied = ref(false)

const isStepwise = computed(() => app.streamMode === 'segments')
const statusText = computed(() => {
  if (!isStreaming.value) return '就绪'
  return isStepwise.value ? '智能体执行中' : '正在回复'
})

function handleSend(text) {
  draft.value = ''
  pinned.value = true
  send(text)
  scrollToBottom()
}

function onScroll() {
  const el = scroller.value
  if (!el) return
  pinned.value = el.scrollHeight - el.scrollTop - el.clientHeight < 90
}

function scrollToBottom() {
  nextTick(() => {
    const el = scroller.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function onNewSession() {
  newSession()
  draft.value = ''
  pinned.value = true
  scrollToBottom()
}

async function copySession() {
  try {
    await navigator.clipboard.writeText(chatId.value)
    sessionCopied.value = true
    setTimeout(() => (sessionCopied.value = false), 1400)
  } catch {
    /* ignore */
  }
}

watch(
  messages,
  () => {
    if (pinned.value) scrollToBottom()
  },
  { deep: true },
)
</script>

<template>
  <div v-if="app" class="chat" :data-app="app.id">
    <aside class="panel">
      <router-link to="/" class="panel__back">
        <svg width="15" height="15" viewBox="0 0 18 18" fill="none" aria-hidden="true">
          <path
            d="M14.5 9h-11M8 4.5 3.5 9 8 13.5"
            stroke="currentColor"
            stroke-width="1.4"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span>返回</span>
      </router-link>

      <div class="panel__head">
        <span class="label">应用 {{ app.index }}</span>
        <h1 class="panel__name">{{ app.name }}</h1>
        <p class="panel__latin">{{ app.latin }}</p>
        <p class="panel__desc">{{ app.tagline }}</p>
      </div>

      <div class="panel__session">
        <template v-if="app.sessionId">
          <span class="label">会话 ID</span>
          <button class="panel__id" type="button" @click="copySession">
            <span class="panel__idtext">{{ chatId }}</span>
            <span class="panel__copy">{{ sessionCopied ? '已复制' : '复制' }}</span>
          </button>
        </template>
        <template v-else>
          <span class="label">执行模式</span>
          <span class="panel__mode">自主规划 · 工具调用</span>
        </template>
      </div>

      <div class="panel__foot">
        <button class="btn btn--solid panel__new" type="button" @click="onNewSession">
          新建对话
        </button>
        <router-link to="/" class="panel__switch">切换应用</router-link>
      </div>
    </aside>

    <main class="room">
      <header class="room__top">
        <div class="room__state">
          <span class="room__dot" :class="{ 'room__dot--live': isStreaming }" />
          <span>{{ statusText }}</span>
        </div>
        <span class="label">{{ app.eyebrow }}</span>
      </header>

      <div ref="scroller" class="room__scroll" @scroll="onScroll">
        <div class="room__inner">
          <section v-if="!messages.length" class="welcome">
            <p class="label">开始对话</p>
            <p class="welcome__lead">{{ app.intro }}</p>

            <div class="suggest">
              <p class="label">可以这样开始</p>
              <button
                v-for="(item, index) in app.suggestions"
                :key="index"
                class="suggest__item"
                type="button"
                @click="handleSend(item)"
              >
                <span class="suggest__index mono">{{ String(index + 1).padStart(2, '0') }}</span>
                <span class="suggest__text">{{ item }}</span>
                <span class="suggest__arrow" aria-hidden="true">→</span>
              </button>
            </div>
          </section>

          <div v-else class="list">
            <ChatBubble
              v-for="message in messages"
              :key="message.id"
              :message="message"
              :app="app"
            />
          </div>
        </div>
      </div>

      <footer class="room__dock">
        <div class="room__composer">
          <ChatComposer
            v-model="draft"
            :placeholder="app.placeholder"
            :streaming="isStreaming"
            @send="handleSend"
            @stop="stop"
          />
          <p v-if="errorMessage" class="room__error">{{ errorMessage }}</p>
        </div>
      </footer>
    </main>
  </div>
</template>

<style scoped>
.chat {
  height: 100%;
  display: grid;
  grid-template-columns: 306px 1fr;
  grid-template-rows: minmax(0, 1fr);
  overflow: hidden;
  background: var(--bg);
}

/* ---------- Sidebar ---------- */
.panel {
  display: flex;
  flex-direction: column;
  gap: 30px;
  padding: 30px 26px;
  border-right: 1px solid var(--line);
  min-height: 0;
  overflow-y: auto;
}
.panel__back {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  width: fit-content;
  font-size: 13px;
  color: var(--ink-soft);
  transition: color 0.25s var(--ease), gap 0.25s var(--ease);
}
.panel__back:hover {
  color: var(--ink);
  gap: 10px;
}

.panel__head {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 10px;
}
.panel__name {
  margin-top: 12px;
  font-family: var(--font-display);
  font-weight: 500;
  font-size: 30px;
  line-height: 1.18;
  color: var(--ink);
}
.panel__latin {
  font-family: var(--font-display);
  font-style: italic;
  font-size: 15px;
  color: var(--accent);
}
.panel__desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--ink-soft);
}

.panel__session {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  border-radius: var(--radius);
  border: 1px solid var(--line);
  background: var(--surface);
}
.panel__id {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  text-align: left;
  color: var(--ink-soft);
}
.panel__idtext {
  font-family: var(--font-mono);
  font-size: 11.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.panel__copy {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--ink-mute);
  opacity: 0;
  transition: opacity 0.25s var(--ease), color 0.25s var(--ease);
}
.panel__id:hover .panel__copy {
  opacity: 1;
  color: var(--ink);
}
.panel__mode {
  font-size: 13.5px;
  color: var(--ink);
}

.panel__foot {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
}
.panel__new {
  width: 100%;
}
.panel__switch {
  text-align: center;
  font-size: 12.5px;
  color: var(--ink-mute);
  transition: color 0.25s var(--ease);
}
.panel__switch:hover {
  color: var(--ink);
}

/* ---------- Room ---------- */
.room {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow: hidden;
  background: var(--surface);
}
.room__top {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 34px;
  border-bottom: 1px solid var(--line);
}
.room__state {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 12.5px;
  color: var(--ink-soft);
}
.room__dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: var(--ink-mute);
  transition: background 0.3s var(--ease);
}
.room__dot--live {
  background: var(--accent);
  animation: blink 1.2s ease-in-out infinite;
}

.room__scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
}
.room__inner {
  max-width: 780px;
  margin: 0 auto;
  padding: 36px 34px 24px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ---------- Welcome ---------- */
.welcome {
  animation: rise 0.55s var(--ease) both;
}
.welcome__lead {
  margin: 20px 0 44px;
  font-family: var(--font-display);
  font-weight: 400;
  font-size: clamp(22px, 2.6vw, 30px);
  line-height: 1.55;
  color: var(--ink);
  max-width: 26ch;
}

.suggest {
  display: flex;
  flex-direction: column;
  gap: 2px;
  border-top: 1px solid var(--line);
}
.suggest > .label {
  padding: 20px 0 12px;
}
.suggest__item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 15px 6px;
  text-align: left;
  border-bottom: 1px solid var(--line);
  transition: padding-left 0.3s var(--ease), color 0.3s var(--ease);
}
.suggest__item:hover {
  padding-left: 14px;
}
.suggest__index {
  color: var(--ink-mute);
  font-size: 11px;
  transition: color 0.3s var(--ease);
}
.suggest__item:hover .suggest__index {
  color: var(--accent);
}
.suggest__text {
  flex: 1;
  font-size: 14px;
  color: var(--ink-soft);
  transition: color 0.3s var(--ease);
}
.suggest__item:hover .suggest__text {
  color: var(--ink);
}
.suggest__arrow {
  color: var(--ink-mute);
  transition: color 0.3s var(--ease), transform 0.3s var(--ease);
}
.suggest__item:hover .suggest__arrow {
  color: var(--ink);
  transform: translateX(3px);
}

/* ---------- Dock ---------- */
.room__dock {
  flex-shrink: 0;
  padding: 16px 34px 22px;
  border-top: 1px solid var(--line);
  background: var(--surface);
}
.room__composer {
  max-width: 780px;
  margin: 0 auto;
}
.room__error {
  margin-top: 10px;
  text-align: center;
  font-size: 12.5px;
  color: #b04a58;
}

@media (max-width: 860px) {
  .chat {
    grid-template-columns: 1fr;
  }
  .panel {
    display: none;
  }
  .room__top {
    padding: 16px 20px;
  }
  .room__inner {
    padding: 26px 20px 16px;
  }
  .room__dock {
    padding: 12px 20px 18px;
  }
}
</style>
