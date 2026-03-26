import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import FlowCockpitView from '../views/FlowCockpitView.vue'

const flowStore = vi.hoisted(() => ({
  flows: [{
    id: 'create-livro',
    name: 'Criar Livro',
    description: 'Persiste livro',
    owner: 'orchestration-team',
    version: '1.0.0',
    domainTag: 'livros',
    businessGoal: 'Cadastrar livro',
    nodes: [
      {
        nodeId: 'start',
        type: 'START_EVENT',
        order: 0,
        name: 'Início',
        description: '',
        purpose: '',
        inputHint: '',
        outputHint: '',
        failureHint: '',
        rollbackStep: '',
      },
      {
        nodeId: 'validar',
        type: 'ACTIVITY',
        order: 1,
        name: 'Validar payload',
        description: 'Bean validation',
        purpose: 'Garantir consistência',
        inputHint: 'LivroRequest',
        outputHint: 'Payload válido',
        failureHint: '422',
        rollbackStep: 'N/A',
      },
    ],
    edges: [{ from: 'start', to: 'validar', label: '', edgeIntent: '' }],
  }],
  loadingFlows: false,
  flowsError: null as string | null,
  narrative: { flowId: 'create-livro', flowName: 'Criar Livro', businessGoal: 'Cadastrar', paths: ['Início -> Validar payload'] },
  loadingNarrative: false,
  narrativeError: null as string | null,
  fetchFlows: vi.fn().mockResolvedValue(undefined),
  fetchNarrative: vi.fn().mockResolvedValue(undefined),
}))

vi.mock('@/stores/flows', () => ({
  useFlowStore: () => flowStore,
}))

describe('FlowCockpitView', () => {
  beforeEach(() => {
    flowStore.fetchFlows.mockClear()
    flowStore.fetchNarrative.mockClear()
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

  it('abre drawer de detalhes ao selecionar um nó', async () => {
    const wrapper = mount(FlowCockpitView, { attachTo: document.body })

    await nextTick()

    await wrapper.find('button[aria-label="Selecionar workflow"]').trigger('click')
    await nextTick()

    const workflowOption = wrapper
      .findAll('button')
      .find(button => button.text().includes('Criar Livro') && button.attributes('aria-selected') === 'false')

    expect(workflowOption).toBeTruthy()
    await workflowOption!.trigger('click')
    await nextTick()

    const nodeButton = wrapper
      .findAll('button')
      .find(button => button.text().includes('Início'))

    expect(nodeButton).toBeTruthy()
    await nodeButton!.trigger('click')
    await nextTick()

    expect(document.body.textContent).toContain('Detalhes do nó')
    expect(document.body.textContent).toContain('Fechar detalhes')

    wrapper.unmount()
  })

  it('destaca narrativa compatível com o nó selecionado', async () => {
    const wrapper = mount(FlowCockpitView, { attachTo: document.body })

    await nextTick()

    await wrapper.find('button[aria-label="Selecionar workflow"]').trigger('click')
    await nextTick()

    const workflowOption = wrapper
      .findAll('button')
      .find(button => button.text().includes('Criar Livro') && button.attributes('aria-selected') === 'false')

    expect(workflowOption).toBeTruthy()
    await workflowOption!.trigger('click')
    await nextTick()

    await wrapper.find('button').trigger('click')
    await nextTick()

    const storyToggle = wrapper
      .findAll('button')
      .find(button => button.text().includes('Ver história do fluxo'))

    expect(storyToggle).toBeTruthy()
    await storyToggle!.trigger('click')
    await nextTick()

    const nodeButton = wrapper
      .findAll('button')
      .find(button => button.text().includes('Validar payload'))

    expect(nodeButton).toBeTruthy()
    await nodeButton!.trigger('click')
    await nextTick()

    expect(wrapper.get('[data-test="narrative-path-0"]').classes()).toContain('border-(--ui-brand)')

    wrapper.unmount()
  })
})
