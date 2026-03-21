<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { MoonStar, SunMedium } from 'lucide-vue-next'
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()
const { t } = useI18n()

onMounted(() => ui.applyStoredTheme())
</script>

<template>
  <button
    class="group inline-flex items-center gap-2 rounded-full border border-(--ui-border-strong) bg-(--ui-surface-interactive) px-1.5 py-1 shadow-(--shadow-soft) transition-all duration-200 hover:-translate-y-0.5 hover:bg-(--ui-surface-interactive-hover)"
    :aria-label="ui.theme === 'dark' ? t('common.a11y.switchToLight') : t('common.a11y.switchToDark')"
    @click="ui.toggleTheme"
  >
    <span
      class="flex h-8 w-8 items-center justify-center rounded-full transition-colors duration-200"
      :class="ui.theme === 'light' ? 'bg-(--ui-warning-soft) text-(--ui-warning)' : 'text-(--ui-placeholder)'"
    >
      <SunMedium class="h-4 w-4" />
    </span>
    <span class="relative inline-flex h-8 w-14 items-center rounded-full border border-(--ui-border) bg-(--ui-panel-muted) px-1 transition-colors">
      <span
        class="absolute h-6 w-6 rounded-full bg-(--ui-brand) shadow-(--ui-shadow-brand) transition-transform duration-200"
        :class="ui.theme === 'dark' ? 'translate-x-6' : 'translate-x-0'"
      />
      <span class="flex w-full items-center justify-between px-1 text-[10px] font-semibold uppercase tracking-[0.2em] text-(--ui-placeholder)">
        <span>L</span>
        <span>D</span>
      </span>
    </span>
    <span
      class="flex h-8 w-8 items-center justify-center rounded-full transition-colors duration-200"
      :class="ui.theme === 'dark' ? 'bg-(--ui-brand-soft) text-(--ui-brand)' : 'text-(--ui-placeholder)'"
    >
      <MoonStar class="h-4 w-4" />
    </span>
  </button>
</template>
