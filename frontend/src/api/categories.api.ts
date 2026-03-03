import axiosClient from "./axiosClient";
import type {
  ApiResponse,
  PageResponse,
  CategoryResponse,
  CategoryRequest,
} from "@/types";

export const categoriesApi = {
  // ── Public (no auth needed) ───────────────────────────────────────────────
  /** All active categories as a flat list — used for selects/dropdowns */
  getActive: () =>
    axiosClient.get<ApiResponse<CategoryResponse[]>>("/categories/active"),

  /** Root categories (no parent) */
  getRoots: () =>
    axiosClient.get<ApiResponse<CategoryResponse[]>>("/categories/root"),

  /** Children of a specific category */
  getChildren: (parentId: number) =>
    axiosClient.get<ApiResponse<CategoryResponse[]>>(
      `/categories/${parentId}/children`,
    ),

  getById: (id: number) =>
    axiosClient.get<ApiResponse<CategoryResponse>>(`/categories/${id}`),

  search: (keyword: string) =>
    axiosClient.get<ApiResponse<CategoryResponse[]>>("/categories/search", {
      params: { keyword },
    }),

  getAll: (params?: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: string;
  }) =>
    axiosClient.get<ApiResponse<PageResponse<CategoryResponse>>>(
      "/categories",
      { params },
    ),

  // ── Admin ─────────────────────────────────────────────────────────────────
  create: (data: CategoryRequest) =>
    axiosClient.post<ApiResponse<CategoryResponse>>("/categories", data),

  update: (id: number, data: CategoryRequest) =>
    axiosClient.put<ApiResponse<CategoryResponse>>(`/categories/${id}`, data),

  delete: (id: number) =>
    axiosClient.delete<ApiResponse<void>>(`/categories/${id}`),
};
