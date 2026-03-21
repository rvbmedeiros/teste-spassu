import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import LivrosView from '../views/LivrosView.vue'

const livroStore = vi.hoisted(() => ({
  livros: [
    {
      codL: 10,
      titulo: 'Livro legado',
      editora: 'Editora X',
      edicao: 1,
      anoPublicacao: '2024',
      valor: 10,
      autores: [{ codAu: 1, nome: 'Autor local' }],
      assuntos: [{ codAs: 1, descricao: 'Assunto local' }],
    },
  ],
  loading: false,
  error: null,
  fetchAll: vi.fn(),
  criar: vi.fn(),
  atualizar: vi.fn(),
  excluir: vi.fn(),
}))

const autorStore = vi.hoisted(() => ({
  autores: [
    { codAu: 1, nome: 'Autor local' },
    { codAu: 2, nome: 'Outro autor' },
  ],
  loading: false,
  error: null,
  fetchAll: vi.fn(),
}))

const assuntoStore = vi.hoisted(() => ({
  assuntos: [
    { codAs: 7, descricao: 'Romance' },
    { codAs: 8, descricao: 'Engenharia' },
  ],
  loading: false,
  error: null,
  fetchAll: vi.fn(),
}))

const authStore = vi.hoisted(() => ({
  isAdmin: true,
}))

vi.mock('@/stores/livros', () => ({
  useLivroStore: () => livroStore,
}))

vi.mock('@/stores/autores', () => ({
  useAutorStore: () => autorStore,
}))

vi.mock('@/stores/assuntos', () => ({
  useAssuntoStore: () => assuntoStore,
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authStore,
}))

describe('LivrosView', () => {
  beforeEach(() => {
    livroStore.fetchAll.mockReset()
    livroStore.criar.mockReset()
    livroStore.atualizar.mockReset()
    livroStore.excluir.mockReset()

    autorStore.fetchAll.mockReset()
    assuntoStore.fetchAll.mockReset()

    livroStore.criar.mockResolvedValue(undefined)
    livroStore.atualizar.mockResolvedValue(undefined)
  })

  it('exibe resumo compacto apos selecionar autores no drawer', async () => {
    const wrapper = mount(LivrosView, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    const novoButton = wrapper.findAll('button').find(button => button.text().includes('Novo Livro'))
    expect(novoButton).toBeDefined()

    await novoButton!.trigger('click')
    await flushPromises()

    const abrirAutores = wrapper.findAll('button').find(button => button.text().includes('Selecionar autores'))
    expect(abrirAutores).toBeDefined()

    await abrirAutores!.trigger('click')
    await flushPromises()

    const opcaoAutor = wrapper.findAll('button').find(button => button.text().includes('Outro autor'))
    expect(opcaoAutor).toBeDefined()

    await opcaoAutor!.trigger('click')
    await flushPromises()

    const aplicar = wrapper.findAll('button').find(button => button.text().includes('Aplicar seleção'))
    expect(aplicar).toBeDefined()

    await aplicar!.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Selecionados: 1')
    expect(wrapper.text()).toContain('Outro autor')
  })

  it('serializa IDs selecionados em arrays ao salvar', async () => {
    const wrapper = mount(LivrosView, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    const novoButton = wrapper.findAll('button').find(button => button.text().includes('Novo Livro'))
    expect(novoButton).toBeDefined()

    await novoButton!.trigger('click')
    await flushPromises()

    const abrirAutores = wrapper.findAll('button').find(button => button.text().includes('Selecionar autores'))
    const abrirAssuntos = wrapper.findAll('button').find(button => button.text().includes('Selecionar assuntos'))

    expect(abrirAutores).toBeDefined()
    expect(abrirAssuntos).toBeDefined()

    await abrirAutores!.trigger('click')
    await flushPromises()

    const opcaoAutor = wrapper.findAll('button').find(button => button.text().includes('Autor local'))
    expect(opcaoAutor).toBeDefined()
    await opcaoAutor!.trigger('click')
    await flushPromises()

    const aplicarAutores = wrapper.findAll('button').find(button => button.text().includes('Aplicar seleção'))
    expect(aplicarAutores).toBeDefined()
    await aplicarAutores!.trigger('click')
    await flushPromises()

    await abrirAssuntos!.trigger('click')
    await flushPromises()

    const opcaoAssunto = wrapper.findAll('button').find(button => button.text().includes('Romance'))
    expect(opcaoAssunto).toBeDefined()
    await opcaoAssunto!.trigger('click')
    await flushPromises()

    const aplicarAssuntos = wrapper.findAll('button').find(button => button.text().includes('Aplicar seleção'))
    expect(aplicarAssuntos).toBeDefined()
    await aplicarAssuntos!.trigger('click')
    await flushPromises()

    const salvar = wrapper.findAll('button').find(button => button.text().includes('Salvar'))
    expect(salvar).toBeDefined()

    await salvar!.trigger('click')
    await flushPromises()

    expect(livroStore.criar).toHaveBeenCalledTimes(1)
    expect(livroStore.criar).toHaveBeenCalledWith(expect.objectContaining({
      autoresCodAu: [1],
      assuntosCodAs: [7],
    }))
  })
})
