import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseModal from '../components/base/BaseModal.vue'

describe('BaseModal', () => {
  it('nao renderiza conteudo quando open=false', () => {
    const wrapper = mount(BaseModal, {
      props: { open: false, title: 'Dialogo' },
      global: { stubs: { Teleport: true, Transition: false } },
    })

    expect(wrapper.text()).not.toContain('Dialogo')
  })

  it('renderiza titulo e descricao quando aberto', () => {
    const wrapper = mount(BaseModal, {
      props: { open: true, title: 'Dialogo', description: 'Descricao' },
      global: { stubs: { Teleport: true, Transition: false } },
    })

    expect(wrapper.text()).toContain('Dialogo')
    expect(wrapper.text()).toContain('Descricao')
  })

  it('emite close ao clicar no botao de fechar', async () => {
    const wrapper = mount(BaseModal, {
      props: { open: true, title: 'Dialogo' },
      global: { stubs: { Teleport: true, Transition: false } },
    })

    await wrapper.find('button[aria-label="Fechar diálogo"]').trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })
})
