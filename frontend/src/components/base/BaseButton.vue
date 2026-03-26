<script setup lang="ts">
import { computed, useSlots } from 'vue'
import { LoaderCircle } from 'lucide-vue-next'

const props = withDefaults(defineProps<{
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  loading?: boolean
  type?: 'button' | 'submit' | 'reset'
  block?: boolean
}>(), {
  variant: 'primary',
  size: 'md',
  type: 'button',
  block: false,
})

const slots = useSlots()

const variantMap = {
  primary: 'border-transparent bg-(--ui-brand) text-(--ui-on-brand) shadow-[var(--ui-shadow-brand)] hover:-translate-y-0.5 hover:bg-(--ui-brand-strong) focus-visible:ring-(--ui-brand)',
  secondary: 'interactive-surface text-(--ui-text) hover:-translate-y-0.5 focus-visible:ring-(--ui-brand)',
  danger: 'border-transparent bg-(--ui-danger) text-(--ui-on-brand) shadow-[var(--ui-shadow-danger)] hover:-translate-y-0.5 hover:brightness-95 focus-visible:ring-(--ui-danger)',
  ghost: 'border-transparent bg-transparent text-(--ui-text-muted) hover:bg-(--ui-surface-active) hover:text-(--ui-text) focus-visible:ring-(--ui-brand)',
}

const iconSizeMap = {
  sm: 'h-9 w-9',
  md: 'h-11 w-11',
  lg: 'h-12 w-12',
}

const textSizeMap = {
  sm: 'px-3.5 py-2 text-xs',
  md: 'px-4.5 py-2.5 text-sm',
  lg: 'px-5.5 py-3 text-sm',
}

const iconOnly = computed(() => !slots.default)

const variantClass = variantMap[props.variant]

const sizeClass = computed(() =>
  iconOnly.value ? iconSizeMap[props.size] : textSizeMap[props.size]
)
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="[
      'group inline-flex max-w-full min-w-0 items-center justify-center gap-2 rounded-2xl border font-semibold transition-[background-color,color,box-shadow,transform] duration-200 focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-(--ui-bg) disabled:pointer-events-none disabled:opacity-50',
      variantClass,
      sizeClass,
      block ? 'w-full' : '',
    ]"
  >
    <span v-if="loading" class="shrink-0">
      <LoaderCircle class="h-4 w-4 animate-spin" />
    </span>
    <span v-else-if="$slots.leading" class="shrink-0 text-current [&_svg]:h-4 [&_svg]:w-4">
      <slot name="leading" />
    </span>
    <span v-if="$slots.default" class="min-w-0 truncate">
      <slot />
    </span>
    <span v-if="$slots.trailing && !loading" class="shrink-0 text-current [&_svg]:h-4 [&_svg]:w-4">
      <slot name="trailing" />
    </span>
  </button>
</template>
