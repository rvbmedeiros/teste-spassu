import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useFlowStore } from '../stores/flows'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
  },
}))

import api from '../services/api'

describe('useFlowStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchFlows popula fluxos no sucesso', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: [{
        id: 'create-livro',
        name: 'Criar Livro',
        description: 'Persiste livro',
        steps: [{
          order: 1,
          name: 'Validar',
          description: 'Valida payload',
          rollbackStep: 'N/A',
        }],
      }],
    })

    const store = useFlowStore()
    await store.fetchFlows()

    expect(store.flows).toHaveLength(1)
    expect(store.loadingFlows).toBe(false)
    expect(store.flowsError).toBeNull()
    expect(api.get).toHaveBeenCalledWith('/flows')
  })

  it('fetchFlows define erro quando a API falha', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('Falha ao carregar fluxos'))

    const store = useFlowStore()
    await store.fetchFlows()

    expect(store.flows).toEqual([])
    expect(store.loadingFlows).toBe(false)
    expect(store.flowsError).toBe('Falha ao carregar fluxos')
  })
})
