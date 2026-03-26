import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAssuntoStore } from '@/stores/assuntos'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

import api from '@/services/api'

describe('useAssuntoStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchAll popula a lista de assuntos', async () => {
    const mockAssuntos = [
      { codAs: 1, descricao: 'Arquitetura' },
      { codAs: 2, descricao: 'DDD' },
    ]
    vi.mocked(api.get).mockResolvedValue({ data: mockAssuntos })

    const store = useAssuntoStore()
    await store.fetchAll()

    expect(store.assuntos).toEqual(mockAssuntos)
    expect(store.error).toBeNull()
  })

  it('buscarPorId retorna o assunto solicitado', async () => {
    const mockAssunto = { codAs: 1, descricao: 'Arquitetura' }
    vi.mocked(api.get).mockResolvedValue({ data: mockAssunto })

    const store = useAssuntoStore()
    const result = await store.buscarPorId(1)

    expect(result).toEqual(mockAssunto)
  })

  it('atualizar envia o payload e recarrega a lista', async () => {
    vi.mocked(api.put).mockResolvedValue({})
    vi.mocked(api.get).mockResolvedValue({ data: [{ codAs: 1, descricao: 'DDD' }] })

    const store = useAssuntoStore()
    await store.atualizar(1, 'DDD')

    expect(api.put).toHaveBeenCalledWith('/api/assuntos/1', { descricao: 'DDD' })
    expect(store.assuntos).toEqual([{ codAs: 1, descricao: 'DDD' }])
  })

  it('excluir remove o assunto da lista local', async () => {
    const store = useAssuntoStore()
    store.assuntos = [
      { codAs: 1, descricao: 'Arquitetura' },
      { codAs: 2, descricao: 'DDD' },
    ]
    vi.mocked(api.delete).mockResolvedValue({})

    await store.excluir(1)

    expect(store.assuntos).toEqual([{ codAs: 2, descricao: 'DDD' }])
  })
})