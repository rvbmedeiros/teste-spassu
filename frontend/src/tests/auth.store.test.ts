import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const keycloakMock = vi.hoisted(() => ({
  init: vi.fn(),
  updateToken: vi.fn(),
  logout: vi.fn(),
  login: vi.fn(),
  authenticated: true,
  token: 'jwt-token',
  tokenParsed: {
    preferred_username: 'admin',
    realm_access: { roles: ['admin'] },
    resource_access: { frontend: { roles: ['ROLE_USER'] } },
  },
}))

vi.mock('@/services/keycloak', () => ({
  default: keycloakMock,
}))

import { useAuthStore } from '../stores/auth'

describe('useAuthStore', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    setActivePinia(createPinia())
    vi.clearAllMocks()
    keycloakMock.authenticated = true
    keycloakMock.token = 'jwt-token'
    keycloakMock.tokenParsed = {
      preferred_username: 'admin',
      realm_access: { roles: ['admin'] },
      resource_access: { frontend: { roles: ['ROLE_USER'] } },
    }
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })

  it('init sincroniza dados e calcula isAdmin', async () => {
    keycloakMock.init.mockResolvedValue(true)
    keycloakMock.updateToken.mockResolvedValue(true)

    const store = useAuthStore()
    await store.init()

    expect(store.authenticated).toBe(true)
    expect(store.username).toBe('admin')
    expect(store.token).toBe('jwt-token')
    expect(store.roles).toEqual(['admin', 'ROLE_USER'])
    expect(store.isAdmin).toBe(true)
  })

  it('logout delega para keycloak com redirectUri da origem atual', () => {
    const store = useAuthStore()

    store.logout()

    expect(keycloakMock.logout).toHaveBeenCalledWith({ redirectUri: window.location.origin })
  })

  it('init chama login quando init do keycloak falha', async () => {
    keycloakMock.init.mockRejectedValue(new Error('init failed'))
    keycloakMock.login.mockResolvedValue(undefined)

    const store = useAuthStore()
    await store.init()

    expect(keycloakMock.login).toHaveBeenCalledTimes(1)
    expect(keycloakMock.login).toHaveBeenCalledWith({ redirectUri: `${window.location.origin}/` })
  })

  it('quando updateToken falha no intervalo, executa logout e login', async () => {
    keycloakMock.init.mockResolvedValue(true)
    keycloakMock.updateToken.mockRejectedValue(new Error('refresh failed'))

    const store = useAuthStore()
    await store.init()

    await vi.advanceTimersByTimeAsync(60_000)

    expect(keycloakMock.updateToken).toHaveBeenCalledWith(30)
    expect(keycloakMock.logout).toHaveBeenCalledTimes(1)
    expect(keycloakMock.login).toHaveBeenCalledTimes(1)
    expect(keycloakMock.login).toHaveBeenCalledWith({ redirectUri: `${window.location.origin}/` })
  })
})
