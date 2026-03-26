import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'
import { extractApiErrorMessage } from '@/services/errors'

export interface Autor {
  codAu: number
  nome: string
}

export const useAutorStore = defineStore('autores', () => {
  const autores = ref<Autor[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function buscarPorId(codAu: number): Promise<Autor> {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Autor>(`/api/autores/${codAu}`)
      return data
    } catch (e: unknown) {
      error.value = extractApiErrorMessage(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchAll(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Autor[]>('/api/autores')
      autores.value = data
    } catch (e: unknown) {
      error.value = extractApiErrorMessage(e)
    } finally {
      loading.value = false
    }
  }

  async function criar(nome: string): Promise<void> {
    await api.post('/api/autores', { nome })
    await fetchAll()
  }

  async function atualizar(codAu: number, nome: string): Promise<void> {
    await api.put(`/api/autores/${codAu}`, { nome })
    await fetchAll()
  }

  async function excluir(codAu: number): Promise<void> {
    await api.delete(`/api/autores/${codAu}`)
    autores.value = autores.value.filter(a => a.codAu !== codAu)
  }

  return { autores, loading, error, buscarPorId, fetchAll, criar, atualizar, excluir }
})
