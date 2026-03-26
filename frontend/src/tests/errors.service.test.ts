import { describe, it, expect } from 'vitest'
import { AxiosError } from 'axios'
import { extractApiErrorMessage } from '@/services/errors'

function createAxiosError(payload: unknown, message = 'Request failed'): AxiosError {
  return new AxiosError(message, 'ERR_BAD_RESPONSE', undefined, undefined, {
    data: payload,
    status: 500,
    statusText: 'Internal Server Error',
    headers: {},
    config: { headers: {} as never },
  })
}

describe('extractApiErrorMessage', () => {
  it('deve mapear codigo BFF usando i18n', () => {
    const error = createAxiosError({ code: 'BFF_1003' })
    const t = (key: string) => `translated:${key}`

    expect(extractApiErrorMessage(error, t)).toBe('translated:common.errorCodes.BFF_1003')
  })

  it('deve usar detail quando nao houver traducao por codigo', () => {
    const error = createAxiosError({ detail: 'Detalhe do backend' })

    expect(extractApiErrorMessage(error)).toBe('Detalhe do backend')
  })

  it('deve usar error quando detail nao estiver presente', () => {
    const error = createAxiosError({ error: 'Erro sintetico' })

    expect(extractApiErrorMessage(error)).toBe('Erro sintetico')
  })

  it('deve usar fallback traduzido para erro nao Axios', () => {
    const t = (key: string) => (key === 'common.erro' ? 'Ocorreu erro traduzido' : key)

    expect(extractApiErrorMessage('falha', t)).toBe('Ocorreu erro traduzido')
  })

  it('deve usar message do Error para erro nao Axios', () => {
    expect(extractApiErrorMessage(new Error('Network error'))).toBe('Network error')
  })

  it('deve usar message do axios quando payload estiver vazio', () => {
    const error = createAxiosError(undefined, 'Bad gateway')

    expect(extractApiErrorMessage(error)).toBe('Bad gateway')
  })
})
