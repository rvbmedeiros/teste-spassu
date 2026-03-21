import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'
import { extractApiErrorMessage } from '@/services/errors'

export interface FlowNode {
  nodeId: string
  type: NodeType
  order: number
  name: string
  description: string
  purpose: string
  inputHint: string
  outputHint: string
  failureHint: string
  rollbackStep: string
}

export interface FlowEdge {
  from: string
  to: string
  label: string
  edgeIntent: string
}

export type NodeType =
  | 'START_EVENT'
  | 'END_EVENT'
  | 'ACTIVITY'
  | 'EXCLUSIVE_GATEWAY'
  | 'PARALLEL_GATEWAY'

export interface FlowGraph {
  id: string
  name: string
  description: string
  owner: string
  version: string
  domainTag: string
  businessGoal: string
  nodes: FlowNode[]
  edges: FlowEdge[]
}

interface LegacyFlowStep {
  order: number
  name: string
  description: string
  purpose?: string
  inputHint?: string
  outputHint?: string
  failureHint?: string
  rollbackStep?: string
}

interface RawFlowGraph {
  id: string
  name: string
  description?: string
  owner?: string
  version?: string
  domainTag?: string
  businessGoal?: string
  nodes?: FlowNode[]
  edges?: FlowEdge[]
  steps?: LegacyFlowStep[]
}

const toGraph = (flow: RawFlowGraph): FlowGraph => {
  const normalizedNodes = flow.nodes ?? []
  const normalizedEdges = flow.edges ?? []

  if (normalizedNodes.length || normalizedEdges.length) {
    return {
      id: flow.id,
      name: flow.name,
      description: flow.description ?? '',
      owner: flow.owner ?? '',
      version: flow.version ?? '',
      domainTag: flow.domainTag ?? '',
      businessGoal: flow.businessGoal ?? '',
      nodes: normalizedNodes,
      edges: normalizedEdges,
    }
  }

  const legacySteps = (flow.steps ?? [])
    .slice()
    .sort((a, b) => a.order - b.order)

  const legacyNodes: FlowNode[] = legacySteps.map(step => ({
    nodeId: `step-${step.order}`,
    type: 'ACTIVITY',
    order: step.order,
    name: step.name,
    description: step.description ?? '',
    purpose: step.purpose ?? '',
    inputHint: step.inputHint ?? '',
    outputHint: step.outputHint ?? '',
    failureHint: step.failureHint ?? '',
    rollbackStep: step.rollbackStep ?? '',
  }))

  const legacyEdges: FlowEdge[] = legacyNodes.slice(0, -1).map((node, index) => ({
    from: node.nodeId,
    to: legacyNodes[index + 1].nodeId,
    label: '',
    edgeIntent: '',
  }))

  return {
    id: flow.id,
    name: flow.name,
    description: flow.description ?? '',
    owner: flow.owner ?? '',
    version: flow.version ?? '',
    domainTag: flow.domainTag ?? '',
    businessGoal: flow.businessGoal ?? '',
    nodes: legacyNodes,
    edges: legacyEdges,
  }
}

export interface FlowNarrative {
  flowId: string
  flowName: string
  businessGoal: string
  paths: string[]
}

export const useFlowStore = defineStore('flows', () => {
  const flows = ref<FlowGraph[]>([])
  const loadingFlows = ref(false)
  const flowsError = ref<string | null>(null)
  const narrative = ref<FlowNarrative | null>(null)
  const loadingNarrative = ref(false)
  const narrativeError = ref<string | null>(null)

  async function fetchFlows(): Promise<void> {
    loadingFlows.value = true
    flowsError.value = null
    try {
      const response = await api.get<RawFlowGraph[]>('/flows')
      flows.value = response.data.map(toGraph)
    } catch (e: unknown) {
      flowsError.value = extractApiErrorMessage(e)
    } finally {
      loadingFlows.value = false
    }
  }

  async function fetchNarrative(flowId: string): Promise<void> {
    loadingNarrative.value = true
    narrativeError.value = null
    try {
      narrative.value = (await api.get<FlowNarrative>(`/flows/${flowId}/narrative`)).data
    } catch (e: unknown) {
      narrativeError.value = extractApiErrorMessage(e)
    } finally {
      loadingNarrative.value = false
    }
  }

  return {
    flows,
    loadingFlows,
    flowsError,
    narrative,
    loadingNarrative,
    narrativeError,
    fetchFlows,
    fetchNarrative,
  }
})
