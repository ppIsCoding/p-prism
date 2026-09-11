<script setup>
import { onMounted, ref } from 'vue'
import { APP_LIST } from '@/config/apps'
import http from '@/api/http'

const apps = APP_LIST
const health = ref('checking') // checking | online | offline

const statusText = {
  checking: '检测服务',
  online: '服务在线',
  offline: '服务离线',
}

async function probe() {
  health.value = 'checking'
  try {
    const res = await http.get('/health/')
    health.value = res && res.code === 0 ? 'online' : 'offline'
  } catch {
    health.value = 'offline'
  }
}

onMounted(probe)
</script>

<template>
  <main class="home">
    <header class="masthead">
      <div class="brand">
        P<span class="brand__dot">·</span>PRISM
      </div>

      <div class="masthead__right">
        <span class="masthead__tag">AI 应用工作室</span>
        <button class="status" type="button" @click="probe">
          <span class="status__dot" :class="`status__dot--${health}`" />
          <span class="status__text">{{ statusText[health] }}</span>
        </button>
      </div>
    </header>

    <div class="stage">
      <section class="intro">
        <p class="label">两个应用</p>
        <h1 class="intro__title">
          一束智能，
          <br />
          两种性情。
        </h1>
        <p class="intro__text">
          同一个内核，两种截然不同的能力。一个倾听你的心事，一个替你完成复杂的任务。
        </p>
        <div class="intro__rule" />
        <p class="intro__hint">选择右侧的应用，开始一段对话。</p>
      </section>

      <section class="apps">
        <router-link
          v-for="(app, index) in apps"
          :key="app.id"
          :to="app.route"
          class="applink"
          :data-app="app.id"
          :style="{ animationDelay: `${140 + index * 90}ms` }"
        >
          <span class="applink__rail" />

          <span class="applink__num">{{ app.index }}</span>

          <div class="applink__body">
            <div class="applink__head">
              <h2 class="applink__name">{{ app.name }}</h2>
              <span class="applink__tag">
                <span class="applink__tagdot" />
                {{ app.accentHint }}
              </span>
            </div>
            <p class="applink__desc">{{ app.blurb }}</p>
            <p class="applink__latin">{{ app.latin }}</p>
          </div>

          <span class="applink__arrow" aria-hidden="true">
            <svg width="17" height="17" viewBox="0 0 18 18" fill="none">
              <path
                d="M3.5 9h11M10 4.5 14.5 9 10 13.5"
                stroke="currentColor"
                stroke-width="1.4"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </span>
        </router-link>
      </section>
    </div>

    <footer class="colophon">
      <span class="label">Spring AI × Vue 3</span>
      <span class="mono colophon__endpoint">localhost:8123/api</span>
    </footer>
  </main>
</template>

<style scoped>
.home {
  position: relative;
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  padding: 34px clamp(24px, 5vw, 72px) 30px;
}

/* ---------- Masthead ---------- */
.masthead {
  display: flex;
  align-items: center;
  justify-content: space-between;
  animation: fade 0.7s var(--ease) both;
}
.brand {
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.3em;
  color: var(--ink);
}
.brand__dot {
  color: var(--ink-mute);
  margin: 0 3px;
}

.masthead__right {
  display: flex;
  align-items: center;
  gap: 22px;
}
.masthead__tag {
  font-size: 12px;
  letter-spacing: 0.16em;
  color: var(--ink-mute);
}
.status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--ink-soft);
  transition: color 0.25s var(--ease);
}
.status:hover {
  color: var(--ink);
}
.status__dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: var(--ink-mute);
}
.status__dot--online {
  background: #4f9d73;
}
.status__dot--offline {
  background: #c26b76;
}
.status__dot--checking {
  background: var(--ink-mute);
  animation: blink 1.1s ease-in-out infinite;
}

/* ---------- Stage ---------- */
.stage {
  flex: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.15fr);
  align-items: start;
  gap: clamp(40px, 6vw, 104px);
  padding: clamp(56px, 11vh, 132px) 0 clamp(30px, 6vh, 70px);
}

.intro {
  max-width: 420px;
  animation: rise 0.8s var(--ease) 0.04s both;
}
.intro__title {
  margin: 22px 0 24px;
  font-family: var(--font-display);
  font-weight: 400;
  font-size: clamp(38px, 4.4vw, 58px);
  line-height: 1.16;
  letter-spacing: -0.01em;
  color: var(--ink);
}
.intro__text {
  font-size: 15px;
  line-height: 1.85;
  color: var(--ink-soft);
  max-width: 30ch;
}
.intro__rule {
  width: 46px;
  height: 1px;
  margin: 30px 0 16px;
  background: var(--line-strong);
}
.intro__hint {
  font-size: 12.5px;
  letter-spacing: 0.04em;
  color: var(--ink-mute);
}

/* ---------- App list ---------- */
.apps {
  display: flex;
  flex-direction: column;
  border-top: 1px solid var(--line);
}
.applink {
  position: relative;
  display: grid;
  grid-template-columns: 34px 1fr auto;
  align-items: center;
  gap: 22px;
  padding: 32px 18px 32px 26px;
  border-bottom: 1px solid var(--line);
  animation: rise 0.7s var(--ease) both;
  transition: background 0.4s var(--ease), padding-left 0.4s var(--ease);
}
.applink:hover {
  background: var(--accent-soft);
  padding-left: 32px;
}
.applink:focus-visible {
  outline: 2px solid var(--accent);
  outline-offset: -2px;
}

.applink__rail {
  position: absolute;
  left: 0;
  top: 50%;
  width: 3px;
  height: 0;
  border-radius: 999px;
  background: var(--accent);
  transform: translateY(-50%);
  transition: height 0.4s var(--ease);
}
.applink:hover .applink__rail {
  height: 52%;
}

.applink__num {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--ink-mute);
  transition: color 0.35s var(--ease);
}
.applink:hover .applink__num {
  color: var(--accent);
}

.applink__head {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin-bottom: 10px;
}
.applink__name {
  font-family: var(--font-display);
  font-weight: 500;
  font-size: clamp(23px, 2.4vw, 30px);
  line-height: 1.15;
  color: var(--ink);
}
.applink__tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  letter-spacing: 0.1em;
  color: var(--ink-mute);
}
.applink__tagdot {
  width: 5px;
  height: 5px;
  border-radius: 999px;
  background: var(--accent);
}
.applink__desc {
  font-size: 14px;
  line-height: 1.75;
  color: var(--ink-soft);
  max-width: 44ch;
}
.applink__latin {
  margin-top: 12px;
  font-family: var(--font-display);
  font-style: italic;
  font-size: 14px;
  color: var(--ink-mute);
}

.applink__arrow {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 999px;
  border: 1px solid var(--line-strong);
  color: var(--ink);
  transition: background 0.35s var(--ease), color 0.35s var(--ease),
    border-color 0.35s var(--ease), transform 0.35s var(--ease);
}
.applink:hover .applink__arrow {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--surface);
  transform: translateX(5px);
}

/* ---------- Colophon ---------- */
.colophon {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 24px;
  border-top: 1px solid var(--line);
  animation: fade 1s var(--ease) 0.5s both;
}
.colophon__endpoint {
  color: var(--ink-mute);
  font-size: 11px;
}

@media (max-width: 900px) {
  .stage {
    grid-template-columns: 1fr;
    gap: 44px;
    padding-top: 64px;
  }
  .intro {
    max-width: none;
  }
  .intro__title {
    font-size: clamp(38px, 10vw, 56px);
  }
  .masthead__tag {
    display: none;
  }
  .applink {
    grid-template-columns: 26px 1fr;
    padding: 26px 8px 26px 18px;
  }
  .applink__arrow {
    display: none;
  }
}
</style>
