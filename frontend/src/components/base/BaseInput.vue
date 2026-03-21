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

const baseClass = 'field-shell focus:border-(--ui-brand) focus:ring-4 focus:ring-(--ui-focus-ring) disabled:cursor-not-allowed disabled:opacity-55'

const inputClass = computed(() => {
  const errorClass = props.error
    ? 'border-(--ui-danger) focus:border-(--ui-danger) focus:ring-(--ui-danger-soft)'
    : 'border-(--ui-border-strong)'
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
    <label v-if="label" class="text-sm font-medium text-(--ui-text)">
      {{ label }}<span v-if="required" class="ml-0.5 text-(--ui-danger)">*</span>
    </label>

    <div class="relative">
      <div v-if="$slots.leading" class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-(--ui-placeholder) [&_svg]:h-4 [&_svg]:w-4">
        <slot name="leading" />
      </div>

      <input
        v-bind="$attrs"
        :value="modelValue ?? ''"
        :class="inputClass"
        @input="handleInput"
      />

      <div v-if="$slots.trailing" class="absolute inset-y-0 right-0 flex items-center pr-3 text-(--ui-placeholder) [&_svg]:h-4 [&_svg]:w-4">
        <slot name="trailing" />
      </div>
    </div>

    <p v-if="error || hint" :class="['text-xs leading-5', error ? 'text-(--ui-danger)' : 'text-(--ui-text-muted)']">
      {{ error || hint }}
    </p>
  </div>
</template>
