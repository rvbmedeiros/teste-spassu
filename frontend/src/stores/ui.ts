import { defineStore } from 'pinia'
import { ref } from 'vue'

type Theme = 'light' | 'dark'

export const useUiStore = defineStore('ui', () => {
  const theme = ref<Theme>((localStorage.getItem('theme') as Theme) ?? 'light')

  function toggleTheme(): void {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    localStorage.setItem('theme', theme.value)
    document.documentElement.classList.toggle('dark', theme.value === 'dark')
  }

  function applyStoredTheme(): void {
    document.documentElement.classList.toggle('dark', theme.value === 'dark')
  }

  return { theme, toggleTheme, applyStoredTheme }
})
