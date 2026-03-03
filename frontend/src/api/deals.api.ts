import axiosClient from './axiosClient'
import type {
  ApiResponse,
  PageResponse,
  FoodItemResponse,
  FoodItemRequest,
  FoodItemQueryParams,
  FoodItemStatus,
} from '@/types'

export const dealsApi = {
  // -- Public ------------------------------------------------------------------
  getAll: (params?: FoodItemQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<FoodItemResponse>>>('/food-items', { params }),

  getById: (id: number) =>
    axiosClient.get<ApiResponse<FoodItemResponse>>(`/food-items/${id}`),

  getByStore: (storeId: number, params?: FoodItemQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<FoodItemResponse>>>(
      `/food-items/store/${storeId}`, { params }
    ),

  getByCategory: (categoryId: number, params?: FoodItemQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<FoodItemResponse>>>(
      `/food-items/category/${categoryId}`, { params }
    ),

  getAvailable: (params?: FoodItemQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<FoodItemResponse>>>('/food-items/available', { params }),

  getFlashSale: (params?: FoodItemQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<FoodItemResponse>>>('/food-items/flash-sale', { params }),

  search: (keyword: string, params?: FoodItemQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<FoodItemResponse>>>('/food-items/search', {
      params: { keyword, ...params },
    }),

  // -- Store Owner / Admin -----------------------------------------------------
  create: (storeId: number, data: FoodItemRequest) =>
    axiosClient.post<ApiResponse<FoodItemResponse>>(`/food-items/store/${storeId}`, data),

  update: (id: number, data: FoodItemRequest) =>
    axiosClient.put<ApiResponse<FoodItemResponse>>(`/food-items/${id}`, data),

  delete: (id: number) =>
    axiosClient.delete<ApiResponse<void>>(`/food-items/${id}`),

  /**
   * Update the status of a food item.
   * Backend: PATCH /api/v1/food-items/{id}/status?status={status}
   * Valid statuses: ACTIVE, INACTIVE, PENDING, SOLD_OUT, EXPIRED
   */
  updateStatus: (id: number, status: FoodItemStatus) =>
    axiosClient.patch<ApiResponse<FoodItemResponse>>(
      `/food-items/${id}/status`,
      null,
      { params: { status } }
    ),
}
