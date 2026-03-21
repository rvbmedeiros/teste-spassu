<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  Activity,
  AlertTriangle,
  BookOpenText,
  RotateCcw,
  Workflow,
} from 'lucide-vue-next'
import { useFlowStore, type NodeType } from '@/stores/flows'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import WorkflowSelect from '@/components/WorkflowSelect.vue'
import BpmDiagram from '@/components/BpmDiagram.vue'
import NodeDetailsDrawer from '@/components/NodeDetailsDrawer.vue'

const { t } = useI18n()
const store = useFlowStore()

const selectedWorkflowId = ref<string | null>(null)
const selectedNodeId = ref<string | null>(null)
const nodeDetailsOpen = ref(false)
const showStory = ref(false)

onMounted(async () => {
  await store.fetchFlows()
})

watch(selectedWorkflowId, async flowId => {
  selectedNodeId.value = null
  nodeDetailsOpen.value = false
  showStory.value = false
  if (!flowId) {
    store.narrative = null
    return
  }

  const workflowExists = store.flows.some(flow => flow.id === flowId)
  if (!workflowExists) {
    store.narrative = null
    return
  }

  await store.fetchNarrative(flowId)
})

const activeFlows = computed(() => store.flows.length)
const workflowOptions = computed(() => store.flows.map(flow => ({
  id: flow.id,
  label: flow.name,
  description: flow.description,
})))
const selectedWorkflow = computed(() => store.flows.find(flow => flow.id === selectedWorkflowId.value) ?? null)
const selectedNode = computed(() => {
  if (!selectedWorkflow.value || !selectedNodeId.value) {
    return null
  }
  return selectedWorkflow.value.nodes.find(node => node.nodeId === selectedNodeId.value) ?? null
})
const typeLabel = (type: NodeType) => {
  if (type === 'START_EVENT') return t('flowcockpit.nodeType.start')
  if (type === 'END_EVENT') return t('flowcockpit.nodeType.end')
  if (type === 'EXCLUSIVE_GATEWAY') return t('flowcockpit.nodeType.exclusiveGateway')
  if (type === 'PARALLEL_GATEWAY') return t('flowcockpit.nodeType.parallelGateway')
  return t('flowcockpit.nodeType.activity')
}

const selectNode = (nodeId: string) => {
  selectedNodeId.value = nodeId
  nodeDetailsOpen.value = true
}
</script>

<template>
  <div class="page-shell">
    <section class="surface-panel-strong rounded-4xl p-6 sm:p-8">
      <div class="flex flex-col gap-6 xl:flex-row xl:items-end xl:justify-between">
        <div class="space-y-4">
          <span class="section-eyebrow">
            <Workflow class="h-3.5 w-3.5" />
            {{ t('nav.flowcockpit') }}
          </span>
          <div class="space-y-2">
            <h1 class="page-title">{{ t('flowcockpit.title') }}</h1>
            <p class="page-subtitle">{{ t('flowcockpit.subtitle') }}</p>
          </div>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <WorkflowSelect
            v-model="selectedWorkflowId"
            :options="workflowOptions"
            :label="t('flowcockpit.workflowSelectorLabel')"
            :placeholder="t('flowcockpit.workflowSelectorPlaceholder')"
            :clear-label="t('flowcockpit.workflowSelectorClear')"
            :empty-label="t('flowcockpit.noWorkflowOptions')"
          />
        </div>
      </div>

      <div class="mt-8 grid gap-4 md:grid-cols-1">
        <div class="metric-tile">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-(--ui-text-muted)">{{ t('flowcockpit.activeFlows') }}</span>
            <Activity class="h-4 w-4 text-(--ui-brand)" />
          </div>
          <p class="mt-4 text-3xl font-semibold tracking-tight text-(--ui-text)">{{ activeFlows }}</p>
        </div>
      </div>
    </section>

    <section>
      <!-- Loading state -->
      <div v-if="store.loadingFlows" class="flex min-h-72 flex-col items-center justify-center gap-3 text-(--ui-text-muted)">
        <Activity class="h-8 w-8 animate-pulse text-(--ui-brand)" />
        <p class="text-sm font-medium">{{ t('flowcockpit.loadingFlows') }}</p>
      </div>

      <!-- Error state -->
      <div v-else-if="store.flowsError" class="flex min-h-72 flex-col items-center justify-center gap-4 text-center">
        <span class="rounded-3xl bg-(--ui-danger-soft) p-4 text-(--ui-danger)">
          <AlertTriangle class="h-6 w-6" />
        </span>
        <div class="space-y-1">
          <p class="text-sm font-semibold text-(--ui-text)">{{ t('flowcockpit.errorFlows') }}</p>
          <p class="text-xs text-(--ui-text-muted)">{{ store.flowsError }}</p>
        </div>
        <BaseButton variant="secondary" size="sm" @click="store.fetchFlows">
          <template #leading><RotateCcw /></template>
          {{ t('common.retry') }}
        </BaseButton>
      </div>

      <!-- Empty state -->
      <div v-else-if="!store.flows.length" class="flex min-h-72 flex-col items-center justify-center gap-3 text-center text-(--ui-text-muted)">
        <Workflow class="h-8 w-8 text-(--ui-brand)" />
        <p class="max-w-xs text-sm font-medium">{{ t('flowcockpit.emptyFlows') }}</p>
      </div>

      <div v-else-if="!selectedWorkflow" class="flex min-h-72 flex-col items-center justify-center gap-3 text-center text-(--ui-text-muted)">
        <Workflow class="h-8 w-8 text-(--ui-brand)" />
        <p class="text-base font-semibold text-(--ui-text)">{{ t('flowcockpit.workflowSelectorEmptyTitle') }}</p>
        <p class="max-w-md text-sm">{{ t('flowcockpit.workflowSelectorEmptySubtitle') }}</p>
      </div>

      <div v-else class="flex flex-col gap-5">
        <BaseCard :title="selectedWorkflow.name" :subtitle="selectedWorkflow.description">
          <div class="mb-4 grid gap-2 rounded-2xl border border-(--ui-border) bg-(--ui-panel-muted) p-4 text-sm md:grid-cols-2">
            <p class="text-(--ui-text-muted)">{{ t('flowcockpit.owner') }}: <span class="text-(--ui-text)">{{ selectedWorkflow.owner || '-' }}</span></p>
            <p class="text-(--ui-text-muted)">{{ t('flowcockpit.version') }}: <span class="text-(--ui-text)">{{ selectedWorkflow.version || '-' }}</span></p>
            <p class="text-(--ui-text-muted)">{{ t('flowcockpit.domainTag') }}: <span class="text-(--ui-text)">{{ selectedWorkflow.domainTag || '-' }}</span></p>
            <p class="text-(--ui-text-muted)">{{ t('flowcockpit.businessGoal') }}: <span class="text-(--ui-text)">{{ selectedWorkflow.businessGoal || '-' }}</span></p>
          </div>

          <div class="mb-4 flex justify-end gap-2">
            <BaseButton variant="ghost" size="sm" @click="showStory = !showStory">
              <template #leading><BookOpenText /></template>
              {{ showStory ? t('flowcockpit.hideStory') : t('flowcockpit.showStory') }}
            </BaseButton>
            <BaseButton
              variant="ghost"
              size="sm"
              :disabled="!selectedNode"
              @click="nodeDetailsOpen = true"
            >
              {{ t('flowcockpit.openNodeDetails') }}
            </BaseButton>
          </div>

          <div v-if="showStory" class="mb-4 rounded-2xl border border-(--ui-border) bg-(--ui-panel-muted) p-4">
            <p class="text-sm font-semibold text-(--ui-text)">{{ t('flowcockpit.storyTitle') }}</p>
            <div v-if="store.loadingNarrative" class="mt-2 text-sm text-(--ui-text-muted)">{{ t('common.loading') }}</div>
            <div v-else-if="store.narrativeError" class="mt-2 text-sm text-(--ui-danger)">{{ store.narrativeError }}</div>
            <ol v-else-if="store.narrative?.paths?.length" class="mt-2 space-y-1 text-sm text-(--ui-text-muted)">
              <li v-for="(path, idx) in store.narrative.paths" :key="path">{{ idx + 1 }}. {{ path }}</li>
            </ol>
            <p v-else class="mt-2 text-sm text-(--ui-text-muted)">{{ t('flowcockpit.emptyStory') }}</p>
          </div>

          <BpmDiagram
            :nodes="selectedWorkflow.nodes || []"
            :edges="selectedWorkflow.edges || []"
            :type-label="typeLabel"
            @select-node="selectNode"
          />
        </BaseCard>

        <NodeDetailsDrawer
          :open="nodeDetailsOpen"
          :node="selectedNode"
          :title="t('flowcockpit.nodeDetailsTitle')"
          :empty-label="t('flowcockpit.nodeDetailsEmpty')"
          :purpose-label="t('flowcockpit.purpose')"
          :input-label="t('flowcockpit.inputHint')"
          :output-label="t('flowcockpit.outputHint')"
          :failure-label="t('flowcockpit.failureHint')"
          :close-label="t('flowcockpit.closeNodeDetails')"
          :close-aria-label="t('common.a11y.closeDialog')"
          @close="nodeDetailsOpen = false"
        />
      </div>
    </section>
  </div>
</template>
