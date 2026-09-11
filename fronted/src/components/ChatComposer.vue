<script setup>
import { nextTick, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '输入消息…' },
  streaming: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'send', 'stop'])

const textarea = ref(null)
let composing = false

function resize() {
  const el = textarea.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 168)}px`
}

watch(
  () => props.modelValue,
  () => nextTick(resize),
)

function onInput(event) {
  emit('update:modelValue', event.target.value)
  resize()
}

function submit() {
  if (props.streaming) return
  const value = props.modelValue.trim()
  if (!value) return
  emit('send', value)
}

function onKeydown(event) {
  if (event.key !== 'Enter') return
  if (event.shiftKey || composing || event.isComposing) return
  event.preventDefault()
  submit()
}

function onCompositionStart() {
  composing = true
}
function onCompositionEnd() {
  composing = false
}
</script>

<template>
  <div class="composer">
    <textarea
      ref="textarea"
      class="composer__input"
      rows="1"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="streaming"
      aria-label="消息输入框"
      @input="onInput"
      @keydown="onKeydown"
      @compositionstart="onCompositionStart"
      @compositionend="onCompositionEnd"
    />

    <div class="composer__bar">
      <span class="composer__hint">Enter 发送 · Shift + Enter 换行</span>

      <button
        v-if="streaming"
        class="composer__btn composer__btn--stop"
        type="button"
        aria-label="停止生成"
        @click="emit('stop')"
      >
        <svg width="12" height="12" viewBox="0 0 12 12" aria-hidden="true">
          <rect x="1.5" y="1.5" width="9" height="9" rx="1.5" fill="currentColor" />
        </svg>
      </button>

      <button
        v-else
        class="composer__btn composer__btn--send"
        type="button"
        :disabled="!modelValue.trim()"
        aria-label="发送消息"
        @click="submit"
      >
        <svg width="16" height="16" viewBox="0 0 18 18" fill="none" aria-hidden="true">
          <path
            d="M9 14.5v-11M4.5 8 9 3.5 13.5 8"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.composer {
  padding: 8px 10px 8px 14px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--line-strong);
  background: var(--surface);
  transition: border-color 0.3s var(--ease), box-shadow 0.3s var(--ease);
}
.composer:focus-within {
  border-color: var(--ink-mute);
  box-shadow: 0 0 0 3px rgba(28, 27, 24, 0.05);
}

.composer__input {
  display: block;
  width: 100%;
  max-height: 168px;
  padding: 8px 4px 4px;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  font-size: 14.5px;
  line-height: 1.7;
  color: var(--ink);
}
.composer__input::placeholder {
  color: var(--ink-mute);
}
.composer__input:disabled {
  opacity: 0.55;
}

.composer__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.composer__hint {
  padding-left: 4px;
  font-size: 11px;
  letter-spacing: 0.03em;
  color: var(--ink-mute);
}

.composer__btn {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 999px;
  transition: background 0.25s var(--ease), color 0.25s var(--ease),
    border-color 0.25s var(--ease), opacity 0.25s var(--ease);
}
.composer__btn--send {
  color: var(--surface);
  background: var(--ink);
}
.composer__btn--send:hover {
  background: #000;
}
.composer__btn--send:disabled {
  opacity: 0.28;
  cursor: not-allowed;
}
.composer__btn--stop {
  color: var(--ink);
  border: 1px solid var(--line-strong);
}
.composer__btn--stop:hover {
  background: var(--surface-2);
}

@media (max-width: 640px) {
  .composer__hint {
    display: none;
  }
  .composer__bar {
    justify-content: flex-end;
  }
}
</style>
