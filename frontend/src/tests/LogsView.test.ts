import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import LogsView from '@/views/LogsView.vue'

const logsStore = vi.hoisted(() => ({
  logs: [{
    sequence: 1,
    source: 'bff',
    timestamp: '2026-03-20T18:00:00Z',
    level: 'ERROR',
    logger: 'com.spassu.LogsController',
    message: 'Falha no proxy',
    thread: 'main',
    requestId: 'req-1',
    traceId: 'trace-1',
  }],
  loading: false,
  connected: true,
  polling: true,
  error: null,
  source: 'all',
  level: 'INFO',
  search: '',
  limit: 150,
  lastUpdatedAt: '2026-03-20T18:01:00Z',
  errorCount: 1,
  activeSources: 1,
  fetchLogs: vi.fn(),
  startPolling: vi.fn(),
  stopPolling: vi.fn(),
  reconnect: vi.fn(),
  clear: vi.fn(),
}))

vi.mock('@/stores/logs', () => ({
  useLogsStore: () => logsStore,
}))

describe('LogsView', () => {
  beforeEach(() => {
    logsStore.fetchLogs.mockReset()
    logsStore.startPolling.mockReset()
    logsStore.stopPolling.mockReset()
  })

  it('renderiza logs agregados e inicia polling', () => {
    const wrapper = mount(LogsView)

    expect(logsStore.startPolling).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('Central de Logs')
    expect(wrapper.text()).toContain('Falha no proxy')
    expect(wrapper.text()).toContain('req-1')
  })
})