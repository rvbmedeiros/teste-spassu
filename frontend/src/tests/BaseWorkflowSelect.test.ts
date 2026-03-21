import { afterEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import WorkflowSelect from '../components/WorkflowSelect.vue'

describe('WorkflowSelect', () => {
  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('renderiza placeholder quando nao ha item selecionado', () => {
    const wrapper = mount(WorkflowSelect, {
      attachTo: document.body,
      props: {
        modelValue: null,
        options: [],
        label: 'Fluxo',
        placeholder: 'Selecione',
        clearLabel: 'Limpar',
        emptyLabel: 'Sem fluxos',
      },
      global: { stubs: { Transition: false } },
    })

    expect(wrapper.text()).toContain('Selecione')
  })

  it('emite update:modelValue ao selecionar opcao', async () => {
    const wrapper = mount(WorkflowSelect, {
      attachTo: document.body,
      props: {
        modelValue: null,
        options: [{ id: 'f1', label: 'Criar Livro', description: 'Fluxo de criação' }],
        label: 'Fluxo',
        placeholder: 'Selecione',
        clearLabel: 'Limpar',
        emptyLabel: 'Sem fluxos',
      },
      global: { stubs: { Transition: false } },
    })

    await wrapper.find('button[aria-label="Fluxo"]').trigger('click')
    await wrapper.findAll('ul li button')[0].trigger('click')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['f1'])
  })

  it('fecha dropdown ao clicar fora do componente', async () => {
    const wrapper = mount(WorkflowSelect, {
      attachTo: document.body,
      props: {
        modelValue: null,
        options: [{ id: 'f1', label: 'Criar Livro' }],
        label: 'Fluxo',
        placeholder: 'Selecione',
        clearLabel: 'Limpar',
        emptyLabel: 'Sem fluxos',
      },
      global: { stubs: { Transition: true } },
    })

    await wrapper.find('button[aria-label="Fluxo"]').trigger('click')
    expect(wrapper.find('[role="listbox"]').exists()).toBe(true)

    const outside = document.createElement('div')
    document.body.appendChild(outside)
    outside.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[role="listbox"]').exists()).toBe(false)
  })
})
