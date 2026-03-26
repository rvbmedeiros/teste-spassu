<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { X } from 'lucide-vue-next'
import type { FlowNode } from '@/stores/flows'
import BaseButton from './base/BaseButton.vue'

const props = defineProps<{
  open: boolean
  node: FlowNode | null
  title: string
  emptyLabel: string
  purposeLabel: string
  inputLabel: string
  outputLabel: string
  failureLabel: string
  closeLabel: string
  closeAriaLabel: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const panelRef = ref<HTMLElement | null>(null)

watch(() => props.open, async isOpen => {
  if (!isOpen) {
    return
  }

  await nextTick()
  panelRef.value?.focus()
})

const onKeydown = (event: KeyboardEvent) => {
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
        class="fixed inset-0 z-70 bg-(--ui-overlay)"
        @click.self="emit('close')"
      >
        <aside
          ref="panelRef"
          class="surface-panel-strong ml-auto flex h-full w-full max-w-xl flex-col border-l border-(--ui-border)"
          role="dialog"
          aria-modal="true"
          :aria-label="title"
          tabindex="-1"
          @keydown="onKeydown"
        >
          <header class="flex items-start justify-between gap-4 border-b border-(--ui-border) px-6 py-5">
            <div class="space-y-1">
              <h3 class="text-lg font-semibold tracking-tight text-(--ui-text)">{{ title }}</h3>
            </div>
            <button
              type="button"
              class="icon-button-shell h-10 w-10"
              :aria-label="closeAriaLabel"
              @click="emit('close')"
            >
              <X class="h-4 w-4" />
            </button>
          </header>

          <div class="flex-1 space-y-4 overflow-y-auto px-6 py-5">
            <div v-if="!node" class="rounded-2xl border border-(--ui-border) bg-(--ui-panel-muted) p-4 text-sm text-(--ui-text-muted)">
              {{ emptyLabel }}
            </div>

            <div v-else class="space-y-3 rounded-2xl border border-(--ui-border) bg-(--ui-panel-muted) p-4">
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
          </div>

          <footer class="border-t border-(--ui-border) px-6 py-4">
            <div class="flex justify-end">
              <BaseButton variant="secondary" @click="emit('close')">{{ closeLabel }}</BaseButton>
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
