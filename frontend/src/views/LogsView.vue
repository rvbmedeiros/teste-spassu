<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  Activity,
  AlertTriangle,
  Clock3,
  DatabaseZap,
  Radio,
  RefreshCw,
  RotateCcw,
  Search,
} from 'lucide-vue-next'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import { useLogsStore, type LogEntry, type LogLevel, type LogSource } from '@/stores/logs'

const { t, d } = useI18n()
const store = useLogsStore()

   const sourceOptions = computed<Array<{ value: LogSource; label: string }>>(() => [
     { value: 'all', label: t('logs.service.all') },
     { value: 'bff', label: t('logs.service.bff') },
     { value: 'orchestration', label: t('logs.service.orchestration') },
     { value: 'microservice', label: t('logs.service.microservice') },
   ])

const levelOptions: LogLevel[] = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR']

onMounted(() => {
  store.startPolling(t)
})

onUnmounted(() => {
  store.stopPolling()
})

const totalLogs = computed(() => store.logs.length)

const formattedUpdatedAt = computed(() => {
  if (!store.lastUpdatedAt) {
    return null
  }

     return new Date(store.lastUpdatedAt).toLocaleTimeString()
})

function applyFilters(): void {
  void store.fetchLogs(t)
}

function levelClass(level: LogEntry['level']): string {
  return {
    TRACE: 'bg-slate-200 text-slate-700 dark:bg-white/10 dark:text-slate-200',
    DEBUG: 'bg-(--ui-brand-soft) text-(--ui-brand)',
    INFO: 'bg-emerald-500/12 text-emerald-700 dark:text-emerald-300',
    WARN: 'bg-amber-500/12 text-amber-700 dark:text-amber-300',
    ERROR: 'bg-(--ui-danger-soft) text-(--ui-danger)',
  }[level]
}
</script>

<template>
  <div class="page-shell">
    <section class="surface-panel-strong rounded-4xl p-6 sm:p-8">
      <div class="flex flex-col gap-6 xl:flex-row xl:items-end xl:justify-between">
        <div class="space-y-4">
          <span class="section-eyebrow">
            <Activity class="h-3.5 w-3.5" />
            {{ t('nav.logs') }}
          </span>
          <div class="space-y-2">
            <h1 class="page-title">{{ t('logs.title') }}</h1>
            <p class="page-subtitle">{{ t('logs.subtitle') }}</p>
          </div>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <span :class="['chip', store.connected ? 'text-emerald-600 dark:text-emerald-400' : 'text-(--ui-text-muted)']">
            <Radio class="h-3.5 w-3.5" :class="store.connected && store.polling ? 'animate-pulse text-emerald-500' : 'text-slate-400'" />
            {{ store.polling ? t('logs.pollingOn') : t('logs.pollingOff') }}
          </span>
          <BaseButton variant="secondary" size="sm" @click="store.reconnect(t)">
            <template #leading>
              <RefreshCw />
            </template>
            {{ t('common.reconnect') }}
          </BaseButton>
          <BaseButton variant="secondary" size="sm" @click="store.polling ? store.stopPolling() : store.startPolling(t)">
            <template #leading>
              <Clock3 />
            </template>
            {{ store.polling ? t('common.pausar') : t('common.retomar') }}
          </BaseButton>
          <BaseButton variant="secondary" size="sm" @click="store.clear">
            <template #leading>
              <RotateCcw />
            </template>
            {{ t('common.clear') }}
          </BaseButton>
        </div>
      </div>

      <div class="mt-8 grid gap-4 md:grid-cols-3">
        <div class="metric-tile">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-(--ui-text-muted)">{{ t('logs.totalLogs') }}</span>
            <Activity class="h-4 w-4 text-(--ui-brand)" />
          </div>
          <p class="mt-4 text-3xl font-semibold tracking-tight text-(--ui-text)">{{ totalLogs }}</p>
        </div>

        <div class="metric-tile">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-(--ui-text-muted)">{{ t('logs.errorLogs') }}</span>
            <AlertTriangle class="h-4 w-4 text-(--ui-danger)" />
          </div>
          <p class="mt-4 text-3xl font-semibold tracking-tight text-(--ui-danger)">{{ store.errorCount }}</p>
        </div>

        <div class="metric-tile">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-(--ui-text-muted)">{{ t('logs.activeSources') }}</span>
            <DatabaseZap class="h-4 w-4 text-(--ui-brand)" />
          </div>
          <p class="mt-4 text-3xl font-semibold tracking-tight text-(--ui-text)">{{ store.activeSources }}</p>
        </div>
      </div>
    </section>

    <BaseCard :title="t('logs.filters')">
      <div class="grid gap-4 lg:grid-cols-[1fr_11rem_11rem_auto] lg:items-end">
        <BaseInput
          v-model.trim="store.search"
          type="search"
          :label="t('common.buscar')"
          :placeholder="t('logs.search')"
          @keyup.enter="applyFilters"
        >
          <template #leading>
            <Search />
          </template>
        </BaseInput>

        <div class="flex flex-col gap-2">
          <label class="text-sm font-medium text-(--ui-text)">{{ t('logs.source') }}</label>
          <select v-model="store.source" class="w-full rounded-2xl border border-(--ui-border-strong) bg-white/75 px-4 py-3 text-sm text-(--ui-text) shadow-(--shadow-soft) outline-none transition-colors duration-200 dark:bg-white/6">
            <option v-for="option in sourceOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </div>

        <div class="flex flex-col gap-2">
          <label class="text-sm font-medium text-(--ui-text)">{{ t('logs.level') }}</label>
          <select v-model="store.level" class="w-full rounded-2xl border border-(--ui-border-strong) bg-white/75 px-4 py-3 text-sm text-(--ui-text) shadow-(--shadow-soft) outline-none transition-colors duration-200 dark:bg-white/6">
            <option v-for="option in levelOptions" :key="option" :value="option">{{ option }}</option>
          </select>
        </div>

        <BaseButton @click="applyFilters">{{ t('logs.applyFilters') }}</BaseButton>
      </div>

      <p v-if="formattedUpdatedAt" class="mt-4 text-xs text-(--ui-text-muted)">{{ t('logs.updatedAt') }}: {{ formattedUpdatedAt }}</p>
    </BaseCard>

    <BaseCard :title="`${t('nav.logs')} (${store.logs.length})`" :padding="false">
      <div v-if="store.loading && !store.logs.length" class="flex min-h-[24rem] flex-col items-center justify-center gap-3 px-6 text-(--ui-text-muted)">
        <Activity class="h-8 w-8 animate-pulse text-(--ui-brand)" />
        <p class="text-sm font-medium">{{ t('common.loading') }}</p>
      </div>

      <div v-else-if="store.error && !store.logs.length" class="flex min-h-[24rem] flex-col items-center justify-center gap-4 px-6 text-center">
        <span class="rounded-3xl bg-(--ui-danger-soft) p-4 text-(--ui-danger)">
          <AlertTriangle class="h-6 w-6" />
        </span>
        <div class="space-y-1">
          <p class="text-sm font-semibold text-(--ui-text)">{{ t('common.erro') }}</p>
          <p class="text-xs text-(--ui-text-muted)">{{ store.error }}</p>
        </div>
        <BaseButton variant="secondary" size="sm" @click="store.fetchLogs(t)">
          <template #leading>
            <RefreshCw />
          </template>
          {{ t('common.retry') }}
        </BaseButton>
      </div>

      <div v-else-if="!store.logs.length" class="flex min-h-[24rem] flex-col items-center justify-center gap-3 px-6 text-center text-(--ui-text-muted)">
        <Activity class="h-8 w-8 text-(--ui-brand)" />
        <p class="max-w-xs text-sm font-medium">{{ t('logs.empty') }}</p>
      </div>

      <div v-else class="max-h-[42rem] divide-y divide-(--ui-border) overflow-y-auto">
        <article v-for="entry in store.logs" :key="`${entry.source}-${entry.sequence}`" class="grid gap-4 px-5 py-4 lg:grid-cols-[8rem_minmax(0,1fr)_17rem] lg:items-start">
          <div class="space-y-2">
            <span :class="['inline-flex rounded-full px-2.5 py-1 text-[11px] font-semibold uppercase tracking-[0.16em]', levelClass(entry.level)]">{{ entry.level }}</span>
            <p class="text-xs font-medium uppercase tracking-[0.16em] text-(--ui-text-muted)">{{ entry.source }}</p>
          </div>

          <div class="min-w-0 space-y-2">
            <p class="text-sm font-semibold text-(--ui-text)">{{ entry.message }}</p>
            <p class="truncate text-xs text-(--ui-text-muted)">{{ t('logs.logger') }}: {{ entry.logger }}</p>
            <div class="flex flex-wrap gap-2 text-xs text-(--ui-text-muted)">
              <span v-if="entry.requestId" class="chip">{{ t('logs.requestId') }}: {{ entry.requestId }}</span>
              <span v-if="entry.traceId" class="chip">{{ t('logs.traceId') }}: {{ entry.traceId }}</span>
            </div>
          </div>

          <div class="space-y-1 text-xs text-(--ui-text-muted) lg:text-right">
            <p>{{ new Date(entry.timestamp).toLocaleString() }}</p>
            <p>{{ entry.thread }}</p>
          </div>
        </article>
      </div>
    </BaseCard>
  </div>
</template>