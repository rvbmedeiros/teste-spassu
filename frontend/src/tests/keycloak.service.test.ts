import { beforeEach, describe, expect, it, vi } from 'vitest'

const KeycloakMock = vi.fn().mockImplementation(function (config) {
  this.config = config
})

vi.mock('keycloak-js', () => ({
  default: KeycloakMock,
}))

describe('keycloak service', () => {
  beforeEach(() => {
    vi.resetModules()
    KeycloakMock.mockClear()
  })

  it('cria a instancia com as chaves esperadas', async () => {
    const module = await import('@/services/keycloak')
    const config = KeycloakMock.mock.calls[0][0] as Record<string, unknown>

    expect(KeycloakMock).toHaveBeenCalledTimes(1)
    expect(config).toHaveProperty('url')
    expect(config).toHaveProperty('realm')
    expect(config).toHaveProperty('clientId')
    expect(module.default).toBeInstanceOf(KeycloakMock)
  })
})