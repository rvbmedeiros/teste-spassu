import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import api from '@/services/api'
import { extractApiErrorMessage } from '@/services/errors'

export interface LogEntry {
  sequence: number
  source: 'bff' | 'orchestration' | 'microservice' | string
  timestamp: string
  level: 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR'
  logger: string
  message: string
  thread: string
  requestId: string | null
  traceId: string | null
}

export type LogSource = 'all' | 'bff' | 'orchestration' | 'microservice'
export type LogLevel = 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR'

const POLL_INTERVAL_MS = 5000

export const useLogsStore = defineStore('logs', () => {
  const logs = ref<LogEntry[]>([])
  const loading = ref(false)
  const connected = ref(false)
  const polling = ref(false)
  const error = ref<string | null>(null)
  const source = ref<LogSource>('all')
  const level = ref<LogLevel>('INFO')
  const search = ref('')
  const limit = ref(50)
  const lastUpdatedAt = ref<string | null>(null)

  let timerId: number | null = null

  const errorCount = computed(() => logs.value.filter(entry => entry.level === 'ERROR').length)
  const activeSources = computed(() => new Set(logs.value.map(entry => entry.source)).size)

  async function fetchLogs(translate?: (key: string) => string): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const { data } = await api.get<LogEntry[]>('/api/logs', {
        params: {
          source: source.value,
          level: level.value,
          limit: limit.value,
          search: search.value.trim() || undefined,
        },
      })

      logs.value = data
      connected.value = true
      lastUpdatedAt.value = new Date().toISOString()
    } catch (e: unknown) {
      connected.value = false
      error.value = extractApiErrorMessage(e, translate)
    } finally {
      loading.value = false
    }
  }

  function startPolling(translate?: (key: string) => string): void {
    if (timerId !== null) {
      return
    }

    polling.value = true
    void fetchLogs(translate)
    timerId = window.setInterval(() => {
      void fetchLogs(translate)
    }, POLL_INTERVAL_MS)
  }

  function stopPolling(): void {
    polling.value = false
    if (timerId !== null) {
      window.clearInterval(timerId)
      timerId = null
    }
  }

  function reconnect(translate?: (key: string) => string): void {
    stopPolling()
    startPolling(translate)
  }

  function clear(): void {
    logs.value = []
  }

  return {
    logs,
    loading,
    connected,
    polling,
    error,
    source,
    level,
    search,
    limit,
    lastUpdatedAt,
    errorCount,
    activeSources,
    fetchLogs,
    startPolling,
    stopPolling,
    reconnect,
    clear,
  }
})