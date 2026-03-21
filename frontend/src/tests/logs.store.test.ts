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
})