import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import App from './App.vue'
import { router } from './router'
import { useAuthStore } from './stores/auth'
import '@/assets/main.css'

import ptBR from './i18n/locales/pt-BR.json'
import en from './i18n/locales/en.json'

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'pt-BR',
  fallbackLocale: 'en',
  messages: { 'pt-BR': ptBR, en },
})

const pinia = createPinia()
const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(i18n)

// Initialize Keycloak auth before mounting
const authStore = useAuthStore()
authStore.init().then(() => {
  app.mount('#app')
})
