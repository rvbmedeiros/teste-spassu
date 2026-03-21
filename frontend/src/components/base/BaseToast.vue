<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-vue-next'

const props = withDefaults(defineProps<{
  message: string
  type?: 'success' | 'error' | 'info'
  duration?: number
}>(), {
  type: 'success',
  duration: 4000,
})

const emit = defineEmits<{ (e: 'close'): void }>()

// Static icon and style maps to avoid repeated computed evaluation
const iconMap = {
  success: CheckCircle2,
  error: AlertCircle,
  info: Info,
}

const variantClassMap = {
  success: 'border-emerald-500/20 bg-emerald-500 text-white',
  error: 'border-red-500/20 bg-red-600 text-white',
  info: 'border-blue-500/20 bg-[color:var(--ui-brand)] text-white',
}

const icon = iconMap[props.type]
const variantClass = variantClassMap[props.type]

let timeoutId: ReturnType<typeof setTimeout> | undefined

onMounted(() => {
  if (props.duration > 0) {
    timeoutId = setTimeout(() => emit('close'), props.duration)
  }
})

onBeforeUnmount(() => {
  if (timeoutId) {
    clearTimeout(timeoutId)
  }
})
</script>

<template>
  <Transition name="toast-slide">
    <div
      class="fixed bottom-4 right-4 z-50 flex max-w-sm items-start gap-3 rounded-[1.25rem] border px-4 py-3 shadow-2xl"
      :class="variantClass"
    >
      <component :is="icon" class="mt-0.5 h-5 w-5 shrink-0" />
      <div class="min-w-0 flex-1">
        <p class="text-sm font-medium leading-6">{{ message }}</p>
      </div>
      <button class="rounded-full p-1 text-white/75 transition hover:bg-white/10 hover:text-white" type="button" @click="emit('close')">
        <X class="h-4 w-4" />
      </button>
    </div>
  </Transition>
</template>

<style scoped>
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.toast-slide-enter-from,
.toast-slide-leave-to {
  opacity: 0;
  transform: translate3d(0, 12px, 0);
}
</style>
