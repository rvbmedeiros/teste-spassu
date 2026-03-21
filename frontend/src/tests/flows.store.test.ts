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
        owner: 'orchestration-team',
        version: '1.0.0',
        domainTag: 'livros',
        businessGoal: 'Cadastrar livro',
        nodes: [{
          nodeId: 'step-1',
          type: 'ACTIVITY',
          order: 1,
          name: 'Validar',
          description: 'Valida payload',
          purpose: 'Validar dados',
          inputHint: 'LivroRequest',
          outputHint: 'Payload valido',
          failureHint: '422',
          rollbackStep: 'N/A',
        }],
        edges: [],
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

  it('fetchFlows converte payload legado com steps para nodes e edges', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: [{
        id: 'create-assunto',
        name: 'Criar Assunto',
        description: 'Fluxo legado',
        steps: [
          {
            order: 1,
            name: 'Validar payload',
            description: 'Valida request',
          },
          {
            order: 2,
            name: 'Enviar para microservice',
            description: 'RPC para microservice',
          },
        ],
      }],
    })

    const store = useFlowStore()
    await store.fetchFlows()

    expect(store.flows).toHaveLength(1)
    expect(store.flows[0].nodes).toHaveLength(2)
    expect(store.flows[0].nodes[0].nodeId).toBe('step-1')
    expect(store.flows[0].nodes[1].nodeId).toBe('step-2')
    expect(store.flows[0].edges).toEqual([
      { from: 'step-1', to: 'step-2', label: '', edgeIntent: '' },
    ])
  })
})
