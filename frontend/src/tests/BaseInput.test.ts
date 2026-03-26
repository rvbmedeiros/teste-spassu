import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseInput from '../components/base/BaseInput.vue'

describe('BaseInput', () => {
  it('renderiza label e obrigatorio', () => {
    const wrapper = mount(BaseInput, {
      props: { label: 'Titulo', required: true },
    })

    expect(wrapper.text()).toContain('Titulo')
    expect(wrapper.text()).toContain('*')
    expect(wrapper.find('input').attributes('required')).toBeDefined()
  })

  it('emite update:modelValue como string por padrao', async () => {
    const wrapper = mount(BaseInput)

    await wrapper.find('input').setValue('Clean Code')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['Clean Code'])
  })

  it('emite update:modelValue como number quando modelModifiers.number=true', async () => {
    const wrapper = mount(BaseInput, {
      props: { modelModifiers: { number: true } },
    })

    await wrapper.find('input').setValue('123')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([123])
  })

  it('emite valor com trim quando modelModifiers.trim=true', async () => {
    const wrapper = mount(BaseInput, {
      props: { modelModifiers: { trim: true } },
    })

    await wrapper.find('input').setValue('  teste  ')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['teste'])
  })

  it('mostra mensagem de erro quando error for informado', () => {
    const wrapper = mount(BaseInput, {
      props: { error: 'Campo obrigatório' },
    })

    expect(wrapper.text()).toContain('Campo obrigatório')
  })
})
