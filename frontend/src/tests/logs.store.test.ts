import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useLogsStore } from '@/stores/logs'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
  },
}))

import api from '@/services/api'

describe('useLogsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  it('fetchLogs popula a lista agregada', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: [{
        sequence: 1,
        source: 'bff',
        timestamp: '2026-03-20T18:00:00Z',
        level: 'INFO',
        logger: 'com.spassu.Logs',
        message: 'Proxying request',
        thread: 'main',
        requestId: 'req-1',
        traceId: 'trace-1',
      }],
    })

    const store = useLogsStore()
    await store.fetchLogs()

    expect(store.logs).toHaveLength(1)
    expect(store.connected).toBe(true)
    expect(store.error).toBeNull()
  })

  it('fetchLogs define erro quando a API falha', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('Falha na API'))

    const store = useLogsStore()
    await store.fetchLogs()

    expect(store.connected).toBe(false)
    expect(store.error).toBe('Falha na API')
  })

  it('fetchLogs envia filtros configurados e atualiza agregados', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: [
        {
          sequence: 1,
          source: 'bff',
          timestamp: '2026-03-20T18:00:00Z',
          level: 'ERROR',
          logger: 'com.spassu.Logs',
          message: 'Erro 1',
          thread: 'main',
          requestId: 'req-1',
          traceId: 'trace-1',
        },
        {
          sequence: 2,
          source: 'microservice',
          timestamp: '2026-03-20T18:01:00Z',
          level: 'INFO',
          logger: 'com.spassu.Logs',
          message: 'Info 2',
          thread: 'main',
          requestId: 'req-2',
          traceId: 'trace-2',
        },
      ],
    })

    const store = useLogsStore()
    store.source = 'microservice'
    store.level = 'ERROR'
    store.limit = 10
    store.search = '  falha  '

    await store.fetchLogs()

    expect(api.get).toHaveBeenCalledWith('/api/logs', {
      params: {
        source: 'microservice',
        level: 'ERROR',
        limit: 10,
        search: 'falha',
      },
    })
    expect(store.errorCount).toBe(1)
    expect(store.activeSources).toBe(2)
    expect(store.lastUpdatedAt).not.toBeNull()
  })

  it('startPolling deve buscar imediatamente e evitar intervalos duplicados', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] })
    const store = useLogsStore()

    store.startPolling()
    store.startPolling()
    await Promise.resolve()

    expect(store.polling).toBe(true)
    expect(api.get).toHaveBeenCalledTimes(1)

    vi.advanceTimersByTime(5000)
    await Promise.resolve()

    expect(api.get).toHaveBeenCalledTimes(2)
  })

  it('reconnect deve reiniciar polling e clear deve limpar logs', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] })
    const store = useLogsStore()
    store.logs = [
      {
        sequence: 1,
        source: 'bff',
        timestamp: '2026-03-20T18:00:00Z',
        level: 'INFO',
        logger: 'com.spassu.Logs',
        message: 'Proxying request',
        thread: 'main',
        requestId: 'req-1',
        traceId: 'trace-1',
      },
    ]

    store.startPolling()
    await Promise.resolve()
    store.reconnect()
    await Promise.resolve()
    store.clear()
    store.stopPolling()

    expect(api.get).toHaveBeenCalledTimes(2)
    expect(store.logs).toHaveLength(0)
    expect(store.polling).toBe(false)
  })
})