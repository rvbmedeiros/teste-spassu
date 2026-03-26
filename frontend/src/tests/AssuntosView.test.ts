import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AssuntosView from '@/views/AssuntosView.vue'

const assuntoStore = vi.hoisted(() => ({
  assuntos: [{ codAs: 1, descricao: 'Descrição local' }],
  loading: false,
  error: null,
  fetchAll: vi.fn(),
  buscarPorId: vi.fn(),
  criar: vi.fn(),
  atualizar: vi.fn(),
  excluir: vi.fn(),
}))

const authStore = vi.hoisted(() => ({
  isAdmin: true,
}))

vi.mock('@/stores/assuntos', () => ({
  useAssuntoStore: () => assuntoStore,
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authStore,
}))

describe('AssuntosView', () => {
  beforeEach(() => {
    assuntoStore.fetchAll.mockReset()
    assuntoStore.buscarPorId.mockReset()
    assuntoStore.criar.mockReset()
    assuntoStore.atualizar.mockReset()
    assuntoStore.excluir.mockReset()
    assuntoStore.buscarPorId.mockResolvedValue({ codAs: 1, descricao: 'Arquitetura' })
  })

  it('abre edição carregando o assunto por ID e mostra título de edição', async () => {
    const wrapper = mount(AssuntosView, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    const editButton = wrapper.findAll('button').find(button => button.text().includes('Editar'))
    expect(editButton).toBeDefined()

    await editButton!.trigger('click')
    await flushPromises()

    const headings = wrapper.findAll('h2')

    expect(assuntoStore.buscarPorId).toHaveBeenCalledWith(1)
    expect(headings[headings.length - 1].text()).toBe('Editar')
    expect(wrapper.find('input').element.value).toBe('Arquitetura')
  })
})