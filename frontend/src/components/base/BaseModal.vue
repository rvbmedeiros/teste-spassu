<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { X } from 'lucide-vue-next'

const props = withDefaults(defineProps<{
  open: boolean
  title?: string
  description?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
}>(), {
  size: 'md',
})

const emit = defineEmits<{
  (e: 'close'): void
}>()

const { t } = useI18n()

const sizeClass = computed(() => ({
  sm: 'max-w-md',
  md: 'max-w-xl',
  lg: 'max-w-2xl',
  xl: 'max-w-4xl',
}[props.size]))
</script>

<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div
        v-if="open"
        class="fixed inset-0 z-50 overflow-y-auto bg-(--ui-overlay) px-4 py-8 sm:px-6"
        @click.self="emit('close')"
      >
        <div class="flex min-h-full items-center justify-center">
          <div :class="['surface-panel-strong w-full rounded-[1.75rem]', sizeClass]">
            <div v-if="title || description || $slots.header" class="flex items-start justify-between gap-4 border-b border-(--ui-border) px-6 py-5 sm:px-7">
              <div class="min-w-0">
                <slot name="header">
                  <h2 v-if="title" class="text-lg font-semibold tracking-tight text-(--ui-text)">
                    {{ title }}
                  </h2>
                  <p v-if="description" class="mt-1 text-sm leading-6 text-(--ui-text-muted)">
                    {{ description }}
                  </p>
                </slot>
              </div>

              <button
                class="icon-button-shell h-10 w-10"
                type="button"
                :aria-label="t('common.a11y.closeDialog')"
                @click="emit('close')"
              >
                <X class="h-4 w-4" />
              </button>
            </div>

            <div class="p-6 sm:p-7">
              <slot />
            </div>

            <div v-if="$slots.footer" class="border-t border-(--ui-border) px-6 py-4 sm:px-7">
              <slot name="footer" />
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* GPU acceleration for modal panel */
.modal-fade-enter-active > div > div > div {
  will-change: transform;
}

.modal-fade-leave-active > div > div > div {
  will-change: transform;
}
</style>
