import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

import api from '../services/api'
import { useLivroStore } from '../stores/livros'

describe('useLivroStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchAll popula a lista de livros', async () => {
    const livros = [{ codL: 1, titulo: 'DDD', editora: 'AW', edicao: 1, anoPublicacao: '2003', valor: 99.9, autores: [], assuntos: [] }]
    vi.mocked(api.get).mockResolvedValue({ data: livros })

    const store = useLivroStore()
    await store.fetchAll(0, 20)

    expect(api.get).toHaveBeenCalledWith('/api/livros', { params: { page: 0, size: 20 } })
    expect(store.livros).toEqual(livros)
    expect(store.error).toBeNull()
  })

  it('criar envia payload e recarrega a lista', async () => {
    vi.mocked(api.post).mockResolvedValue({})
    vi.mocked(api.get).mockResolvedValue({ data: [{ codL: 1, titulo: 'Novo', editora: 'AW', edicao: 1, anoPublicacao: '2003', valor: 10, autores: [], assuntos: [] }] })
    const request = { titulo: 'Novo', editora: 'AW', edicao: 1, anoPublicacao: '2003', valor: 10, autoresCodAu: [1], assuntosCodAs: [1] }

    const store = useLivroStore()
    await store.criar(request)

    expect(api.post).toHaveBeenCalledWith('/api/livros', request)
    expect(store.livros).toHaveLength(1)
  })

  it('atualizar envia payload e recarrega a lista', async () => {
    vi.mocked(api.put).mockResolvedValue({})
    vi.mocked(api.get).mockResolvedValue({ data: [{ codL: 1, titulo: 'Atualizado', editora: 'AW', edicao: 2, anoPublicacao: '2004', valor: 20, autores: [], assuntos: [] }] })
    const request = { titulo: 'Atualizado', editora: 'AW', edicao: 2, anoPublicacao: '2004', valor: 20, autoresCodAu: [1], assuntosCodAs: [1] }

    const store = useLivroStore()
    await store.atualizar(1, request)

    expect(api.put).toHaveBeenCalledWith('/api/livros/1', request)
    expect(store.livros[0].titulo).toBe('Atualizado')
  })

  it('excluir remove o livro localmente', async () => {
    vi.mocked(api.delete).mockResolvedValue({})
    const store = useLivroStore()
    store.livros = [
      { codL: 1, titulo: 'A', editora: 'AW', edicao: 1, anoPublicacao: '2003', valor: 10, autores: [], assuntos: [] },
      { codL: 2, titulo: 'B', editora: 'AW', edicao: 1, anoPublicacao: '2004', valor: 20, autores: [], assuntos: [] },
    ]

    await store.excluir(1)

    expect(api.delete).toHaveBeenCalledWith('/api/livros/1')
    expect(store.livros).toHaveLength(1)
    expect(store.livros[0].codL).toBe(2)
  })
})