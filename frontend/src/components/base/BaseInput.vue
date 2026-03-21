<script setup lang="ts">
import { computed, useSlots } from 'vue'

defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: string | number | null
  modelModifiers?: { number?: boolean; trim?: boolean }
  label?: string
  error?: string
  hint?: string
  required?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number): void
}>()

const slots = useSlots()

// Memoize static class strings to avoid re-evaluation on every keystroke
const baseClass = 'w-full rounded-2xl border bg-white/75 px-4 py-3 text-sm text-[color:var(--ui-text)] shadow-[var(--shadow-soft)] outline-none transition-colors duration-200 dark:bg-white/6 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:border-[color:var(--ui-brand)] focus:ring-4 focus:ring-blue-500/12'

const inputClass = computed(() => {
  const errorClass = props.error
    ? 'border-red-400/70 focus:border-red-500 focus:ring-red-500/12'
    : 'border-[color:var(--ui-border-strong)]'
  const paddingClass = `${slots.leading ? 'pl-11' : ''} ${slots.trailing ? 'pr-11' : ''}`
  return `${baseClass} ${errorClass} ${paddingClass}`
})

function handleInput(event: Event) {
  const nextValue = (event.target as HTMLInputElement).value

  if (props.modelModifiers?.trim) {
    if (props.modelModifiers.number) {
      emit('update:modelValue', Number(nextValue.trim()))
      return
    }

    emit('update:modelValue', nextValue.trim())
    return
  }

  if (props.modelModifiers?.number) {
    emit('update:modelValue', Number(nextValue))
    return
  }

  emit('update:modelValue', nextValue)
}
</script>

<template>
  <div class="flex flex-col gap-2">
    <label v-if="label" class="text-sm font-medium text-[color:var(--ui-text)]">
      {{ label }}<span v-if="required" class="ml-0.5 text-red-500">*</span>
    </label>

    <div class="relative">
      <div v-if="$slots.leading" class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400 [&_svg]:h-4 [&_svg]:w-4">
        <slot name="leading" />
      </div>

      <input
        v-bind="$attrs"
        :value="modelValue ?? ''"
        :class="inputClass"
        @input="handleInput"
      />

      <div v-if="$slots.trailing" class="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 [&_svg]:h-4 [&_svg]:w-4">
        <slot name="trailing" />
      </div>
    </div>

    <p v-if="error || hint" :class="['text-xs leading-5', error ? 'text-red-500' : 'text-[color:var(--ui-text-muted)]']">
      {{ error || hint }}
    </p>
  </div>
</template>
