import { beforeEach, afterEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import BaseSwitcher from '../components/base/BaseSwitcher.vue'
import { useUiStore } from '../stores/ui'

describe('BaseSwitcher', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    document.documentElement.classList.remove('dark')
  })

  afterEach(() => {
    document.documentElement.classList.remove('dark')
    localStorage.clear()
  })

  it('renderiza com label de alternancia para dark mode quando tema atual e light', () => {
    const wrapper = mount(BaseSwitcher)

    expect(wrapper.attributes('aria-label')).toBe('Switch to dark mode')
  })

  it('ao clicar alterna tema e atualiza aria-label', async () => {
    const wrapper = mount(BaseSwitcher)

    await wrapper.trigger('click')

    expect(wrapper.attributes('aria-label')).toBe('Switch to light mode')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })

  it('executa applyStoredTheme ao montar', () => {
    localStorage.setItem('theme', 'dark')
    const store = useUiStore()

    mount(BaseSwitcher)

    expect(store.theme).toBe('dark')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })
})
