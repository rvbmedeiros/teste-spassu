import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import BaseButton from '@/components/base/BaseButton.vue'
import { createI18n } from 'vue-i18n'
import ptBR from '../i18n/locales/pt-BR.json'

const i18n = createI18n({ legacy: false, locale: 'pt-BR', messages: { 'pt-BR': ptBR } })

const globalPlugins = { plugins: [i18n] }

describe('BaseButton', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('renderiza o slot corretamente', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      slots: { default: 'Salvar' },
    })
    expect(wrapper.text()).toContain('Salvar')
  })

  it('está disabled quando a prop disabled é true', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      props: { disabled: true },
      slots: { default: 'Btn' },
    })
    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('mostra spinner quando loading é true', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      props: { loading: true },
      slots: { default: 'Aguarde' },
    })
    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('está disabled durante loading', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      props: { loading: true },
      slots: { default: 'Aguarde' },
    })
    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('emite click normal', async () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      slots: { default: 'Click' },
    })
    await wrapper.trigger('click')
    expect(wrapper.emitted()).toBeTruthy()
  })

  it('aplica w-full quando block é true', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      props: { block: true },
      slots: { default: 'Block' },
    })
    expect(wrapper.classes()).toContain('w-full')
  })

  it('não aplica w-full quando block é false (padrão)', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      slots: { default: 'Inline' },
    })
    expect(wrapper.classes()).not.toContain('w-full')
  })

  it('renderiza slot leading quando fornecido', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      slots: { default: 'Salvar', leading: '<svg data-testid="icon" />' },
    })
    expect(wrapper.find('[data-testid="icon"]').exists()).toBe(true)
  })

  it('não renderiza slot leading durante loading', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      props: { loading: true },
      slots: { default: 'Aguarde', leading: '<svg data-testid="icon" />' },
    })
    // spinner substitui o ícone leading durante loading
    expect(wrapper.find('[data-testid="icon"]').exists()).toBe(false)
    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('renderiza slot trailing quando fornecido e não está loading', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      slots: { default: 'Avançar', trailing: '<svg data-testid="trailing-icon" />' },
    })
    expect(wrapper.find('[data-testid="trailing-icon"]').exists()).toBe(true)
  })

  it('não renderiza slot trailing durante loading', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      props: { loading: true },
      slots: { default: 'Aguarde', trailing: '<svg data-testid="trailing-icon" />' },
    })
    expect(wrapper.find('[data-testid="trailing-icon"]').exists()).toBe(false)
  })

  it('aplica classe de variante primary por padrão', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      slots: { default: 'Primário' },
    })
    // primary tem border-transparent
    expect(wrapper.classes().some(c => c.includes('border-transparent'))).toBe(true)
  })

  it('aplica tipo button por padrão', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      slots: { default: 'Submit' },
    })
    expect(wrapper.attributes('type')).toBe('button')
  })

  it('aplica type submit quando a prop é submit', () => {
    const wrapper = mount(BaseButton, {
      global: globalPlugins,
      props: { type: 'submit' },
      slots: { default: 'Submit' },
    })
    expect(wrapper.attributes('type')).toBe('submit')
  })
})
