<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(defineProps<{
  title?: string
  subtitle?: string
  padding?: boolean
  tone?: 'default' | 'brand' | 'muted'
}>(), {
  padding: true,
  tone: 'default',
})

const slots = useSlots()

// Static tone class map to avoid repeated computed evaluation
const toneClassMap = {
  default: 'surface-panel',
  brand: 'surface-panel border-[color:var(--ui-brand-soft)]',
  muted: 'surface-muted shadow-none',
}

const hasHeader = computed(() => Boolean(props.title || props.subtitle || slots.header || slots.actions))

const toneClass = toneClassMap[props.tone]
</script>

<template>
  <section :class="['overflow-hidden rounded-[1.75rem]', toneClass]">
    <div v-if="hasHeader" class="flex flex-col gap-4 border-b border-[color:var(--ui-border)] px-6 py-5 sm:flex-row sm:items-start sm:justify-between">
      <div class="min-w-0">
        <slot name="header">
          <h2 v-if="title" class="text-lg font-semibold tracking-tight text-[color:var(--ui-text)]">
            {{ title }}
          </h2>
          <p v-if="subtitle" class="mt-1 text-sm leading-6 text-[color:var(--ui-text-muted)]">
            {{ subtitle }}
          </p>
        </slot>
      </div>
      <div v-if="$slots.actions" class="flex shrink-0 items-center gap-2">
        <slot name="actions" />
      </div>
    </div>

    <div :class="padding ? 'p-6 sm:p-7' : ''">
      <slot />
    </div>

    <div v-if="$slots.footer" class="border-t border-[color:var(--ui-border)] px-6 py-4">
      <slot name="footer" />
    </div>
  </section>
</template>
