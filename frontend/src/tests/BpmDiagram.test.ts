import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import BpmDiagram from '@/components/BpmDiagram.vue'

describe('BpmDiagram', () => {
  it('separa trails de um gateway exclusivo e continua apos o merge', async () => {
    const wrapper = mount(BpmDiagram, {
      props: {
        nodes: [
          { nodeId: 'fim', type: 'END_EVENT', order: 99, name: 'Fim', description: '', purpose: '', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
          { nodeId: 'start', type: 'START_EVENT', order: 0, name: 'Inicio', description: '', purpose: '', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
          { nodeId: 'gateway', type: 'EXCLUSIVE_GATEWAY', order: 2, name: 'Decisao', description: '', purpose: '', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
          { nodeId: 'validar', type: 'ACTIVITY', order: 1, name: 'Validar', description: 'Valida', purpose: 'Checar', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
          { nodeId: 'aprovar', type: 'ACTIVITY', order: 3, name: 'Aprovar', description: '', purpose: '', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
          { nodeId: 'corrigir', type: 'ACTIVITY', order: 4, name: 'Corrigir', description: '', purpose: '', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
        ],
        edges: [
          { from: 'start', to: 'validar', label: '', edgeIntent: '' },
          { from: 'validar', to: 'gateway', label: '', edgeIntent: '' },
          { from: 'gateway', to: 'aprovar', label: 'Aprovado', edgeIntent: 'success' },
          { from: 'gateway', to: 'corrigir', label: 'Rejeitado', edgeIntent: 'failure' },
          { from: 'aprovar', to: 'fim', label: '', edgeIntent: '' },
          { from: 'corrigir', to: 'fim', label: '', edgeIntent: '' },
        ],
        typeLabel: (type: string) => `tipo:${type}`,
        selectedNodeId: 'corrigir',
      },
      global: {
        mocks: {
          $t: (key: string) => key,
        },
      },
    })

    expect(wrapper.findAll('[data-test="branch-trail"]')).toHaveLength(2)
    expect(wrapper.findAll('[data-test="branch-label"]').map(node => node.text())).toEqual(['Aprovado', 'Rejeitado'])
    expect(wrapper.find('[data-test="branch-connector-split"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="branch-connector-rail"]').exists()).toBe(true)
    expect(wrapper.findAll('[data-test="branch-connector-drop"]')).toHaveLength(2)
    expect(wrapper.find('[data-test="trail-node-aprovar"]').text()).toContain('Aprovar')
    expect(wrapper.find('[data-test="trail-node-corrigir"]').text()).toContain('Corrigir')
    expect(wrapper.findAll('[data-test="trail-node-fim"]')).toHaveLength(1)
    expect(wrapper.find('[data-test="trail-node-corrigir"]').classes()).toContain('ring-4')
    expect(wrapper.find('[data-test="merge-badge"]').text()).toContain('flowcockpit.mergePoint')

    await wrapper.get('[data-branch-test="branch-trail-aprovar"]').trigger('mouseenter')

    expect(wrapper.get('[data-branch-test="branch-trail-aprovar"]').classes()).toContain('border-(--ui-brand)')
    expect(wrapper.get('[data-branch-test="branch-trail-corrigir"]').classes()).toContain('opacity-70')

    await wrapper.get('[data-branch-test="branch-trail-aprovar"]').trigger('mouseleave')

    await wrapper.get('[data-test="trail-node-validar"]').trigger('click')

    expect(wrapper.emitted('select-node')?.[0]).toEqual(['validar'])
  })

  it('separa trails tambem para gateway paralelo', () => {
    const wrapper = mount(BpmDiagram, {
      props: {
        nodes: [
          { nodeId: 'start', type: 'START_EVENT', order: 0, name: 'Inicio', description: '', purpose: '', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
          { nodeId: 'gateway', type: 'PARALLEL_GATEWAY', order: 1, name: 'Preparar', description: '', purpose: '', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
          { nodeId: 'autores', type: 'ACTIVITY', order: 2, name: 'Validar autores', description: '', purpose: '', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
          { nodeId: 'assuntos', type: 'ACTIVITY', order: 3, name: 'Validar assuntos', description: '', purpose: '', inputHint: '', outputHint: '', failureHint: '', rollbackStep: '' },
        ],
        edges: [
          { from: 'start', to: 'gateway', label: '', edgeIntent: '' },
          { from: 'gateway', to: 'autores', label: 'Autores', edgeIntent: 'authors' },
          { from: 'gateway', to: 'assuntos', label: 'Assuntos', edgeIntent: 'subjects' },
        ],
        typeLabel: (type: string) => `tipo:${type}`,
      },
      global: {
        mocks: {
          $t: (key: string) => key,
        },
      },
    })

    expect(wrapper.findAll('[data-test="branch-trail"]')).toHaveLength(2)
    expect(wrapper.text()).toContain('Autores')
    expect(wrapper.text()).toContain('Assuntos')
    expect(wrapper.find('[data-test="trail-node-autores"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="trail-node-assuntos"]').exists()).toBe(true)
  })
})