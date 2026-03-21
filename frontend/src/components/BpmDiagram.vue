<script setup lang="ts">
import { computed } from 'vue'
import type { FlowEdge, FlowNode } from '@/stores/flows'
import BpmTrailNode from '@/components/BpmTrailNode.vue'
import type { BpmTrailNode as BpmTrailTreeNode } from '@/components/bpmTrailTypes'

const props = withDefaults(defineProps<{
  nodes?: FlowNode[]
  edges?: FlowEdge[]
  typeLabel: (type: FlowNode['type']) => string
  selectedNodeId?: string | null
}>(), {
  nodes: () => [],
  edges: () => [],
  selectedNodeId: null,
})

const emit = defineEmits<{
  (e: 'select-node', nodeId: string): void
}>()

const nodeById = computed(() => new Map(props.nodes.map(node => [node.nodeId, node])))

const incomingCountByNodeId = computed(() => {
  const index = new Map<string, number>()
  for (const edge of props.edges) {
    index.set(edge.to, (index.get(edge.to) ?? 0) + 1)
  }
  return index
})

const outgoingByNodeId = computed(() => {
  const index = new Map<string, FlowEdge[]>()
  for (const edge of props.edges) {
    if (!index.has(edge.from)) {
      index.set(edge.from, [])
    }
    index.get(edge.from)?.push(edge)
  }
  return index
})

const orderFor = (nodeId: string) => nodeById.value.get(nodeId)?.order ?? Number.MAX_SAFE_INTEGER

const sortEdges = (edges: FlowEdge[]) => edges.slice().sort((left, right) => {
  const orderDiff = orderFor(left.to) - orderFor(right.to)
  if (orderDiff !== 0) {
    return orderDiff
  }
  return (left.label || left.edgeIntent || left.to).localeCompare(right.label || right.edgeIntent || right.to)
})

const sharedMergeNodeId = (mergeNodeIds: Array<string | null>) => {
  if (!mergeNodeIds.length) {
    return null
  }

  const [first] = mergeNodeIds
  if (!first) {
    return null
  }

  return mergeNodeIds.every(mergeNodeId => mergeNodeId === first) ? first : null
}

const buildTrail = (nodeId: string, visited: Set<string>, stopAtMerge: boolean): { item: BpmTrailTreeNode | null, mergeNodeId: string | null } => {
  if (visited.has(nodeId)) {
    return { item: null, mergeNodeId: null }
  }

  if (stopAtMerge && (incomingCountByNodeId.value.get(nodeId) ?? 0) > 1) {
    return { item: null, mergeNodeId: nodeId }
  }

  const node = nodeById.value.get(nodeId)
  if (!node) {
    return { item: null, mergeNodeId: null }
  }

  const nextVisited = new Set(visited)
  nextVisited.add(nodeId)

  const outgoing = sortEdges(outgoingByNodeId.value.get(nodeId) ?? [])
  if (!outgoing.length) {
    return {
      item: {
        node,
        isMerge: (incomingCountByNodeId.value.get(node.nodeId) ?? 0) > 1,
        branches: [],
        continuation: null,
      },
      mergeNodeId: null,
    }
  }

  const isBranchingNode = outgoing.length > 1 || node.type === 'EXCLUSIVE_GATEWAY' || node.type === 'PARALLEL_GATEWAY'
  if (!isBranchingNode) {
    const next = buildTrail(outgoing[0].to, nextVisited, stopAtMerge)
    return {
      item: {
        node,
        isMerge: (incomingCountByNodeId.value.get(node.nodeId) ?? 0) > 1,
        branches: [],
        continuation: next.item,
      },
      mergeNodeId: next.mergeNodeId,
    }
  }

  const branches = outgoing.map(edge => {
    const branch = buildTrail(edge.to, new Set(nextVisited), true)
    return {
      edge,
      trail: branch.item,
      mergeNodeId: branch.mergeNodeId,
    }
  })

  const mergeNodeId = sharedMergeNodeId(branches.map(branch => branch.mergeNodeId))
  const continuation = mergeNodeId
    ? buildTrail(mergeNodeId, nextVisited, stopAtMerge)
    : { item: null, mergeNodeId: null }

  return {
    item: {
      node,
      isMerge: (incomingCountByNodeId.value.get(node.nodeId) ?? 0) > 1,
      branches: branches.map(branch => ({ edge: branch.edge, trail: branch.trail })),
      continuation: continuation.item,
    },
    mergeNodeId: continuation.mergeNodeId,
  }
}

const collectRenderedNodeIds = (item: BpmTrailTreeNode | null, renderedNodeIds: Set<string>) => {
  if (!item || renderedNodeIds.has(item.node.nodeId)) {
    return
  }

  renderedNodeIds.add(item.node.nodeId)
  for (const branch of item.branches) {
    collectRenderedNodeIds(branch.trail, renderedNodeIds)
  }
  collectRenderedNodeIds(item.continuation, renderedNodeIds)
}

const trailRoots = computed(() => {
  const sortedNodes = props.nodes.slice().sort((left, right) => left.order - right.order)
  const explicitStart = sortedNodes.find(node => node.type === 'START_EVENT')
  const rootNodeIds = explicitStart
    ? [explicitStart.nodeId]
    : sortedNodes
        .filter(node => (incomingCountByNodeId.value.get(node.nodeId) ?? 0) === 0)
        .map(node => node.nodeId)

  const resolvedRootNodeIds = rootNodeIds.length ? rootNodeIds : sortedNodes.slice(0, 1).map(node => node.nodeId)
  const renderedNodeIds = new Set<string>()
  const roots: BpmTrailTreeNode[] = []

  for (const rootNodeId of resolvedRootNodeIds) {
    const trail = buildTrail(rootNodeId, new Set(), false).item
    if (!trail) {
      continue
    }
    roots.push(trail)
    collectRenderedNodeIds(trail, renderedNodeIds)
  }

  for (const node of sortedNodes) {
    if (renderedNodeIds.has(node.nodeId)) {
      continue
    }

    const trail = buildTrail(node.nodeId, new Set(), false).item
    if (!trail) {
      continue
    }
    roots.push(trail)
    collectRenderedNodeIds(trail, renderedNodeIds)
  }

  return roots
})
</script>

<template>
  <div class="space-y-6">
    <section
      v-for="trail in trailRoots"
      :key="trail.node.nodeId"
      class="rounded-4xl border border-(--ui-border) bg-(--ui-panel-soft) p-4 sm:p-6"
    >
      <BpmTrailNode
        :item="trail"
        :type-label="typeLabel"
        :selected-node-id="selectedNodeId"
        @select-node="emit('select-node', $event)"
      />
    </section>
  </div>
</template>
