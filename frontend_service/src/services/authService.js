import apiClient, { clearAuthTokens, saveAuthTokens } from './apiClient'

export const loginWithPassword = async (payload) => {
  const response = await apiClient.post('/api/auth/login', payload)
  saveAuthTokens(response.data)
  return response.data
}

export const loginWithGoogle = async (idToken) => {
  const response = await apiClient.post('/api/auth/google-login', { idToken })
  saveAuthTokens(response.data)
  return response.data
}

export const requestRegisterOtp = async (payload) => {
  const response = await apiClient.post('/api/auth/register/request-otp', payload)
  return response.data
}

export const verifyRegisterOtp = async (payload) => {
  const response = await apiClient.post('/api/auth/register/verify-otp', payload)
  saveAuthTokens(response.data)
  return response.data
}

export const refreshAuth = async (refreshToken) => {
  const response = await apiClient.post('/api/auth/refresh', { refreshToken })
  saveAuthTokens(response.data)
  return response.data
}

export const logout = () => {
  clearAuthTokens()
}
