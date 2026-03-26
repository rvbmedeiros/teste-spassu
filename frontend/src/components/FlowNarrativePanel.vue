<script setup lang="ts">
import type { FlowNode } from '@/stores/flows'

defineProps<{
  node: FlowNode | null
  title: string
  emptyLabel: string
  purposeLabel: string
  inputLabel: string
  outputLabel: string
  failureLabel: string
}>()
</script>

<template>
  <aside class="rounded-3xl border border-(--ui-border) bg-(--ui-surface-interactive) p-4">
    <h3 class="text-sm font-semibold text-(--ui-text)">{{ title }}</h3>

    <div v-if="!node" class="mt-3 text-sm text-(--ui-text-muted)">
      {{ emptyLabel }}
    </div>

    <div v-else class="mt-3 space-y-3">
      <div>
        <p class="text-sm font-semibold text-(--ui-text)">{{ node.name }}</p>
        <p class="text-xs text-(--ui-text-muted)">{{ node.description || '-' }}</p>
      </div>

      <p class="text-xs text-(--ui-text-muted)">
        {{ purposeLabel }}: <span class="text-(--ui-text)">{{ node.purpose || '-' }}</span>
      </p>
      <p class="text-xs text-(--ui-text-muted)">
        {{ inputLabel }}: <span class="text-(--ui-text)">{{ node.inputHint || '-' }}</span>
      </p>
      <p class="text-xs text-(--ui-text-muted)">
        {{ outputLabel }}: <span class="text-(--ui-text)">{{ node.outputHint || '-' }}</span>
      </p>
      <p class="text-xs text-(--ui-text-muted)">
        {{ failureLabel }}: <span class="text-(--ui-text)">{{ node.failureHint || '-' }}</span>
      </p>
    </div>
  </aside>
</template>
