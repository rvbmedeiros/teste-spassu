import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAutorStore } from '@/stores/autores'

// Mock the api module
vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

import api from '@/services/api'

describe('useAutorStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchAll popula a lista de autores', async () => {
    const mockAutores = [
      { codAu: 1, nome: 'Autor A' },
      { codAu: 2, nome: 'Autor B' },
    ]
    vi.mocked(api.get).mockResolvedValue({ data: mockAutores })

    const store = useAutorStore()
    await store.fetchAll()

    expect(store.autores).toEqual(mockAutores)
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('buscarPorId retorna o autor solicitado', async () => {
    const mockAutor = { codAu: 1, nome: 'Autor A' }
    vi.mocked(api.get).mockResolvedValue({ data: mockAutor })

    const store = useAutorStore()
    const result = await store.buscarPorId(1)

    expect(result).toEqual(mockAutor)
    expect(store.error).toBeNull()
  })

  it('criar envia o payload e recarrega a lista', async () => {
    vi.mocked(api.post).mockResolvedValue({})
    vi.mocked(api.get).mockResolvedValue({ data: [{ codAu: 1, nome: 'Novo Autor' }] })

    const store = useAutorStore()
    await store.criar('Novo Autor')

    expect(api.post).toHaveBeenCalledWith('/api/autores', { nome: 'Novo Autor' })
    expect(store.autores).toEqual([{ codAu: 1, nome: 'Novo Autor' }])
  })

  it('atualizar envia o payload e recarrega a lista', async () => {
    vi.mocked(api.put).mockResolvedValue({})
    vi.mocked(api.get).mockResolvedValue({ data: [{ codAu: 1, nome: 'Kent Beck' }] })

    const store = useAutorStore()
    await store.atualizar(1, 'Kent Beck')

    expect(api.put).toHaveBeenCalledWith('/api/autores/1', { nome: 'Kent Beck' })
    expect(store.autores).toEqual([{ codAu: 1, nome: 'Kent Beck' }])
  })

  it('excluir remove o autor da lista local', async () => {
    const store = useAutorStore()
    store.autores = [
      { codAu: 1, nome: 'A' },
      { codAu: 2, nome: 'B' },
    ]
    vi.mocked(api.delete).mockResolvedValue({})

    await store.excluir(1)

    expect(store.autores).toHaveLength(1)
    expect(store.autores[0].codAu).toBe(2)
  })

  it('fetchAll define error quando a API falha', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('Network error'))

    const store = useAutorStore()
    await store.fetchAll()

    expect(store.autores).toHaveLength(0)
    expect(store.error).toBe('Network error')
  })
})
