import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'
import { extractApiErrorMessage } from '@/services/errors'

export interface Assunto {
  codAs: number
  descricao: string
}

export const useAssuntoStore = defineStore('assuntos', () => {
  const assuntos = ref<Assunto[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function buscarPorId(codAs: number): Promise<Assunto> {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Assunto>(`/api/assuntos/${codAs}`)
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
      const { data } = await api.get<Assunto[]>('/api/assuntos')
      assuntos.value = data
    } catch (e: unknown) {
      error.value = extractApiErrorMessage(e)
    } finally {
      loading.value = false
    }
  }

  async function criar(descricao: string): Promise<void> {
    await api.post('/api/assuntos', { descricao })
    await fetchAll()
  }

  async function atualizar(codAs: number, descricao: string): Promise<void> {
    await api.put(`/api/assuntos/${codAs}`, { descricao })
    await fetchAll()
  }

  async function excluir(codAs: number): Promise<void> {
    await api.delete(`/api/assuntos/${codAs}`)
    assuntos.value = assuntos.value.filter(a => a.codAs !== codAs)
  }

  return { assuntos, loading, error, buscarPorId, fetchAll, criar, atualizar, excluir }
})
