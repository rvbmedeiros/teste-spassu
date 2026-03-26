import axios from 'axios'

type TranslateFn = (key: string) => string

const errorCodeMap: Record<string, string> = {
  BFF_0001: 'common.errorCodes.BFF_0001',
  BFF_0002: 'common.errorCodes.BFF_0002',
  BFF_0800: 'common.errorCodes.BFF_0800',
  BFF_0804: 'common.errorCodes.BFF_0804',
  BFF_1002: 'common.errorCodes.BFF_1002',
  BFF_1003: 'common.errorCodes.BFF_1003',
  BFF_9000: 'common.errorCodes.BFF_9000',
}

export function extractApiErrorMessage(error: unknown, translate?: TranslateFn): string {
  const fallback = translate ? translate('common.erro') : 'An error occurred. Please try again.'

  if (!axios.isAxiosError(error)) {
    return error instanceof Error ? error.message : fallback
  }

  const payload = error.response?.data as {
    code?: string
    detail?: string
    error?: string
  } | undefined

  const messageKey = payload?.code ? errorCodeMap[payload.code] : undefined
  if (messageKey && translate) {
    return translate(messageKey)
  }

  if (typeof payload?.detail === 'string' && payload.detail.trim()) {
    return payload.detail
  }

  if (typeof payload?.error === 'string' && payload.error.trim()) {
    return payload.error
  }

  return error.message || fallback
}