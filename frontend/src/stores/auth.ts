import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import keycloak from '@/services/keycloak'

type ParsedToken = {
  preferred_username?: string
  realm_access?: {
    roles?: string[]
  }
  resource_access?: Record<string, {
    roles?: string[]
  }>
}

export const useAuthStore = defineStore('auth', () => {
  const authenticated = ref(false)
  const username = ref('')
  const roles = ref<string[]>([])
  const token = ref('')

  const normalizedRoles = computed(() => roles.value.map(normalizeRole))
  const isAdmin = computed(() => normalizedRoles.value.includes('ADMIN'))

  async function init(): Promise<void> {
    const redirectUri = `${window.location.origin}${window.location.pathname}`

    try {
      const result = await keycloak.init({
        onLoad: 'login-required',
        checkLoginIframe: false,
        pkceMethod: 'S256',
      })
      if (result) {
        _syncFromKeycloak()
      }

      // Refresh token before it expires
      setInterval(async () => {
        try {
          await keycloak.updateToken(30)
          _syncFromKeycloak()
        } catch (error) {
          console.error('Keycloak token refresh failed', error)
          keycloak.logout()
          keycloak.login({ redirectUri })
        }
      }, 60_000)
    } catch (err) {
      console.error('Keycloak init failed - forcing login', err)
      try {
        await keycloak.login({ redirectUri })
      } catch (loginErr) {
        console.error('Keycloak login fallback failed', loginErr)
      }
    }
  }

  function logout(): void {
    keycloak.logout({ redirectUri: window.location.origin })
  }

  function _syncFromKeycloak(): void {
    const tokenParsed = (keycloak.tokenParsed ?? {}) as ParsedToken

    authenticated.value = !!keycloak.authenticated
    username.value = tokenParsed.preferred_username ?? ''
    roles.value = extractRoles(tokenParsed)
    token.value = keycloak.token ?? ''
  }

  return { authenticated, username, roles, token, isAdmin, init, logout }
})

function extractRoles(tokenParsed: ParsedToken): string[] {
  const realmRoles = tokenParsed.realm_access?.roles ?? []
  const resourceRoles = Object.values(tokenParsed.resource_access ?? {})
    .flatMap(resource => resource.roles ?? [])

  return Array.from(new Set([...realmRoles, ...resourceRoles]))
}

function normalizeRole(role: string): string {
  return role.replace(/^ROLE_/, '').toUpperCase()
}
