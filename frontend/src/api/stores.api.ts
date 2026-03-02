import axiosClient from './axiosClient'
import type {
  ApiResponse,
  PageResponse,
  StoreResponse,
  CreateStoreRequest,
  UpdateStoreRequest,
  StoreQueryParams,
} from '@/types'

export const storesApi = {
  // ── Public ──────────────────────────────────────────────────────────────
  getActive: (params?: StoreQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<StoreResponse>>>('/stores', { params }),

  getById: (id: number) =>
    axiosClient.get<ApiResponse<StoreResponse>>(`/stores/${id}`),

  search: (keyword: string) =>
    axiosClient.get<ApiResponse<PageResponse<StoreResponse>>>('/stores/search', {
      params: { keyword },
    }),

  getByType: (type: string) =>
    axiosClient.get<ApiResponse<PageResponse<StoreResponse>>>(`/stores/type/${type}`),

  // ── Authenticated ────────────────────────────────────────────────────────
  getNearby: (lat: number, lon: number, radius?: number) =>
    axiosClient.get<ApiResponse<StoreResponse[]>>('/stores/nearby', {
      params: { lat, lon, radius },
    }),

  // ── Store Owner / Admin ──────────────────────────────────────────────────
  create: (data: CreateStoreRequest) =>
    axiosClient.post<ApiResponse<StoreResponse>>('/stores', data),

  update: (id: number, data: UpdateStoreRequest) =>
    axiosClient.put<ApiResponse<StoreResponse>>(`/stores/${id}`, data),

  delete: (id: number) =>
    axiosClient.delete<ApiResponse<void>>(`/stores/${id}`),

  getMyStores: () =>
    axiosClient.get<ApiResponse<StoreResponse[]>>('/stores/my-stores'),

  updateStatus: (id: number, status: string, reason?: string) =>
    axiosClient.patch<ApiResponse<StoreResponse>>(`/stores/${id}/status`, { status, reason }),

  // ── Admin ────────────────────────────────────────────────────────────────
  approve: (id: number) =>
    axiosClient.post<ApiResponse<StoreResponse>>(`/stores/${id}/approve`),

  getAll: (params?: StoreQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<StoreResponse>>>('/stores/admin/all', { params }),
}
