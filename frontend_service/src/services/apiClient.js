import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const ACCESS_TOKEN_KEY = 'movieticket_access_token'
const REFRESH_TOKEN_KEY = 'movieticket_refresh_token'
export const AUTH_LOGOUT_EVENT = 'movieticket:auth-logout'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

const isBrowser = typeof window !== 'undefined'

export const getAccessToken = () => {
  if (!isBrowser) {
    return ''
  }

  return window.localStorage.getItem(ACCESS_TOKEN_KEY) || ''
}

export const getRefreshToken = () => {
  if (!isBrowser) {
    return ''
  }

  return window.localStorage.getItem(REFRESH_TOKEN_KEY) || ''
}

export const saveAuthTokens = (authPayload) => {
  if (!isBrowser || !authPayload) {
    return
  }

  if (authPayload.accessToken) {
    window.localStorage.setItem(ACCESS_TOKEN_KEY, authPayload.accessToken)
  }

  if (authPayload.refreshToken) {
    window.localStorage.setItem(REFRESH_TOKEN_KEY, authPayload.refreshToken)
  }
}

export const clearAuthTokens = () => {
  if (!isBrowser) {
    return
  }

  window.localStorage.removeItem(ACCESS_TOKEN_KEY)
  window.localStorage.removeItem(REFRESH_TOKEN_KEY)
}

apiClient.interceptors.request.use((config) => {
  const accessToken = getAccessToken()

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }

  return config
})

let refreshPromise = null

const refreshAccessToken = async () => {
  const refreshToken = getRefreshToken()

  if (!refreshToken) {
    throw new Error('No refresh token found')
  }

  const response = await axios.post(
    `${API_BASE_URL}/api/auth/refresh`,
    { refreshToken },
    {
      headers: {
        'Content-Type': 'application/json',
      },
    },
  )

  saveAuthTokens(response.data)
  return response.data.accessToken || ''
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error?.config
    const status = error?.response?.status

    if (!originalRequest || status !== 401) {
      return Promise.reject(error)
    }

    if (originalRequest._retry || originalRequest.url?.includes('/api/auth/refresh')) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    try {
      if (!refreshPromise) {
        refreshPromise = refreshAccessToken().finally(() => {
          refreshPromise = null
        })
      }

      const newAccessToken = await refreshPromise
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
      return apiClient(originalRequest)
    } catch (refreshError) {
      clearAuthTokens()

      if (isBrowser) {
        window.dispatchEvent(new Event(AUTH_LOGOUT_EVENT))
      }

      return Promise.reject(refreshError)
    }
  },
)

export default apiClient
