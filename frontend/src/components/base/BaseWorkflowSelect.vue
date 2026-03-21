<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ChevronDown, Check } from 'lucide-vue-next'

export interface WorkflowSelectOption {
  id: string
  label: string
  description?: string
}

const props = withDefaults(defineProps<{
  modelValue: string | null
  options: WorkflowSelectOption[]
  label: string
  placeholder: string
  clearLabel: string
  emptyLabel: string
}>(), {
  modelValue: null,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | null): void
}>()

const rootRef = ref<HTMLElement | null>(null)
const open = ref(false)

const selected = computed(() => props.options.find(option => option.id === props.modelValue) ?? null)

function toggleOpen(): void {
  open.value = !open.value
}

function selectOption(id: string | null): void {
  emit('update:modelValue', id)
  open.value = false
}

function onClickOutside(event: MouseEvent): void {
  if (!rootRef.value) {
    return
  }
  if (!rootRef.value.contains(event.target as Node)) {
    open.value = false
  }
}

onMounted(() => {
  document.addEventListener('mousedown', onClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onClickOutside)
})
</script>

<template>
  <div ref="rootRef" class="relative w-full max-w-md">
    <label class="mb-2 block text-xs font-semibold uppercase tracking-[0.14em] text-(--ui-text-muted)">
      {{ label }}
    </label>

    <button
      type="button"
      class="surface-panel flex w-full items-center justify-between rounded-2xl border border-(--ui-border) px-4 py-3 text-left transition-colors hover:border-(--ui-brand-soft)"
      :aria-expanded="open"
      :aria-label="label"
      @click="toggleOpen"
    >
      <span class="truncate" :class="selected ? 'text-(--ui-text)' : 'text-(--ui-text-muted)'">
        {{ selected ? selected.label : placeholder }}
      </span>
      <ChevronDown class="h-4 w-4 shrink-0 text-(--ui-text-muted)" :class="open ? 'rotate-180' : ''" />
    </button>

    <Transition name="select-fade">
      <div
        v-if="open"
        class="surface-panel absolute left-0 right-0 z-20 mt-2 overflow-hidden rounded-2xl border border-(--ui-border) shadow-(--shadow-soft)"
        role="listbox"
      >
        <button
          type="button"
          class="flex w-full items-center justify-between px-4 py-3 text-left text-sm text-(--ui-text-muted) transition-colors hover:bg-(--ui-brand-soft)"
          :aria-selected="modelValue === null"
          @click="selectOption(null)"
        >
          <span>{{ clearLabel }}</span>
          <Check v-if="modelValue === null" class="h-4 w-4" />
        </button>

        <p v-if="!options.length" class="px-4 py-3 text-sm text-(--ui-text-muted)">
          {{ emptyLabel }}
        </p>

        <ul v-else class="max-h-72 overflow-y-auto border-t border-(--ui-border)">
          <li v-for="option in options" :key="option.id">
            <button
              type="button"
              class="flex w-full items-start justify-between gap-3 px-4 py-3 text-left transition-colors hover:bg-(--ui-brand-soft)"
              :aria-selected="option.id === modelValue"
              @click="selectOption(option.id)"
            >
              <span class="min-w-0">
                <span class="block truncate text-sm font-medium text-(--ui-text)">{{ option.label }}</span>
                <span v-if="option.description" class="mt-0.5 block truncate text-xs text-(--ui-text-muted)">{{ option.description }}</span>
              </span>
              <Check v-if="option.id === modelValue" class="mt-0.5 h-4 w-4 shrink-0 text-(--ui-brand)" />
            </button>
          </li>
        </ul>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.select-fade-enter-active,
.select-fade-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.select-fade-enter-from,
.select-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
