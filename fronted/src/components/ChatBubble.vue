<script setup>
import { computed, ref } from 'vue'
import MarkdownView from './MarkdownView.vue'

const props = defineProps({
  message: { type: Object, required: true },
  app: { type: Object, required: true },
})

const isUser = computed(() => props.message.role === 'user')
const isStepwise = computed(() => props.app.streamMode === 'segments')

const plainText = computed(() => (props.message.segments || []).join('\n\n'))

const copied = ref(false)
async function copy() {
  try {
    await navigator.clipboard.writeText(props.message.text || plainText.value)
    copied.value = true
    setTimeout(() => (copied.value = false), 1400)
  } catch {
    /* clipboard unavailable */
  }
}
</script>

<template>
  <div class="row" :class="isUser ? 'row--user' : 'row--ai'">
    <div v-if="!isUser" class="avatar mono">{{ app.index }}</div>

    <div class="stack">
      <div v-if="!isUser" class="meta">
        <span class="meta__name">{{ app.name }}</span>
        <button class="meta__copy" type="button" @click="copy">
          {{ copied ? '已复制' : '复制' }}
        </button>
      </div>

      <div class="bubble" :class="isUser ? 'bubble--user' : 'bubble--ai'">
        <template v-if="isUser">
          <p class="user-text">{{ message.text }}</p>
        </template>

        <template v-else>
          <template v-if="isStepwise">
            <div
              v-for="(segment, index) in message.segments"
              :key="index"
              class="step"
            >
              <span class="step__tag">第 {{ index + 1 }} 步</span>
              <MarkdownView :content="segment" />
            </div>
          </template>

          <MarkdownView
            v-else
            :content="message.segments && message.segments[0] ? message.segments[0] : ''"
          />

          <span v-if="message.streaming" class="caret" aria-hidden="true" />

          <p v-if="message.error" class="error">{{ message.error }}</p>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.row {
  display: flex;
  gap: 13px;
  animation: rise 0.4s var(--ease) both;
}
.row--user {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  margin-top: 26px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: var(--surface-2);
  color: var(--accent-ink);
  font-size: 11px;
  font-weight: 500;
}

.stack {
  display: flex;
  flex-direction: column;
  gap: 7px;
  max-width: min(78%, 720px);
}
.row--user .stack {
  align-items: flex-end;
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-left: 2px;
}
.meta__name {
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.02em;
  color: var(--ink-soft);
}
.meta__copy {
  font-size: 11px;
  letter-spacing: 0.04em;
  color: var(--ink-mute);
  opacity: 0;
  transition: opacity 0.25s var(--ease), color 0.25s var(--ease);
}
.row:hover .meta__copy {
  opacity: 1;
}
.meta__copy:hover {
  color: var(--ink);
}

.bubble {
  padding: 14px 18px;
  border-radius: 4px 14px 14px 14px;
}
.bubble--ai {
  background: var(--surface-2);
  border: 1px solid var(--line);
}
.bubble--user {
  border-radius: 14px 4px 14px 14px;
  background: var(--ink);
  color: #f4f2ed;
}
.bubble--user :deep(.user-text) {
  margin: 0;
  font-size: 14.5px;
  line-height: 1.75;
  color: #f4f2ed;
  white-space: pre-wrap;
  word-break: break-word;
}

.step + .step {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--line-strong);
}
.step__tag {
  display: block;
  margin-bottom: 6px;
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--accent);
}

.caret {
  display: inline-block;
  width: 7px;
  height: 16px;
  margin-left: 3px;
  vertical-align: text-bottom;
  border-radius: 1px;
  background: var(--accent);
  animation: blink 1.05s steps(1, end) infinite;
}

.error {
  margin: 8px 0 0;
  font-size: 13px;
  color: #b04a58;
}
</style>
