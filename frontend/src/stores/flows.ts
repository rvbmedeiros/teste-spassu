import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'
import { extractApiErrorMessage } from '@/services/errors'

export interface FlowNode {
  order: number
  name: string
  description: string
  rollbackStep: string
}

export interface FlowGraph {
  id: string
  name: string
  description: string
  steps: FlowNode[]
}

export const useFlowStore = defineStore('flows', () => {
  const flows = ref<FlowGraph[]>([])
  const loadingFlows = ref(false)
  const flowsError = ref<string | null>(null)

  async function fetchFlows(): Promise<void> {
    loadingFlows.value = true
    flowsError.value = null
    try {
      flows.value = (await api.get<FlowGraph[]>('/flows')).data
    } catch (e: unknown) {
      flowsError.value = extractApiErrorMessage(e)
    } finally {
      loadingFlows.value = false
    }
  }

  return { flows, loadingFlows, flowsError, fetchFlows }
})
