<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { PencilLine, Plus, Trash2, UserRound, UsersRound } from 'lucide-vue-next'
import { extractApiErrorMessage } from '@/services/errors'
import { useAutorStore } from '@/stores/autores'
import { useAuthStore } from '@/stores/auth'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseModal from '@/components/base/BaseModal.vue'
import BaseToast from '@/components/base/BaseToast.vue'

const { t } = useI18n()
const store = useAutorStore()
const auth = useAuthStore()

onMounted(() => store.fetchAll())

const showModal = ref(false)
const editingId = ref<number | null>(null)
const form = ref({ nome: '' })
const confirmDeleteId = ref<number | null>(null)
const toast = ref<{ message: string; type: 'success' | 'error' } | null>(null)

function openCreate() {
  editingId.value = null
  form.value = { nome: '' }
  showModal.value = true
}

async function openEdit(codAu: number) {
  try {
    const autor = await store.buscarPorId(codAu)
    editingId.value = codAu
    form.value = { nome: autor.nome }
    showModal.value = true
  } catch (error) {
    toast.value = { message: extractApiErrorMessage(error, t), type: 'error' }
  }
}

async function save() {
  try {
    if (editingId.value) {
      await store.atualizar(editingId.value, form.value.nome)
    } else {
      await store.criar(form.value.nome)
    }
    showModal.value = false
    toast.value = { message: t('autores.salvoSucesso'), type: 'success' }
  } catch (error) {
    toast.value = { message: extractApiErrorMessage(error, t), type: 'error' }
  }
}

async function confirmDelete(codAu: number) {
  try {
    await store.excluir(codAu)
    confirmDeleteId.value = null
    toast.value = { message: t('autores.excluidoSucesso'), type: 'success' }
  } catch (error) {
    toast.value = { message: extractApiErrorMessage(error, t), type: 'error' }
  }
}
</script>

<template>
  <div class="page-shell">
    <section class="surface-panel-strong rounded-4xl p-6 sm:p-8">
      <div class="flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
        <div class="space-y-4">
          <span class="section-eyebrow">
            <UsersRound class="h-3.5 w-3.5" />
            {{ t('nav.autores') }}
          </span>
          <div class="space-y-2">
            <h1 class="page-title">{{ t('autores.title') }}</h1>
            <p class="page-subtitle">{{ t('autores.subtitle') }}</p>
          </div>
        </div>

        <BaseButton v-if="auth.isAdmin" size="lg" @click="openCreate">
          <template #leading>
            <Plus />
          </template>
          {{ t('autores.novo') }}
        </BaseButton>
      </div>
    </section>

    <BaseCard :padding="false" :title="t('autores.title')" :subtitle="t('autores.subtitle')">
      <div class="table-shell">
        <div v-if="store.loading" class="flex min-h-60 items-center justify-center px-6 text-sm font-medium text-(--ui-text-muted)">
          {{ t('common.loading') }}
        </div>
        <div v-else-if="store.error" class="flex min-h-60 items-center justify-center px-6 text-sm font-medium text-red-500">
          {{ store.error }}
        </div>
        <div v-else-if="!store.autores.length" class="flex min-h-60 flex-col items-center justify-center gap-3 px-6 text-center text-(--ui-text-muted)">
          <UsersRound class="h-8 w-8 text-(--ui-brand)" />
          <p class="text-sm font-medium">{{ t('autores.semRegistros') }}</p>
        </div>
        <table v-else class="min-w-full text-sm">
          <thead>
            <tr class="table-head">
              <th class="px-5 py-4">Cód.</th>
              <th class="px-5 py-4">{{ t('autores.nome') }}</th>
              <th v-if="auth.isAdmin" class="px-5 py-4 text-right">{{ t('common.acoes') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="autor in store.autores" :key="autor.codAu" class="table-row">
              <td class="px-5 py-4 text-(--ui-text-muted)">{{ autor.codAu }}</td>
              <td class="px-5 py-4">
                <div class="flex items-center gap-3">
                  <span class="rounded-2xl bg-(--ui-brand-soft) p-2 text-(--ui-brand)">
                    <UserRound class="h-4 w-4" />
                  </span>
                  <span class="font-semibold text-(--ui-text)">{{ autor.nome }}</span>
                </div>
              </td>
              <td v-if="auth.isAdmin" class="px-5 py-4">
                <div class="flex justify-end gap-2">
                  <BaseButton variant="secondary" size="sm" @click="openEdit(autor.codAu)">
                    <template #leading>
                      <PencilLine />
                    </template>
                    {{ t('common.editar') }}
                  </BaseButton>
                  <BaseButton variant="danger" size="sm" @click="confirmDeleteId = autor.codAu">
                    <template #leading>
                      <Trash2 />
                    </template>
                    {{ t('common.excluir') }}
                  </BaseButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </BaseCard>

    <BaseModal :open="showModal" :title="editingId ? t('common.editar') : t('autores.novo')" :description="t('autores.subtitle')" @close="showModal = false">
      <form class="space-y-4" @submit.prevent="save">
        <BaseInput :label="t('autores.nome')" v-model="form.nome" required>
          <template #leading>
            <UserRound />
          </template>
        </BaseInput>
      </form>

      <template #footer>
        <div class="flex justify-end gap-2">
          <BaseButton variant="secondary" @click="showModal = false">{{ t('livros.cancelar') }}</BaseButton>
          <BaseButton @click="save">{{ t('livros.salvar') }}</BaseButton>
        </div>
      </template>
    </BaseModal>

    <BaseModal :open="confirmDeleteId !== null" :title="t('livros.confirmarExclusao')" :description="t('livros.confirmarExclusaoMsg')" size="sm" @close="confirmDeleteId = null">
      <template #footer>
        <div class="flex justify-end gap-2">
          <BaseButton variant="secondary" @click="confirmDeleteId = null">{{ t('livros.cancelar') }}</BaseButton>
          <BaseButton variant="danger" @click="confirmDeleteId && confirmDelete(confirmDeleteId)">
            <template #leading>
              <Trash2 />
            </template>
            {{ t('livros.excluir') }}
          </BaseButton>
        </div>
      </template>
    </BaseModal>

    <BaseToast v-if="toast" :message="toast.message" :type="toast.type" @close="toast = null" />
  </div>
</template>
