import axiosClient from './axiosClient'
import type {
  ApiResponse,
  PageResponse,
  FoodItemResponse,
  FoodItemRequest,
  FoodItemQueryParams,
} from '@/types'

export const dealsApi = {
  // ── Public ──────────────────────────────────────────────────────────────
  getAll: (params?: FoodItemQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<FoodItemResponse>>>('/food-items', { params }),

  getById: (id: number) =>
    axiosClient.get<ApiResponse<FoodItemResponse>>(`/food-items/${id}`),

  getByStore: (storeId: number, params?: FoodItemQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<FoodItemResponse>>>(
      `/food-items/store/${storeId}`, { params }
    ),

  getActiveByStore: (storeId: number) =>
    axiosClient.get<ApiResponse<FoodItemResponse[]>>(`/food-items/store/${storeId}/active`),

  // ── Store Owner / Admin ──────────────────────────────────────────────────
  create: (storeId: number, data: FoodItemRequest) =>
    axiosClient.post<ApiResponse<FoodItemResponse>>(`/food-items/store/${storeId}`, data),

  update: (id: number, data: FoodItemRequest) =>
    axiosClient.put<ApiResponse<FoodItemResponse>>(`/food-items/${id}`, data),

  delete: (id: number) =>
    axiosClient.delete<ApiResponse<void>>(`/food-items/${id}`),

  activate: (id: number) =>
    axiosClient.patch<ApiResponse<FoodItemResponse>>(`/food-items/${id}/activate`),

  deactivate: (id: number) =>
    axiosClient.patch<ApiResponse<FoodItemResponse>>(`/food-items/${id}/deactivate`),
}
