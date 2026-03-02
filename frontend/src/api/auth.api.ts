import axiosClient from './axiosClient'
import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from '@/types'

export const authApi = {
  register: (data: RegisterRequest) =>
    axiosClient.post<ApiResponse<AuthResponse>>('/auth/register', data),

  login: (data: LoginRequest) =>
    axiosClient.post<ApiResponse<AuthResponse>>('/auth/login', data),

  /** Refresh token is sent automatically via httpOnly cookie */
  refreshToken: () =>
    axiosClient.post<ApiResponse<AuthResponse>>('/auth/refresh-token'),

  logout: (refreshToken?: string) =>
    axiosClient.post<ApiResponse<void>>('/auth/logout', { refreshToken }),
}
