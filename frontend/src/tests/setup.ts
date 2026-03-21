import { config } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import ptBR from '../i18n/locales/pt-BR.json'

const i18n = createI18n({
  legacy: false,
  locale: 'pt-BR',
  messages: { 'pt-BR': ptBR },
})

config.global.plugins = [i18n]
