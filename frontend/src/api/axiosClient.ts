import axios from 'axios'
import { useAuthStore } from '@/store/authStore'

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  withCredentials: true, // sends the httpOnly refresh-token cookie automatically
  headers: { 'Content-Type': 'application/json' },
})

// ── Request: attach access token ─────────────────────────────────────────────
axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ── Response: auto-logout on 401 ─────────────────────────────────────────────
axiosClient.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      useAuthStore.getState().logout()
    }
    return Promise.reject(err)
  },
)

export default axiosClient
