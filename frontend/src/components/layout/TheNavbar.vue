<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, useRoute } from 'vue-router'
import {
  Activity,
  BookOpenText,
  FileText,
  Languages,
  LogOut,
  Menu,
  Tags,
  UsersRound,
  Workflow,
  X,
} from 'lucide-vue-next'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseSwitcher from '@/components/base/BaseSwitcher.vue'
import logo from '@/assets/logo.png'
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
    <div class="surface-panel-strong mx-auto max-w-384 rounded-4xl px-4 py-3 sm:px-5">
      <div class="flex items-center justify-between gap-4">
        <div class="flex items-center gap-2">
          <button
            class="icon-button-shell h-11 w-11 text-(--ui-text) lg:hidden"
            type="button"
            :aria-label="mobileOpen ? t('common.a11y.closeNavigation') : t('common.a11y.openNavigation')"
            @click="mobileOpen = !mobileOpen"
          >
            <X v-if="mobileOpen" class="h-4 w-4" />
            <Menu v-else class="h-4 w-4" />
          </button>

          <RouterLink to="/" class="flex min-w-0 items-center">
            <img :src="logo" alt="Spassu Livros" class="h-10 w-auto sm:h-11" />
          </RouterLink>
        </div>

        <div class="hidden items-center gap-2 lg:flex">
          <RouterLink
            v-for="link in navLinks"
            :key="link.to"
            :to="link.to"
            class="nav-item px-3.5 py-2"
            active-class="bg-(--ui-brand-soft) text-(--ui-brand)"
          >
            <component :is="link.icon" class="h-4 w-4" />
            <span>{{ link.label }}</span>
          </RouterLink>
        </div>

        <div class="flex items-center gap-2 sm:gap-3">
          <span v-if="auth.username" class="status-badge hidden bg-(--ui-panel-muted) text-(--ui-text-muted) md:inline-flex">
            {{ auth.username }}
          </span>

          <div class="hidden lg:flex lg:items-center lg:gap-2">
            <BaseButton variant="secondary" size="sm" @click="switchLocale">
              <template #leading>
                <Languages />
              </template>
              {{ t('common.localeSwitcher') }}
            </BaseButton>

            <BaseSwitcher />
          </div>

          <BaseButton class="hidden sm:inline-flex" variant="ghost" size="sm" @click="auth.logout">
            <template #leading>
              <LogOut />
            </template>
            {{ t('nav.logout') }}
          </BaseButton>

        </div>
      </div>

      <Transition name="nav-reveal">
        <div v-if="mobileOpen" class="mt-4 border-t border-(--ui-border) pt-4 lg:hidden">
          <div class="grid gap-2">
            <RouterLink
              v-for="link in navLinks"
              :key="link.to"
              :to="link.to"
              class="nav-item px-4 py-3"
              active-class="bg-(--ui-brand-soft) text-(--ui-brand)"
            >
              <component :is="link.icon" class="h-4 w-4" />
              <span>{{ link.label }}</span>
            </RouterLink>
          </div>

          <div class="mt-4 flex flex-col gap-3 border-t border-(--ui-border) pt-4 sm:hidden">
            <BaseButton variant="secondary" block @click="switchLocale">
              <template #leading>
                <Languages />
              </template>
              {{ t('common.localeSwitcher') }}
            </BaseButton>
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
