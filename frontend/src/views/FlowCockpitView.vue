<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  Activity,
  AlertTriangle,
  ArrowDown,
  RotateCcw,
  Workflow,
} from 'lucide-vue-next'
import { useFlowStore, type FlowGraph } from '@/stores/flows'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseWorkflowSelect from '@/components/base/BaseWorkflowSelect.vue'

const { t } = useI18n()
const store = useFlowStore()
const STEPS_VISIBLE = 3

const selectedWorkflowId = ref<string | null>(null)
const expandedFlows = ref<Set<string>>(new Set())

onMounted(async () => {
  await store.fetchFlows()
})

const activeFlows = computed(() => store.flows.length)
const workflowOptions = computed(() => store.flows.map(flow => ({
  id: flow.id,
  label: flow.name,
  description: flow.description,
})))
const selectedWorkflow = computed(() => store.flows.find(flow => flow.id === selectedWorkflowId.value) ?? null)
const selectedVisibleSteps = computed(() => selectedWorkflow.value ? visibleSteps(selectedWorkflow.value) : [])

const isExpanded = (flowId: string) => expandedFlows.value.has(flowId)

const visibleSteps = (flow: FlowGraph) => (isExpanded(flow.id) ? flow.steps : flow.steps.slice(0, STEPS_VISIBLE))

const toggleFlow = (flowId: string) => {
  const nextExpanded = new Set(expandedFlows.value)
  if (nextExpanded.has(flowId)) {
    nextExpanded.delete(flowId)
  } else {
    nextExpanded.add(flowId)
  }
  expandedFlows.value = nextExpanded
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
          <BaseWorkflowSelect
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
          <ol>
            <li v-for="(step, index) in selectedVisibleSteps" :key="step.order">
              <div class="mx-auto max-w-2xl rounded-3xl border border-(--ui-border) bg-white/55 p-5 dark:bg-white/4">
                <span class="inline-flex items-center justify-center rounded-full bg-(--ui-brand-soft) px-2.5 py-1 text-xs font-semibold text-(--ui-brand)">
                  {{ t('flowcockpit.step') }} {{ step.order }}
                </span>

                <div class="mt-3 flex items-start justify-between gap-3">
                  <div>
                    <p class="font-semibold text-(--ui-text)">{{ step.name }}</p>
                    <p v-if="step.description" class="mt-1 text-sm leading-6 text-(--ui-text-muted)">{{ step.description }}</p>
                  </div>
                </div>

                <p v-if="step.rollbackStep" class="mt-3 inline-flex items-center gap-2 rounded-full bg-amber-500/12 px-3 py-1 text-xs font-medium text-amber-700 dark:text-amber-300">
                  <RotateCcw class="h-3.5 w-3.5" />
                  {{ step.rollbackStep }}
                </p>
              </div>

              <div v-if="index !== selectedVisibleSteps.length - 1" class="flex justify-center py-3">
                <div class="flex flex-col items-center text-(--ui-text-muted)">
                  <span class="h-4 w-px bg-(--ui-border-strong)" />
                  <span class="rounded-full bg-(--ui-brand-soft) p-1 text-(--ui-brand)">
                    <ArrowDown class="h-3.5 w-3.5" />
                  </span>
                  <span class="h-4 w-px bg-(--ui-border-strong)" />
                </div>
              </div>
            </li>
          </ol>

          <div v-if="selectedWorkflow.steps.length > STEPS_VISIBLE" class="mt-4 flex justify-center">
            <BaseButton variant="ghost" size="sm" @click="toggleFlow(selectedWorkflow.id)">
              {{ isExpanded(selectedWorkflow.id) ? t('flowcockpit.showLess') : t('flowcockpit.showAllSteps', { n: selectedWorkflow.steps.length }) }}
            </BaseButton>
          </div>
        </BaseCard>
      </div>
    </section>
  </div>
</template>
