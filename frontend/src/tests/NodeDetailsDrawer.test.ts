import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import NodeDetailsDrawer from '@/components/NodeDetailsDrawer.vue'

describe('NodeDetailsDrawer', () => {
  it('foca o painel ao abrir e emite close com escape', async () => {
    const wrapper = mount(NodeDetailsDrawer, {
      attachTo: document.body,
      props: {
        open: false,
        node: null,
        title: 'Detalhes',
        emptyLabel: 'Nenhum no',
        purposeLabel: 'Objetivo',
        inputLabel: 'Entrada',
        outputLabel: 'Saida',
        failureLabel: 'Falha',
        closeLabel: 'Fechar',
        closeAriaLabel: 'Fechar painel',
      },
      global: {
        stubs: {
          Teleport: true,
          Transition: true,
          BaseButton: {
            emits: ['click'],
            template: '<button @click="$emit(\'click\')"><slot /></button>',
          },
        },
      },
    })

    await wrapper.setProps({
      open: true,
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
    })
    await flushPromises()

    const dialog = document.body.querySelector('[role="dialog"]') as HTMLElement | null
    expect(dialog).not.toBeNull()
    expect(document.activeElement).toBe(dialog)

    await wrapper.get('[role="dialog"]').trigger('keydown', { key: 'Escape' })

    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('fecha ao clicar no overlay e renderiza fallback do no', async () => {
    const wrapper = mount(NodeDetailsDrawer, {
      props: {
        open: true,
        node: null,
        title: 'Detalhes',
        emptyLabel: 'Nenhum no',
        purposeLabel: 'Objetivo',
        inputLabel: 'Entrada',
        outputLabel: 'Saida',
        failureLabel: 'Falha',
        closeLabel: 'Fechar',
        closeAriaLabel: 'Fechar painel',
      },
      global: {
        stubs: {
          Teleport: true,
          Transition: true,
          BaseButton: {
            emits: ['click'],
            template: '<button @click="$emit(\'click\')"><slot /></button>',
          },
        },
      },
    })

    expect(wrapper.text()).toContain('Nenhum no')

    await wrapper.find('.fixed.inset-0').trigger('click')

    expect(wrapper.emitted('close')).toHaveLength(1)
  })
})