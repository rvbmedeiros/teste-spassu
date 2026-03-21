<script setup lang="ts">
import { onMounted } from 'vue'
import { MoonStar, SunMedium } from 'lucide-vue-next'
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()

onMounted(() => ui.applyStoredTheme())
</script>

<template>
  <button
    class="group inline-flex items-center gap-2 rounded-full border border-[color:var(--ui-border-strong)] bg-white/75 px-1.5 py-1 shadow-[var(--shadow-soft)] transition-all duration-200 hover:-translate-y-0.5 dark:bg-white/6"
    :aria-label="ui.theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'"
    @click="ui.toggleTheme"
  >
    <span
      class="flex h-8 w-8 items-center justify-center rounded-full transition-colors duration-200"
      :class="ui.theme === 'light' ? 'bg-amber-100 text-amber-600' : 'text-slate-400'"
    >
      <SunMedium class="h-4 w-4" />
    </span>
    <span class="relative inline-flex h-8 w-14 items-center rounded-full bg-slate-200/80 px-1 transition-colors dark:bg-slate-800">
      <span
        class="absolute h-6 w-6 rounded-full bg-[color:var(--ui-brand)] shadow-[0_8px_18px_rgba(37,99,235,0.28)] transition-transform duration-200"
        :class="ui.theme === 'dark' ? 'translate-x-6' : 'translate-x-0'"
      />
      <span class="flex w-full items-center justify-between px-1 text-[10px] font-semibold uppercase tracking-[0.2em] text-slate-500 dark:text-slate-400">
        <span>L</span>
        <span>D</span>
      </span>
    </span>
    <span
      class="flex h-8 w-8 items-center justify-center rounded-full transition-colors duration-200"
      :class="ui.theme === 'dark' ? 'bg-blue-500/14 text-blue-300' : 'text-slate-400'"
    >
      <MoonStar class="h-4 w-4" />
    </span>
  </button>
</template>
