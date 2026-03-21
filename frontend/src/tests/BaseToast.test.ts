import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseToast from '../components/base/BaseToast.vue'

describe('BaseToast', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })

  it('renderiza mensagem recebida', () => {
    const wrapper = mount(BaseToast, {
      props: { message: 'Operação realizada com sucesso' },
      global: { stubs: { Transition: false } },
    })

    expect(wrapper.text()).toContain('Operação realizada com sucesso')
  })

  it('emite close automaticamente após duration', async () => {
    const wrapper = mount(BaseToast, {
      props: { message: 'Auto close', duration: 1000 },
      global: { stubs: { Transition: false } },
    })

    await vi.advanceTimersByTimeAsync(1000)

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('emite close ao clicar no botao de fechar', async () => {
    const wrapper = mount(BaseToast, {
      props: { message: 'Manual close', duration: 0 },
      global: { stubs: { Transition: false } },
    })

    await wrapper.find('button').trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })
})
