import { mount } from '@vue/test-utils'
import { flushPromises } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import { createMemoryHistory, createRouter } from 'vue-router'
import { reactive } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import TheNavbar from '@/components/layout/TheNavbar.vue'
import ptBR from '@/i18n/locales/pt-BR.json'

const logout = vi.fn()
const authState = reactive({ username: 'admin', logout })

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authState,
}))

function createNavbarWrapper(initialPath = '/livros') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/livros', component: { template: '<div />' } },
      { path: '/autores', component: { template: '<div />' } },
      { path: '/assuntos', component: { template: '<div />' } },
      { path: '/relatorio', component: { template: '<div />' } },
      { path: '/flowcockpit', component: { template: '<div />' } },
      { path: '/logs', component: { template: '<div />' } },
    ],
  })
  const i18n = createI18n({
    legacy: false,
    locale: 'pt-BR',
    fallbackLocale: 'en',
    messages: {
      'pt-BR': ptBR,
      en: ptBR,
    },
  })

  return router.push(initialPath).then(async () => {
    await router.isReady()
    return {
      router,
      wrapper: mount(TheNavbar, {
        global: {
          plugins: [router, i18n],
          stubs: {
            BaseButton: {
              emits: ['click'],
              template: '<button @click="$emit(\'click\')"><slot /><slot name="leading" /></button>',
            },
            BaseSwitcher: true,
            Transition: true,
          },
        },
      }),
    }
  })
}

describe('TheNavbar', () => {
  beforeEach(() => {
    authState.username = 'admin'
    logout.mockReset()
    localStorage.clear()
  })

  it('renderiza usuario autenticado e alterna locale', async () => {
    const { wrapper } = await createNavbarWrapper()

    expect(wrapper.text()).toContain('admin')

    const localeButton = wrapper.findAll('button').find(button => button.text().includes('EN'))
    expect(localeButton).toBeDefined()

    await localeButton!.trigger('click')

    expect(localStorage.getItem('locale')).toBe('en')
  })

  it('fecha o menu mobile ao trocar de rota', async () => {
    const { wrapper, router } = await createNavbarWrapper()

    const toggleButton = wrapper.findAll('button').at(-1)
    expect(toggleButton).toBeDefined()

    await toggleButton!.trigger('click')
    expect(wrapper.findAll('a[href="/livros"]')).toHaveLength(2)

    await router.push('/autores')
    await flushPromises()
    await flushPromises()

    expect(wrapper.findAll('a[href="/livros"]')).toHaveLength(1)
  })

  it('executa logout ao clicar na acao desktop', async () => {
    const { wrapper } = await createNavbarWrapper()

    const logoutButton = wrapper.findAll('button').find(button => button.text().includes('Sair'))
    expect(logoutButton).toBeDefined()

    await logoutButton!.trigger('click')

    expect(logout).toHaveBeenCalledOnce()
  })
})