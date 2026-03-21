<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, useRoute } from 'vue-router'
import {
  Activity,
  BookOpenText,
  FileText,
  Languages,
  LibraryBig,
  LogOut,
  Menu,
  Tags,
  UsersRound,
  Workflow,
  X,
} from 'lucide-vue-next'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseSwitcher from '@/components/base/BaseSwitcher.vue'
import { useAuthStore } from '@/stores/auth'

const { t, locale } = useI18n()
const auth = useAuthStore()
const route = useRoute()
const mobileOpen = ref(false)

const navLinks = computed(() => [
  { to: '/livros', label: t('nav.livros'), icon: BookOpenText },
  { to: '/autores', label: t('nav.autores'), icon: UsersRound },
  { to: '/assuntos', label: t('nav.assuntos'), icon: Tags },
  { to: '/relatorio', label: t('nav.relatorio'), icon: FileText },
  { to: '/flowcockpit', label: t('nav.flowcockpit'), icon: Workflow },
  { to: '/logs', label: t('nav.logs'), icon: Activity },
])

watch(() => route.fullPath, () => {
  mobileOpen.value = false
})

function switchLocale() {
  locale.value = locale.value === 'pt-BR' ? 'en' : 'pt-BR'
  localStorage.setItem('locale', locale.value)
}
</script>

<template>
  <nav class="sticky top-0 z-40 px-4 pt-4 sm:px-6 lg:px-8">
    <div class="surface-panel-strong mx-auto max-w-7xl rounded-4xl px-4 py-3 sm:px-5">
      <div class="flex items-center justify-between gap-4">
        <RouterLink to="/" class="flex min-w-0 items-center gap-3">
          <span class="flex h-11 w-11 items-center justify-center rounded-2xl bg-(--ui-brand) text-white shadow-[0_16px_36px_rgba(37,99,235,0.24)]">
            <LibraryBig class="h-5 w-5" />
          </span>
          <span class="min-w-0">
            <span class="block truncate text-sm font-semibold uppercase tracking-[0.22em] text-(--ui-brand)">Spassu</span>
            <span class="block truncate text-base font-semibold tracking-tight text-(--ui-text)">Livros Control Center</span>
          </span>
        </RouterLink>

        <div class="hidden items-center gap-2 lg:flex">
          <RouterLink
            v-for="link in navLinks"
            :key="link.to"
            :to="link.to"
            class="inline-flex items-center gap-2 rounded-2xl px-3.5 py-2 text-sm font-medium text-(--ui-text-muted) transition-colors hover:bg-black/5 hover:text-(--ui-text) dark:hover:bg-white/8"
            active-class="bg-[color:var(--ui-brand-soft)] text-[color:var(--ui-brand)]"
          >
            <component :is="link.icon" class="h-4 w-4" />
            <span>{{ link.label }}</span>
          </RouterLink>
        </div>

        <div class="flex items-center gap-2 sm:gap-3">
          <span v-if="auth.username" class="hidden rounded-full border border-(--ui-border) bg-white/60 px-3 py-1.5 text-xs font-medium text-(--ui-text-muted) md:inline-flex dark:bg-white/6">
            {{ auth.username }}
          </span>

          <BaseButton variant="secondary" size="sm" @click="switchLocale">
            <template #leading>
              <Languages />
            </template>
            {{ t('common.localeSwitcher') }}
          </BaseButton>

          <BaseSwitcher class="hidden sm:inline-flex" />

          <BaseButton class="hidden sm:inline-flex" variant="ghost" size="sm" @click="auth.logout">
            <template #leading>
              <LogOut />
            </template>
            {{ t('nav.logout') }}
          </BaseButton>

          <button
            class="inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-(--ui-border) bg-white/70 text-(--ui-text) shadow-(--shadow-soft) transition-colors hover:bg-white dark:bg-white/6 lg:hidden"
            type="button"
            :aria-label="mobileOpen ? 'Close navigation' : 'Open navigation'"
            @click="mobileOpen = !mobileOpen"
          >
            <X v-if="mobileOpen" class="h-4 w-4" />
            <Menu v-else class="h-4 w-4" />
          </button>
        </div>
      </div>

      <Transition name="nav-reveal">
        <div v-if="mobileOpen" class="mt-4 border-t border-(--ui-border) pt-4 lg:hidden">
          <div class="grid gap-2">
            <RouterLink
              v-for="link in navLinks"
              :key="link.to"
              :to="link.to"
              class="inline-flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium text-(--ui-text-muted) transition-colors hover:bg-black/5 hover:text-(--ui-text) dark:hover:bg-white/8"
              active-class="bg-[color:var(--ui-brand-soft)] text-[color:var(--ui-brand)]"
            >
              <component :is="link.icon" class="h-4 w-4" />
              <span>{{ link.label }}</span>
            </RouterLink>
          </div>

          <div class="mt-4 flex flex-col gap-3 border-t border-(--ui-border) pt-4 sm:hidden">
            <BaseSwitcher />
            <BaseButton variant="ghost" block @click="auth.logout">
              <template #leading>
                <LogOut />
              </template>
              {{ t('nav.logout') }}
            </BaseButton>
          </div>
        </div>
      </Transition>
    </div>
  </nav>
</template>

<style scoped>
.nav-reveal-enter-active,
.nav-reveal-leave-active {
  transition: all 0.22s ease;
}

.nav-reveal-enter-from,
.nav-reveal-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
