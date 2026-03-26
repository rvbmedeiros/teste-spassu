import axios from 'axios'
import keycloak from './keycloak'

let loginRedirectPromise: Promise<void> | null = null

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL as string || '',
  headers: { 'Content-Type': 'application/json' },
})

function buildSafeRedirectUri(): string {
  // Never send OIDC callback hash/query back as redirect_uri to avoid recursive growth.
  return `${window.location.origin}${window.location.pathname}`
}

function isOidcCallbackInProgress(): boolean {
  const hash = window.location.hash || ''
  return /(?:^|[?#&])(code|state|session_state|iss)=/.test(hash)
}

async function triggerLoginRedirect(): Promise<void> {
  if (!loginRedirectPromise) {
    loginRedirectPromise = keycloak.login({ redirectUri: buildSafeRedirectUri() })
      .catch((error: unknown) => {
        console.error('Keycloak re-login failed', error)
        throw error
      })
      .finally(() => {
        loginRedirectPromise = null
      })
  }

  return loginRedirectPromise
}

// Attach Bearer token to every request
api.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    await keycloak.updateToken(30)
    config.headers.Authorization = `Bearer ${keycloak.token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error.response?.status
    if (status === 401) {
      if (isOidcCallbackInProgress()) {
        console.warn('401 during OIDC callback; skipping forced re-login to avoid redirect loop')
        return Promise.reject(error)
      }
      console.warn('API responded with unauthorized, redirecting to login', status)
      await triggerLoginRedirect()
    }
    return Promise.reject(error)
  }
)

export default api
