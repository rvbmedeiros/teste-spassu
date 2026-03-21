import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FlowCockpitView from '../views/FlowCockpitView.vue'

const flowStore = vi.hoisted(() => ({
  flows: [{
    id: 'create-livro',
    name: 'Criar Livro',
    description: 'Persiste livro',
    steps: [
      { order: 1, name: 'Validar payload', description: 'Bean validation', rollbackStep: 'N/A' },
      { order: 2, name: 'Persistir', description: 'POST /api/livros', rollbackStep: 'Excluir' },
      { order: 3, name: 'Publicar evento', description: 'RabbitMQ', rollbackStep: 'N/A' },
      { order: 4, name: 'Confirmar resposta', description: 'Retorno 201', rollbackStep: 'N/A' },
    ],
  }],
  loadingFlows: false,
  flowsError: null as string | null,
  fetchFlows: vi.fn().mockResolvedValue(undefined),
}))

vi.mock('@/stores/flows', () => ({
  useFlowStore: () => flowStore,
}))

describe('FlowCockpitView', () => {
  beforeEach(() => {
    flowStore.fetchFlows.mockClear()
    flowStore.loadingFlows = false
    flowStore.flowsError = null
  })

  it('carrega fluxos ao montar', () => {
    mount(FlowCockpitView)

    expect(flowStore.fetchFlows).toHaveBeenCalledTimes(1)
  })

  it('exibe estado de erro quando store retorna erro', () => {
    flowStore.flowsError = 'Falha no endpoint de fluxos'

    const wrapper = mount(FlowCockpitView)

    expect(wrapper.text()).toContain('Falha no endpoint de fluxos')
  })
})
