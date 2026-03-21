<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { Search, X } from 'lucide-vue-next'
import BaseButton from './BaseButton.vue'
import BaseInput from './BaseInput.vue'

interface SelectableItem {
  id: number
  label: string
}

const props = withDefaults(defineProps<{
  open: boolean
  title: string
  description?: string
  items: SelectableItem[]
  selectedIds: number[]
  searchPlaceholder: string
  selectedCountLabel: string
  emptyStateLabel: string
  cancelLabel: string
  clearLabel: string
  applyLabel: string
}>(), {
  description: '',
})

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'apply', ids: number[]): void
}>()

const query = ref('')
const activeIndex = ref(0)
const localSelected = ref<Set<number>>(new Set())
const optionRefs = ref<Array<HTMLButtonElement | null>>([])
const contentRef = ref<HTMLElement | null>(null)

const filteredItems = computed(() => {
  const normalizedQuery = query.value.trim().toLowerCase()
  if (!normalizedQuery) {
    return props.items
  }

  return props.items.filter(item => item.label.toLowerCase().includes(normalizedQuery))
})

const selectedCount = computed(() => localSelected.value.size)

watch(() => props.open, async isOpen => {
  if (!isOpen) {
    return
  }

  query.value = ''
  activeIndex.value = 0
  localSelected.value = new Set(props.selectedIds)
  await nextTick()
  const input = contentRef.value?.querySelector('input')
  input?.focus()
})

watch(filteredItems, items => {
  if (!items.length) {
    activeIndex.value = 0
    return
  }

  if (activeIndex.value > items.length - 1) {
    activeIndex.value = items.length - 1
  }
})

function isSelected(id: number): boolean {
  return localSelected.value.has(id)
}

function toggleSelection(id: number): void {
  if (localSelected.value.has(id)) {
    localSelected.value.delete(id)
    return
  }

  localSelected.value.add(id)
}

function clearSelection(): void {
  localSelected.value = new Set()
}

function apply(): void {
  emit('apply', Array.from(localSelected.value))
}

function focusActiveOption(): void {
  optionRefs.value[activeIndex.value]?.focus()
}

function onListboxKeydown(event: KeyboardEvent): void {
  if (!filteredItems.value.length) {
    return
  }

  if (event.key === 'ArrowDown') {
    event.preventDefault()
    activeIndex.value = Math.min(activeIndex.value + 1, filteredItems.value.length - 1)
    focusActiveOption()
    return
  }

  if (event.key === 'ArrowUp') {
    event.preventDefault()
    activeIndex.value = Math.max(activeIndex.value - 1, 0)
    focusActiveOption()
    return
  }

  if (event.key === 'Home') {
    event.preventDefault()
    activeIndex.value = 0
    focusActiveOption()
    return
  }

  if (event.key === 'End') {
    event.preventDefault()
    activeIndex.value = filteredItems.value.length - 1
    focusActiveOption()
    return
  }

  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    const item = filteredItems.value[activeIndex.value]
    if (item) {
      toggleSelection(item.id)
    }
    return
  }

  if (event.key === 'Escape') {
    event.preventDefault()
    emit('close')
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="drawer-slide">
      <div
        v-if="open"
        class="fixed inset-0 z-70 bg-slate-950/45"
        @click.self="emit('close')"
      >
        <aside
          ref="contentRef"
          class="surface-panel-strong ml-auto flex h-full w-full max-w-xl flex-col border-l border-(--ui-border)"
          role="dialog"
          aria-modal="true"
          :aria-label="title"
        >
          <header class="flex items-start justify-between gap-4 border-b border-(--ui-border) px-6 py-5">
            <div class="space-y-1">
              <h3 class="text-lg font-semibold tracking-tight text-(--ui-text)">{{ title }}</h3>
              <p v-if="description" class="text-sm text-(--ui-text-muted)">{{ description }}</p>
            </div>
            <button
              type="button"
              class="inline-flex h-10 w-10 items-center justify-center rounded-2xl border border-(--ui-border) bg-white/70 text-slate-500 transition-colors hover:text-(--ui-text) dark:bg-white/6"
              :aria-label="cancelLabel"
              @click="emit('close')"
            >
              <X class="h-4 w-4" />
            </button>
          </header>

          <div class="flex-1 space-y-4 overflow-y-auto px-6 py-5">
            <BaseInput
              v-model="query"
              type="search"
              :label="searchPlaceholder"
              :placeholder="searchPlaceholder"
            >
              <template #leading>
                <Search />
              </template>
            </BaseInput>

            <p class="text-xs font-semibold uppercase tracking-[0.14em] text-(--ui-text-muted)">
              {{ selectedCountLabel }}: {{ selectedCount }}
            </p>

            <div
              class="surface-muted max-h-88 overflow-y-auto rounded-3xl p-2"
              role="listbox"
              aria-multiselectable="true"
              tabindex="0"
              @keydown="onListboxKeydown"
            >
              <p v-if="!filteredItems.length" class="px-3 py-4 text-sm text-(--ui-text-muted)">
                {{ emptyStateLabel }}
              </p>

              <ul v-else class="space-y-1">
                <li v-for="(item, index) in filteredItems" :key="item.id">
                  <button
                    :ref="el => optionRefs[index] = el as HTMLButtonElement"
                    type="button"
                    role="option"
                    :aria-selected="isSelected(item.id)"
                    :class="[
                      'flex w-full items-center justify-between rounded-2xl px-3 py-2.5 text-left text-sm transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/45',
                      isSelected(item.id)
                        ? 'bg-(--ui-brand) text-white'
                        : 'text-(--ui-text) hover:bg-(--ui-brand-soft)'
                    ]"
                    @click="toggleSelection(item.id)"
                    @focus="activeIndex = index"
                  >
                    <span class="truncate">{{ item.label }}</span>
                    <span
                      :class="[
                        'inline-flex h-5 w-5 items-center justify-center rounded-md border text-xs',
                        isSelected(item.id)
                          ? 'border-white/65 bg-white/22 text-white'
                          : 'border-(--ui-border-strong) text-transparent'
                      ]"
                      aria-hidden="true"
                    >
                      ✓
                    </span>
                  </button>
                </li>
              </ul>
            </div>
          </div>

          <footer class="border-t border-(--ui-border) px-6 py-4">
            <div class="flex flex-wrap justify-end gap-2">
              <BaseButton variant="secondary" @click="clearSelection">{{ clearLabel }}</BaseButton>
              <BaseButton variant="secondary" @click="emit('close')">{{ cancelLabel }}</BaseButton>
              <BaseButton @click="apply">{{ applyLabel }}</BaseButton>
            </div>
          </footer>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.drawer-slide-enter-active,
.drawer-slide-leave-active {
  transition: opacity 0.2s ease;
}

.drawer-slide-enter-active aside,
.drawer-slide-leave-active aside {
  transition: transform 0.24s ease;
}

.drawer-slide-enter-from,
.drawer-slide-leave-to {
  opacity: 0;
}

.drawer-slide-enter-from aside,
.drawer-slide-leave-to aside {
  transform: translateX(20px);
}
</style>
