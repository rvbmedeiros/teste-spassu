<script setup lang="ts">
import { computed, ref } from 'vue'
import { GitBranch, Play, Plus, Square, X } from 'lucide-vue-next'
import type { FlowEdge, FlowNode } from '@/stores/flows'
import type { BpmTrailNode as BpmTrailTreeNode } from './bpmTrailTypes'

defineOptions({
  name: 'BpmTrailNode',
})

const props = defineProps<{
  item: BpmTrailTreeNode
  typeLabel: (type: FlowNode['type']) => string
  selectedNodeId?: string | null
}>()

const emit = defineEmits<{
  (e: 'select-node', nodeId: string): void
}>()

const branchGridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${Math.max(props.item.branches.length, 1)}, minmax(16rem, 1fr))`,
}))

const branchLabel = (edge: FlowEdge) => edge.label || edge.edgeIntent || edge.to

const isSelected = computed(() => props.selectedNodeId === props.item.node.nodeId)
const hoveredBranchKey = ref<string | null>(null)

const branchKey = (edge: FlowEdge) => `${edge.from}-${edge.to}-${edge.label}-${edge.edgeIntent}`

const isBranchHighlighted = (edge: FlowEdge) => {
  const key = branchKey(edge)
  return hoveredBranchKey.value === null || hoveredBranchKey.value === key
}

const setHoveredBranch = (edge: FlowEdge | null) => {
  hoveredBranchKey.value = edge ? branchKey(edge) : null
}

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
    <button
      type="button"
      class="mx-auto block w-full max-w-3xl rounded-3xl border bg-(--ui-panel) p-5 text-left transition-colors"
      :class="isSelected ? 'border-(--ui-brand) ring-4 ring-(--ui-focus-ring)' : 'border-(--ui-border)'"
      :data-test="`trail-node-${item.node.nodeId}`"
      @click="emit('select-node', item.node.nodeId)"
    >
      <div class="flex flex-col items-center gap-3 text-center">
        <span
          v-if="item.isMerge"
          data-test="merge-badge"
          class="rounded-full border border-(--ui-brand) bg-(--ui-brand-soft) px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.14em] text-(--ui-brand)"
        >
          {{ $t('flowcockpit.mergePoint') }}
        </span>

        <div class="flex items-center justify-center" :class="shapeClass(item.node.type)">
          <Play v-if="item.node.type === 'START_EVENT'" class="h-5 w-5" />
          <Square v-else-if="item.node.type === 'END_EVENT'" class="h-5 w-5" />
          <X v-else-if="item.node.type === 'EXCLUSIVE_GATEWAY'" class="h-5 w-5 -rotate-45" />
          <Plus v-else-if="item.node.type === 'PARALLEL_GATEWAY'" class="h-5 w-5 -rotate-45" />
          <GitBranch v-else class="h-5 w-5 text-(--ui-brand)" />
        </div>

        <span class="rounded-full bg-(--ui-brand-soft) px-3 py-1 text-xs font-semibold text-(--ui-brand)">
          {{ typeLabel(item.node.type) }}
        </span>

        <div class="space-y-1">
          <p class="font-semibold text-(--ui-text)">{{ item.node.name }}</p>
          <p v-if="item.node.description" class="text-sm text-(--ui-text-muted)">{{ item.node.description }}</p>
          <p v-if="item.node.purpose" class="text-xs text-(--ui-text-muted)">{{ item.node.purpose }}</p>
        </div>
      </div>
    </button>

    <template v-if="item.branches.length">
      <div class="flex justify-center py-1">
        <div class="h-6 w-px bg-(--ui-border-strong)" />
      </div>

      <div
        class="relative overflow-x-auto px-2 pb-1 pt-8"
      >
        <div
          data-test="branch-connector-split"
          class="pointer-events-none absolute left-1/2 top-0 h-8 w-px -translate-x-1/2 bg-(--ui-border-strong)"
        />
        <div
          data-test="branch-connector-rail"
          class="pointer-events-none absolute left-8 right-8 top-8 h-px bg-(--ui-border-strong)"
        />

        <div
          data-test="branch-trails"
          class="grid gap-3 md:gap-4"
          :style="branchGridStyle"
        >
          <div
            v-for="branch in item.branches"
            :key="branchKey(branch.edge)"
            class="relative pt-6"
          >
            <div
              data-test="branch-connector-drop"
              class="pointer-events-none absolute left-1/2 top-0 h-6 w-px -translate-x-1/2 bg-(--ui-border-strong) transition-colors"
              :class="isBranchHighlighted(branch.edge) ? 'bg-(--ui-brand)' : 'bg-(--ui-border-strong)'"
            />

            <div
              data-test="branch-trail"
              :data-branch-test="`branch-trail-${branch.edge.to}`"
              class="min-w-[16rem] rounded-3xl border bg-(--ui-panel-muted) p-4 transition-[border-color,background-color,box-shadow,transform,opacity] duration-200"
              :class="isBranchHighlighted(branch.edge)
                ? 'border-(--ui-brand) bg-(--ui-brand-soft) shadow-(--ui-shadow-brand)'
                : hoveredBranchKey === null
                  ? 'border-(--ui-border)'
                  : 'border-(--ui-border) opacity-70'
              "
              @mouseenter="setHoveredBranch(branch.edge)"
              @mouseleave="setHoveredBranch(null)"
              @focusin="setHoveredBranch(branch.edge)"
              @focusout="setHoveredBranch(null)"
            >
              <div class="mb-4 flex justify-center">
                <span
                  data-test="branch-label"
                  class="rounded-full border px-3 py-1 text-xs font-semibold transition-colors"
                  :class="isBranchHighlighted(branch.edge)
                    ? 'border-(--ui-brand) bg-(--ui-panel) text-(--ui-brand)'
                    : 'border-(--ui-border-strong) bg-(--ui-surface-interactive-hover) text-(--ui-text-muted)'
                  "
                >
                  {{ branchLabel(branch.edge) }}
                </span>
              </div>

              <BpmTrailNode
                v-if="branch.trail"
                :item="branch.trail"
                :type-label="typeLabel"
                :selected-node-id="selectedNodeId"
                @select-node="emit('select-node', $event)"
              />
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-if="item.continuation">
      <div class="flex justify-center py-1">
        <div class="h-6 w-px bg-(--ui-border-strong)" />
      </div>

      <BpmTrailNode
        :item="item.continuation"
        :type-label="typeLabel"
        :selected-node-id="selectedNodeId"
        @select-node="emit('select-node', $event)"
      />
    </template>
  </div>
</template>