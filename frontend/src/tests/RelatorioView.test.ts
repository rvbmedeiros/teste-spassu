import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import RelatorioView from '../views/RelatorioView.vue'

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
  },
}))

vi.mock('../services/errors', () => ({
  extractApiErrorMessage: vi.fn(() => 'Erro ao gerar relatorio'),
}))

import api from '../services/api'

describe('RelatorioView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      writable: true,
      value: vi.fn(() => 'blob:relatorio'),
    })
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      writable: true,
      value: vi.fn(),
    })
  })

  it('inicia download do pdf quando a API responde com sucesso', async () => {
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
    vi.mocked(api.get).mockResolvedValue({ data: new Blob(['pdf']) })

    const wrapper = mount(RelatorioView)
    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(api.get).toHaveBeenCalledWith('/api/relatorio/pdf', { responseType: 'blob' })
    expect(URL.createObjectURL).toHaveBeenCalled()
    expect(clickSpy).toHaveBeenCalled()
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:relatorio')

    clickSpy.mockRestore()
  })

  it('exibe toast de erro quando a API falha', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('boom'))

    const wrapper = mount(RelatorioView)
    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Erro ao gerar relatorio')
  })
})