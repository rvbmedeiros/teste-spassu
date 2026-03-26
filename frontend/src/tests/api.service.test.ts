import { AxiosError } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const keycloakMock = vi.hoisted(() => ({
  authenticated: false,
  token: 'token',
  updateToken: vi.fn().mockResolvedValue(true),
  login: vi.fn().mockResolvedValue(undefined),
}))

vi.mock('@/services/keycloak', () => ({
  default: keycloakMock,
}))

import api from '@/services/api'

function createAxiosError(status: number): AxiosError {
  return new AxiosError(`Request failed with status code ${status}`, 'ERR_BAD_RESPONSE', undefined, undefined, {
    data: {},
    status,
    statusText: 'Error',
    headers: {},
    config: { headers: {} as never },
  })
}

describe('api response interceptor', () => {
  beforeEach(() => {
    keycloakMock.login.mockClear()
    window.history.replaceState({}, '', '/livros')
  })

  it('nao deve redirecionar para login em 403', async () => {
    const rejectedHandler = api.interceptors.response.handlers[0]?.rejected

    await expect(rejectedHandler?.(createAxiosError(403))).rejects.toBeInstanceOf(AxiosError)

    expect(keycloakMock.login).not.toHaveBeenCalled()
  })

  it('deve disparar apenas um login para 401 concorrentes', async () => {
    const rejectedHandler = api.interceptors.response.handlers[0]?.rejected
    const unauthorizedError = createAxiosError(401)

    await Promise.allSettled([
      rejectedHandler?.(unauthorizedError),
      rejectedHandler?.(unauthorizedError),
    ])

    expect(keycloakMock.login).toHaveBeenCalledTimes(1)
    expect(keycloakMock.login).toHaveBeenCalledWith({ redirectUri: `${window.location.origin}/livros` })
  })

  it('nao deve redirecionar para login durante callback OIDC', async () => {
    window.history.replaceState({}, '', '/livros#code=abc&state=xyz')

    const rejectedHandler = api.interceptors.response.handlers[0]?.rejected
    const unauthorizedError = createAxiosError(401)

    await expect(rejectedHandler?.(unauthorizedError)).rejects.toBeInstanceOf(AxiosError)

    expect(keycloakMock.login).not.toHaveBeenCalled()
  })
})