import { beforeEach, afterEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUiStore } from '../stores/ui'

describe('useUiStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    document.documentElement.classList.remove('dark')
  })

  afterEach(() => {
    document.documentElement.classList.remove('dark')
    localStorage.clear()
  })

  it('inicia com tema light quando localStorage esta vazio', () => {
    const store = useUiStore()

    expect(store.theme).toBe('light')
  })

  it('toggleTheme alterna para dark, persiste e aplica classe no html', () => {
    const store = useUiStore()

    store.toggleTheme()

    expect(store.theme).toBe('dark')
    expect(localStorage.getItem('theme')).toBe('dark')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })

  it('toggleTheme alterna de dark para light removendo classe dark', () => {
    localStorage.setItem('theme', 'dark')
    const store = useUiStore()
    store.applyStoredTheme()

    store.toggleTheme()

    expect(store.theme).toBe('light')
    expect(localStorage.getItem('theme')).toBe('light')
    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })

  it('applyStoredTheme aplica tema salvo no html', () => {
    localStorage.setItem('theme', 'dark')
    const store = useUiStore()

    store.applyStoredTheme()

    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })
})
