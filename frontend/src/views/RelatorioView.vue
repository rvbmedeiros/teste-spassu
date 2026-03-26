<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { BadgeDollarSign, FileText, Library, Sparkles, UsersRound } from 'lucide-vue-next'
import api from '@/services/api'
import { extractApiErrorMessage } from '@/services/errors'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseToast from '@/components/base/BaseToast.vue'

const { t } = useI18n()
const loading = ref(false)
const toast = ref<{ message: string; type: 'success' | 'error' } | null>(null)

async function gerarRelatorio() {
  loading.value = true
  try {
    const response = await api.get('/api/relatorio/pdf', { responseType: 'blob' })
    const url = URL.createObjectURL(response.data)
    const a = document.createElement('a')
    a.href = url
    a.download = 'relatorio-livros.pdf'
    a.click()
    URL.revokeObjectURL(url)
    toast.value = { message: t('relatorio.downloadIniciado'), type: 'success' }
  } catch (error) {
    toast.value = { message: extractApiErrorMessage(error, t), type: 'error' }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-shell">
    <section class="surface-panel-strong rounded-4xl p-6 sm:p-8">
      <div class="grid gap-8 lg:grid-cols-[1fr_20rem] lg:items-end">
        <div class="space-y-4">
          <span class="section-eyebrow">
            <FileText class="h-3.5 w-3.5" />
            {{ t('nav.relatorio') }}
          </span>
          <div class="space-y-2">
            <h1 class="page-title">{{ t('relatorio.title') }}</h1>
            <p class="page-subtitle">{{ t('relatorio.subtitle') }}</p>
          </div>
        </div>

        <div class="metric-tile">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-(--ui-text-muted)">{{ t('relatorio.exportType') }}</span>
            <Sparkles class="h-4 w-4 text-(--ui-brand)" />
          </div>
          <p class="mt-4 text-3xl font-semibold tracking-tight text-(--ui-text)">{{ t('relatorio.exportLabel') }}</p>
        </div>
      </div>
    </section>

    <BaseCard :title="t('relatorio.title')" :subtitle="t('relatorio.subtitle')">
      <div class="grid gap-6 lg:grid-cols-[1fr_16rem] lg:items-center">
        <div class="grid gap-3 sm:grid-cols-3">
          <div class="surface-muted rounded-3xl p-4">
            <Library class="h-4 w-4 text-(--ui-brand)" />
            <p class="mt-3 text-sm font-medium text-(--ui-text)">{{ t('livros.titulo') }}</p>
          </div>
          <div class="surface-muted rounded-3xl p-4">
            <UsersRound class="h-4 w-4 text-(--ui-brand)" />
            <p class="mt-3 text-sm font-medium text-(--ui-text)">{{ t('livros.autores') }}</p>
          </div>
          <div class="surface-muted rounded-3xl p-4">
            <BadgeDollarSign class="h-4 w-4 text-(--ui-brand)" />
            <p class="mt-3 text-sm font-medium text-(--ui-text)">{{ t('livros.valor') }}</p>
          </div>
        </div>

        <BaseButton :loading="loading" size="lg" block @click="gerarRelatorio">
          <template #leading>
            <FileText />
          </template>
          {{ loading ? t('relatorio.gerando') : t('relatorio.gerar') }}
        </BaseButton>
      </div>
    </BaseCard>

    <BaseToast v-if="toast" :message="toast.message" :type="toast.type" @close="toast = null" />
  </div>
</template>
