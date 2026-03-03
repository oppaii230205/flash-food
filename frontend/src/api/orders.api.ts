import axiosClient from "./axiosClient";
import type {
  ApiResponse,
  PageResponse,
  OrderResponse,
  CreateOrderRequest,
  CancelOrderRequest,
  OrderQueryParams,
} from "@/types";

export const ordersApi = {
  // ── Customer ─────────────────────────────────────────────────────────────
  create: (data: CreateOrderRequest) =>
    axiosClient.post<ApiResponse<OrderResponse>>("/orders", data),

  getMyOrders: (params?: OrderQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<OrderResponse>>>(
      "/orders/my-orders",
      { params },
    ),

  getById: (id: number) =>
    axiosClient.get<ApiResponse<OrderResponse>>(`/orders/${id}`),

  cancel: (id: number, data?: CancelOrderRequest) =>
    axiosClient.patch<ApiResponse<OrderResponse>>(`/orders/${id}/cancel`, data),

  processPayment: (id: number) =>
    axiosClient.post<ApiResponse<OrderResponse>>(`/orders/${id}/payment`),

  // ── Store Owner ───────────────────────────────────────────────────────────
  getByStore: (storeId: number, params?: OrderQueryParams) =>
    axiosClient.get<ApiResponse<PageResponse<OrderResponse>>>(
      `/orders/store/${storeId}`,
      { params },
    ),
  confirm: (id: number) =>
    axiosClient.patch<ApiResponse<OrderResponse>>(`/orders/${id}/confirm`),

  markPreparing: (id: number) =>
    axiosClient.patch<ApiResponse<OrderResponse>>(`/orders/${id}/preparing`),

  markReady: (id: number) =>
    axiosClient.patch<ApiResponse<OrderResponse>>(`/orders/${id}/ready`),

  complete: (id: number) =>
    axiosClient.patch<ApiResponse<OrderResponse>>(`/orders/${id}/complete`),
};
