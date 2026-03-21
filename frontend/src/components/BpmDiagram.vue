<script setup lang="ts">
import { computed } from 'vue'
import { GitBranch, Play, Plus, Square, X } from 'lucide-vue-next'
import type { FlowEdge, FlowNode } from '@/stores/flows'

const props = withDefaults(defineProps<{
  nodes?: FlowNode[]
  edges?: FlowEdge[]
  typeLabel: (type: FlowNode['type']) => string
}>(), {
  nodes: () => [],
  edges: () => [],
})

const emit = defineEmits<{
  (e: 'select-node', nodeId: string): void
}>()

const nodeById = computed(() => new Map(props.nodes.map(node => [node.nodeId, node])))

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

const orderedNodes = computed(() => {
  const visited = new Set<string>()
  const result: FlowNode[] = []
  const start = props.nodes.find(node => node.type === 'START_EVENT')

  const walk = (nodeId: string): void => {
    if (visited.has(nodeId)) {
      return
    }

    const node = nodeById.value.get(nodeId)
    if (!node) {
      return
    }

    visited.add(nodeId)
    result.push(node)

    const outgoing = outgoingByNodeId.value.get(nodeId) ?? []
    for (const edge of outgoing) {
      walk(edge.to)
    }
  }

  if (start) {
    walk(start.nodeId)
  }

  for (const node of props.nodes) {
    if (!visited.has(node.nodeId)) {
      result.push(node)
    }
  }

  return result
})

const branchesFor = (nodeId: string) => outgoingByNodeId.value.get(nodeId) ?? []

const shapeClass = (type: FlowNode['type']) => {
  if (type === 'START_EVENT') {
    return 'h-14 w-14 rounded-full bg-(--ui-brand) text-(--ui-on-brand)'
  }
  if (type === 'END_EVENT') {
    return 'h-14 w-14 rounded-full border-4 border-(--ui-text) bg-transparent text-(--ui-text)'
  }
  if (type === 'EXCLUSIVE_GATEWAY' || type === 'PARALLEL_GATEWAY') {
    return 'h-12 w-12 rotate-45 rounded-sm border-2 border-(--ui-brand) bg-(--ui-brand-soft) text-(--ui-brand)'
  }
  return 'w-full rounded-2xl border border-(--ui-border) bg-(--ui-surface-interactive) p-4 text-left'
}
</script>

<template>
  <div class="space-y-4">
    <ol>
      <li v-for="(node, index) in orderedNodes" :key="node.nodeId">
        <button
          type="button"
          class="mx-auto block w-full max-w-3xl rounded-3xl border border-(--ui-border) bg-(--ui-panel) p-5 text-left"
          @click="emit('select-node', node.nodeId)"
        >
          <div class="flex flex-col items-center gap-3 text-center">
            <div class="flex items-center justify-center" :class="shapeClass(node.type)">
              <Play v-if="node.type === 'START_EVENT'" class="h-5 w-5" />
              <Square v-else-if="node.type === 'END_EVENT'" class="h-5 w-5" />
              <X v-else-if="node.type === 'EXCLUSIVE_GATEWAY'" class="h-5 w-5 -rotate-45" />
              <Plus v-else-if="node.type === 'PARALLEL_GATEWAY'" class="h-5 w-5 -rotate-45" />
              <GitBranch v-else class="h-5 w-5 text-(--ui-brand)" />
            </div>

            <span class="rounded-full bg-(--ui-brand-soft) px-3 py-1 text-xs font-semibold text-(--ui-brand)">
              {{ typeLabel(node.type) }}
            </span>

            <div class="space-y-1">
              <p class="font-semibold text-(--ui-text)">{{ node.name }}</p>
              <p v-if="node.description" class="text-sm text-(--ui-text-muted)">{{ node.description }}</p>
              <p v-if="node.purpose" class="text-xs text-(--ui-text-muted)">{{ node.purpose }}</p>
            </div>

            <div v-if="branchesFor(node.nodeId).length" class="flex flex-wrap justify-center gap-2">
              <span
                v-for="edge in branchesFor(node.nodeId)"
                :key="`${edge.from}-${edge.to}-${edge.label}`"
                class="rounded-full border border-(--ui-border-strong) bg-(--ui-surface-interactive-hover) px-2.5 py-1 text-xs text-(--ui-text-muted)"
              >
                {{ edge.label || edge.to }}
              </span>
            </div>
          </div>
        </button>

        <div v-if="index !== orderedNodes.length - 1" class="flex justify-center py-2">
          <div class="h-6 w-px bg-(--ui-border-strong)" />
        </div>
      </li>
    </ol>
  </div>
</template>
