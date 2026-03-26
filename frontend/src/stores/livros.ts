import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'
import { extractApiErrorMessage } from '@/services/errors'
import type { Autor } from './autores'
import type { Assunto } from './assuntos'

export interface LivroRequest {
  titulo: string
  editora: string
  edicao: number
  anoPublicacao: string
  valor: number
  autoresCodAu: number[]
  assuntosCodAs: number[]
}

export interface Livro {
  codL: number
  titulo: string
  editora: string
  edicao: number
  anoPublicacao: string
  valor: number
  autores: Autor[]
  assuntos: Assunto[]
}

export const useLivroStore = defineStore('livros', () => {
  const livros = ref<Livro[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchAll(page = 0, size = 20): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Livro[]>('/api/livros', { params: { page, size } })
      livros.value = data
    } catch (e: unknown) {
      error.value = extractApiErrorMessage(e)
    } finally {
      loading.value = false
    }
  }

  async function criar(request: LivroRequest): Promise<void> {
    await api.post('/api/livros', request)
    await fetchAll()
  }

  async function atualizar(codL: number, request: LivroRequest): Promise<void> {
    await api.put(`/api/livros/${codL}`, request)
    await fetchAll()
  }

  async function excluir(codL: number): Promise<void> {
    await api.delete(`/api/livros/${codL}`)
    livros.value = livros.value.filter(l => l.codL !== codL)
  }

  return { livros, loading, error, fetchAll, criar, atualizar, excluir }
})
