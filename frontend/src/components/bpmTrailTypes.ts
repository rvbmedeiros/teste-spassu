import type { FlowEdge, FlowNode } from '@/stores/flows'

export interface BpmTrailBranch {
  edge: FlowEdge
  trail: BpmTrailNode | null
}

export interface BpmTrailNode {
  node: FlowNode
  isMerge: boolean
  branches: BpmTrailBranch[]
  continuation: BpmTrailNode | null
}