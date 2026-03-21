<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  AlertTriangle,
  BadgeDollarSign,
  BookOpenText,
  CalendarRange,
  Library,
  ListFilter,
  PencilLine,
  Plus,
  RotateCcw,
  Tags,
  Trash2,
  UserRound,
} from 'lucide-vue-next'
import { useLivroStore, type LivroRequest } from '@/stores/livros'
import { useAutorStore } from '@/stores/autores'
import { useAssuntoStore } from '@/stores/assuntos'
import { useAuthStore } from '@/stores/auth'
import { extractApiErrorMessage } from '@/services/errors'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseModal from '@/components/base/BaseModal.vue'
import BaseSelectionDrawer from '@/components/base/BaseSelectionDrawer.vue'
import BaseToast from '@/components/base/BaseToast.vue'

const { t } = useI18n()
const store = useLivroStore()
const autorStore = useAutorStore()
const assuntoStore = useAssuntoStore()
const auth = useAuthStore()

onMounted(async () => {
  await Promise.all([store.fetchAll(), autorStore.fetchAll(), assuntoStore.fetchAll()])
})

const showModal = ref(false)
const editingId = ref<number | null>(null)
const confirmDeleteId = ref<number | null>(null)
const toast = ref<{ message: string; type: 'success' | 'error' } | null>(null)
const saving = ref(false)

const totalLivros = computed(() => store.livros.length)
const totalAutores = computed(() => autorStore.autores.length)
const totalAssuntos = computed(() => assuntoStore.assuntos.length)

type SelectorType = 'autores' | 'assuntos'

interface SelectorItem {
  id: number
  label: string
}

interface FormState {
  titulo: string
  editora: string
  edicao: number
  anoPublicacao: string
  valor: number
  autoresCodAu: Set<number>
  assuntosCodAs: Set<number>
}

const emptyForm = (): FormState => ({
  titulo: '', editora: '', edicao: 1, anoPublicacao: '', valor: 0,
  autoresCodAu: new Set(), assuntosCodAs: new Set(),
})

const form = ref<FormState>(emptyForm())
const selectorOpen = ref(false)
const selectorType = ref<SelectorType>('autores')

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  showModal.value = true
}

function openEdit(livro: typeof store.livros[0]) {
  editingId.value = livro.codL
  form.value = {
    titulo: livro.titulo,
    editora: livro.editora,
    edicao: livro.edicao,
    anoPublicacao: livro.anoPublicacao,
    valor: livro.valor,
    autoresCodAu: new Set(livro.autores.map(autor => autor.codAu)),
    assuntosCodAs: new Set(livro.assuntos.map(assunto => assunto.codAs)),
  }
  showModal.value = true
}

async function save() {
  saving.value = true
  try {
    // Convert Set back to Array for API request
    const request: LivroRequest = {
      titulo: form.value.titulo,
      editora: form.value.editora,
      edicao: form.value.edicao,
      anoPublicacao: form.value.anoPublicacao,
      valor: form.value.valor,
      autoresCodAu: Array.from(form.value.autoresCodAu),
      assuntosCodAs: Array.from(form.value.assuntosCodAs),
    }
    if (editingId.value) {
      await store.atualizar(editingId.value, request)
    } else {
      await store.criar(request)
    }
    showModal.value = false
    toast.value = { message: t('livros.salvoSucesso'), type: 'success' }
  } catch (error) {
    toast.value = { message: extractApiErrorMessage(error, t), type: 'error' }
  } finally {
    saving.value = false
  }
}

async function confirmDelete(codL: number) {
  try {
    await store.excluir(codL)
    confirmDeleteId.value = null
    toast.value = { message: t('livros.excluidoSucesso'), type: 'success' }
  } catch (error) {
    toast.value = { message: extractApiErrorMessage(error, t), type: 'error' }
  }
}

function openSelector(type: SelectorType) {
  selectorType.value = type
  selectorOpen.value = true
}

function applySelector(ids: number[]) {
  if (selectorType.value === 'autores') {
    form.value.autoresCodAu = new Set(ids)
  } else {
    form.value.assuntosCodAs = new Set(ids)
  }

  selectorOpen.value = false
}

const autorOptions = computed<SelectorItem[]>(() =>
  autorStore.autores.map(autor => ({ id: autor.codAu, label: autor.nome })),
)

const assuntoOptions = computed<SelectorItem[]>(() =>
  assuntoStore.assuntos.map(assunto => ({ id: assunto.codAs, label: assunto.descricao })),
)

const selectedAutoresPreview = computed(() =>
  autorStore.autores.filter(autor => form.value.autoresCodAu.has(autor.codAu)),
)

const selectedAssuntosPreview = computed(() =>
  assuntoStore.assuntos.filter(assunto => form.value.assuntosCodAs.has(assunto.codAs)),
)

const selectorTitle = computed(() =>
  selectorType.value === 'autores' ? t('livros.selecionarAutores') : t('livros.selecionarAssuntos'),
)

const selectorDescription = computed(() =>
  selectorType.value === 'autores' ? t('livros.subtitleSelecionarAutores') : t('livros.subtitleSelecionarAssuntos'),
)

const selectorSearchPlaceholder = computed(() =>
  selectorType.value === 'autores' ? t('livros.buscarAutores') : t('livros.buscarAssuntos'),
)

const activeSelectorOptions = computed(() =>
  selectorType.value === 'autores' ? autorOptions.value : assuntoOptions.value,
)

const activeSelectorIds = computed(() =>
  selectorType.value === 'autores'
    ? Array.from(form.value.autoresCodAu)
    : Array.from(form.value.assuntosCodAs),
)

function hiddenCount(total: number): number {
  return Math.max(total - 3, 0)
}

function selectedLabel(total: number): string {
  return t('livros.selecionadosCount', { count: total })
}
</script>

<template>
  <div class="page-shell">
    <section class="surface-panel-strong rounded-4xl p-6 sm:p-8">
      <div class="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
        <div class="space-y-4">
          <span class="section-eyebrow">
            <BookOpenText class="h-3.5 w-3.5" />
            {{ t('nav.livros') }}
          </span>
          <div class="space-y-2">
            <h1 class="page-title">{{ t('livros.title') }}</h1>
            <p class="page-subtitle">{{ t('livros.subtitle') }}</p>
          </div>
        </div>

        <BaseButton v-if="auth.isAdmin" size="lg" @click="openCreate">
          <template #leading>
            <Plus />
          </template>
          {{ t('livros.novo') }}
        </BaseButton>
      </div>

      <div class="mt-8 grid gap-4 md:grid-cols-3">
        <div class="metric-tile">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-(--ui-text-muted)">{{ t('livros.title') }}</span>
            <span class="rounded-2xl bg-(--ui-brand-soft) p-2 text-(--ui-brand)">
              <Library class="h-4 w-4" />
            </span>
          </div>
          <p class="mt-4 text-3xl font-semibold tracking-tight text-(--ui-text)">{{ totalLivros }}</p>
        </div>

        <div class="metric-tile">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-(--ui-text-muted)">{{ t('livros.autores') }}</span>
            <span class="rounded-2xl bg-(--ui-brand-soft) p-2 text-(--ui-brand)">
              <UserRound class="h-4 w-4" />
            </span>
          </div>
          <p class="mt-4 text-3xl font-semibold tracking-tight text-(--ui-text)">{{ totalAutores }}</p>
        </div>

        <div class="metric-tile">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-(--ui-text-muted)">{{ t('livros.assuntos') }}</span>
            <span class="rounded-2xl bg-(--ui-brand-soft) p-2 text-(--ui-brand)">
              <Tags class="h-4 w-4" />
            </span>
          </div>
          <p class="mt-4 text-3xl font-semibold tracking-tight text-(--ui-text)">{{ totalAssuntos }}</p>
        </div>
      </div>
    </section>

    <BaseCard :padding="false" :title="t('livros.title')" :subtitle="t('livros.subtitle')">
      <div class="table-shell">
        <div v-if="store.loading" class="flex min-h-72 flex-col items-center justify-center gap-3 px-6 text-center text-(--ui-text-muted)">
          <Library class="h-8 w-8 text-(--ui-brand)" />
          <p class="text-sm font-medium">{{ t('common.loading') }}</p>
        </div>

        <div v-else-if="store.error" class="flex min-h-72 flex-col items-center justify-center gap-4 px-6 text-center">
          <span class="rounded-3xl bg-(--ui-danger-soft) p-4 text-(--ui-danger)">
            <AlertTriangle class="h-6 w-6" />
          </span>
          <div class="space-y-1">
            <p class="text-sm font-semibold text-(--ui-text)">{{ store.error }}</p>
          </div>
          <BaseButton variant="secondary" size="sm" @click="store.fetchAll()">
            <template #leading><RotateCcw /></template>
            {{ t('common.retry') }}
          </BaseButton>
        </div>

        <div v-else-if="!store.livros.length" class="flex min-h-72 flex-col items-center justify-center gap-3 px-6 text-center text-(--ui-text-muted)">
          <BookOpenText class="h-8 w-8 text-(--ui-brand)" />
          <p class="text-sm font-medium">{{ t('livros.semRegistros') }}</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr class="table-head">
                <th class="px-5 py-4">{{ t('livros.titulo') }}</th>
                <th class="px-5 py-4">{{ t('livros.editora') }}</th>
                <th class="px-5 py-4">{{ t('livros.edicao') }}</th>
                <th class="px-5 py-4">{{ t('livros.anoPublicacao') }}</th>
                <th class="px-5 py-4">{{ t('livros.valor') }}</th>
                <th v-if="auth.isAdmin" class="px-5 py-4 text-right">{{ t('common.acoes') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="livro in store.livros" :key="livro.codL" class="table-row">
                <td class="px-5 py-4 align-top">
                  <div class="space-y-2">
                    <p class="font-semibold text-(--ui-text)">{{ livro.titulo }}</p>
                    <div class="flex flex-wrap gap-2">
                      <span class="chip">{{ livro.autores.length }} {{ t('livros.autores') }}</span>
                      <span class="chip">{{ livro.assuntos.length }} {{ t('livros.assuntos') }}</span>
                    </div>
                  </div>
                </td>
                <td class="px-5 py-4 text-(--ui-text-muted)">{{ livro.editora }}</td>
                <td class="px-5 py-4 text-(--ui-text-muted)">{{ livro.edicao }}</td>
                <td class="px-5 py-4 text-(--ui-text-muted)">{{ livro.anoPublicacao }}</td>
                <td class="px-5 py-4 font-semibold text-(--ui-text)">
                  {{ new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(livro.valor) }}
                </td>
                <td v-if="auth.isAdmin" class="px-5 py-4">
                  <div class="flex justify-end gap-2">
                    <BaseButton variant="secondary" size="sm" @click="openEdit(livro)">
                      <template #leading>
                        <PencilLine />
                      </template>
                      {{ t('common.editar') }}
                    </BaseButton>
                    <BaseButton variant="danger" size="sm" @click="confirmDeleteId = livro.codL">
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
      </div>
    </BaseCard>

    <BaseModal
      :open="showModal"
      :title="editingId ? t('livros.editar') : t('livros.novo')"
      :description="t('livros.subtitle')"
      size="lg"
      @close="showModal = false"
    >
      <form class="space-y-5" @submit.prevent="save">
        <div class="grid gap-4 md:grid-cols-2">
          <BaseInput :label="t('livros.titulo')" v-model="form.titulo" required>
            <template #leading>
              <BookOpenText />
            </template>
          </BaseInput>
          <BaseInput :label="t('livros.editora')" v-model="form.editora" required>
            <template #leading>
              <Library />
            </template>
          </BaseInput>
          <BaseInput :label="t('livros.edicao')" v-model.number="form.edicao" type="number" min="1" required>
            <template #leading>
              <BookOpenText />
            </template>
          </BaseInput>
          <BaseInput :label="t('livros.anoPublicacao')" v-model="form.anoPublicacao" placeholder="2024" required>
            <template #leading>
              <CalendarRange />
            </template>
          </BaseInput>
        </div>

        <BaseInput :label="t('livros.valor')" v-model.number="form.valor" type="number" step="0.01" min="0.01" required>
          <template #leading>
            <BadgeDollarSign />
          </template>
        </BaseInput>

        <div class="grid gap-4 md:grid-cols-2">
          <div class="surface-muted space-y-3 rounded-3xl p-4">
            <div class="flex items-center justify-between gap-3">
              <div class="flex items-center gap-2 text-sm font-medium text-(--ui-text)">
                <UserRound class="h-4 w-4 text-(--ui-brand)" />
                {{ t('livros.autores') }}
              </div>
              <BaseButton
                type="button"
                variant="secondary"
                size="sm"
                :aria-expanded="selectorOpen && selectorType === 'autores'"
                @click="openSelector('autores')"
              >
                <template #leading>
                  <ListFilter />
                </template>
                {{ t('livros.selecionarAutores') }}
              </BaseButton>
            </div>

            <p class="text-xs font-semibold uppercase tracking-[0.14em] text-(--ui-text-muted)">
              {{ selectedLabel(selectedAutoresPreview.length) }}
            </p>

            <div v-if="selectedAutoresPreview.length" class="flex flex-wrap gap-2">
              <span v-for="autor in selectedAutoresPreview.slice(0, 3)" :key="autor.codAu" class="chip">
                {{ autor.nome }}
              </span>
              <span v-if="hiddenCount(selectedAutoresPreview.length)" class="chip">
                +{{ hiddenCount(selectedAutoresPreview.length) }}
              </span>
            </div>

            <p v-else class="text-sm text-(--ui-text-muted)">{{ t('livros.nenhumSelecionado') }}</p>
          </div>

          <div class="surface-muted space-y-3 rounded-3xl p-4">
            <div class="flex items-center justify-between gap-3">
              <div class="flex items-center gap-2 text-sm font-medium text-(--ui-text)">
                <Tags class="h-4 w-4 text-(--ui-brand)" />
                {{ t('livros.assuntos') }}
              </div>
              <BaseButton
                type="button"
                variant="secondary"
                size="sm"
                :aria-expanded="selectorOpen && selectorType === 'assuntos'"
                @click="openSelector('assuntos')"
              >
                <template #leading>
                  <ListFilter />
                </template>
                {{ t('livros.selecionarAssuntos') }}
              </BaseButton>
            </div>

            <p class="text-xs font-semibold uppercase tracking-[0.14em] text-(--ui-text-muted)">
              {{ selectedLabel(selectedAssuntosPreview.length) }}
            </p>

            <div v-if="selectedAssuntosPreview.length" class="flex flex-wrap gap-2">
              <span v-for="assunto in selectedAssuntosPreview.slice(0, 3)" :key="assunto.codAs" class="chip">
                {{ assunto.descricao }}
              </span>
              <span v-if="hiddenCount(selectedAssuntosPreview.length)" class="chip">
                +{{ hiddenCount(selectedAssuntosPreview.length) }}
              </span>
            </div>

            <p v-else class="text-sm text-(--ui-text-muted)">{{ t('livros.nenhumSelecionado') }}</p>
          </div>
        </div>
      </form>

      <template #footer>
        <div class="flex justify-end gap-2">
          <BaseButton variant="secondary" @click="showModal = false">{{ t('livros.cancelar') }}</BaseButton>
          <BaseButton :loading="saving" @click="save">{{ t('livros.salvar') }}</BaseButton>
        </div>
      </template>
    </BaseModal>

    <BaseSelectionDrawer
      :open="selectorOpen"
      :title="selectorTitle"
      :description="selectorDescription"
      :items="activeSelectorOptions"
      :selected-ids="activeSelectorIds"
      :search-placeholder="selectorSearchPlaceholder"
      :selected-count-label="t('livros.selecionados')"
      :empty-state-label="t('livros.semResultados')"
      :cancel-label="t('livros.cancelar')"
      :clear-label="t('livros.limparSelecao')"
      :apply-label="t('livros.aplicarSelecao')"
      @close="selectorOpen = false"
      @apply="applySelector"
    />

    <BaseModal
      :open="confirmDeleteId !== null"
      :title="t('livros.confirmarExclusao')"
      :description="t('livros.confirmarExclusaoMsg')"
      size="sm"
      @close="confirmDeleteId = null"
    >
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
