import axiosClient from './axiosClient'
import type {
  ApiResponse,
  PageResponse,
  UserResponse,
  UpdateProfileRequest,
  ChangePasswordRequest,
  UpdateLocationRequest,
} from '@/types'

export const usersApi = {
  getMe: () =>
    axiosClient.get<ApiResponse<UserResponse>>('/users/me'),

  updateProfile: (data: UpdateProfileRequest) =>
    axiosClient.put<ApiResponse<UserResponse>>('/users/me', data),

  changePassword: (data: ChangePasswordRequest) =>
    axiosClient.post<ApiResponse<void>>('/users/me/change-password', data),

  updateLocation: (data: UpdateLocationRequest) =>
    axiosClient.patch<ApiResponse<UserResponse>>('/users/me/location', data),

  // Admin endpoints
  getAll: (params?: { page?: number; size?: number; sort?: string }) =>
    axiosClient.get<ApiResponse<PageResponse<UserResponse>>>('/users', { params }),

  getById: (id: number) =>
    axiosClient.get<ApiResponse<UserResponse>>(`/users/${id}`),
}
