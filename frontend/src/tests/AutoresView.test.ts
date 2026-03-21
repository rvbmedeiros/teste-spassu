import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AutoresView from '@/views/AutoresView.vue'

const autorStore = vi.hoisted(() => ({
  autores: [{ codAu: 1, nome: 'Nome local' }],
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

vi.mock('@/stores/autores', () => ({
  useAutorStore: () => autorStore,
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authStore,
}))

describe('AutoresView', () => {
  beforeEach(() => {
    autorStore.fetchAll.mockReset()
    autorStore.buscarPorId.mockReset()
    autorStore.criar.mockReset()
    autorStore.atualizar.mockReset()
    autorStore.excluir.mockReset()
    autorStore.buscarPorId.mockResolvedValue({ codAu: 1, nome: 'Martin Fowler' })
  })

  it('abre edição carregando o autor por ID', async () => {
    const wrapper = mount(AutoresView, {
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

    expect(autorStore.buscarPorId).toHaveBeenCalledWith(1)
    expect(wrapper.find('input').element.value).toBe('Martin Fowler')
  })
})