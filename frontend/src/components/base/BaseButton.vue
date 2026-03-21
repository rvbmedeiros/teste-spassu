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

// Static variant and size maps to avoid repeated computed evaluation
const variantMap = {
  primary: 'border-transparent bg-[color:var(--ui-brand)] text-white shadow-[0_14px_34px_rgba(37,99,235,0.28)] hover:-translate-y-0.5 hover:bg-[color:var(--ui-brand-strong)] focus-visible:ring-[color:var(--ui-brand)]',
  secondary: 'border-[color:var(--ui-border-strong)] bg-white/80 text-[color:var(--ui-text)] shadow-[var(--shadow-soft)] hover:-translate-y-0.5 hover:bg-white dark:bg-white/6 dark:hover:bg-white/10 focus-visible:ring-[color:var(--ui-brand)]',
  danger: 'border-transparent bg-[color:var(--ui-danger)] text-white shadow-[0_14px_34px_rgba(220,38,38,0.22)] hover:-translate-y-0.5 hover:bg-red-700 focus-visible:ring-[color:var(--ui-danger)]',
  ghost: 'border-transparent bg-transparent text-[color:var(--ui-text-muted)] hover:bg-black/5 hover:text-[color:var(--ui-text)] dark:hover:bg-white/8 focus-visible:ring-[color:var(--ui-brand)]',
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
