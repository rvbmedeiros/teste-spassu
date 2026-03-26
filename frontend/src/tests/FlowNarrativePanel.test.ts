import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import FlowNarrativePanel from '@/components/FlowNarrativePanel.vue'

describe('FlowNarrativePanel', () => {
  it('renderiza estado vazio quando nenhum no eh selecionado', () => {
    const wrapper = mount(FlowNarrativePanel, {
      props: {
        node: null,
        title: 'Detalhes',
        emptyLabel: 'Selecione um no',
        purposeLabel: 'Objetivo',
        inputLabel: 'Entrada',
        outputLabel: 'Saida',
        failureLabel: 'Falha',
      },
    })

    expect(wrapper.text()).toContain('Selecione um no')
  })

  it('renderiza fallback para campos opcionais vazios', () => {
    const wrapper = mount(FlowNarrativePanel, {
      props: {
        node: {
          nodeId: 'validar',
          type: 'ACTIVITY',
          order: 1,
          name: 'Validar',
          description: '',
          purpose: '',
          inputHint: '',
          outputHint: '',
          failureHint: '',
          rollbackStep: '',
        },
        title: 'Detalhes',
        emptyLabel: 'Selecione um no',
        purposeLabel: 'Objetivo',
        inputLabel: 'Entrada',
        outputLabel: 'Saida',
        failureLabel: 'Falha',
      },
    })

    expect(wrapper.text()).toContain('Validar')
    expect(wrapper.text()).toContain('Objetivo: -')
    expect(wrapper.text()).toContain('Entrada: -')
    expect(wrapper.text()).toContain('Saida: -')
    expect(wrapper.text()).toContain('Falha: -')
  })
})