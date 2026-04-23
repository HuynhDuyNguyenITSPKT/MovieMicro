import apiClient from './apiClient'

export const loginWithPassword = async (payload) => {
  const response = await apiClient.post('/api/auth/login', payload)
  return response.data
}

export const loginWithGoogle = async (idToken) => {
  const response = await apiClient.post('/api/auth/google-login', { idToken })
  return response.data
}

export const requestRegisterOtp = async (payload) => {
  const response = await apiClient.post('/api/auth/register/request-otp', payload)
  return response.data
}

export const verifyRegisterOtp = async (payload) => {
  const response = await apiClient.post('/api/auth/register/verify-otp', payload)
  return response.data
}
