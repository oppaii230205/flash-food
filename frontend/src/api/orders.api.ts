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
  // -- Customer ----------------------------------------------------------------
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
    axiosClient.patch<ApiResponse<OrderResponse>>(`/orders/${id}/cancel`, data ?? {}),

  processPayment: (id: number) =>
    axiosClient.post<ApiResponse<OrderResponse>>(`/orders/${id}/payment`),

  // -- Store Owner -------------------------------------------------------------
  /** GET /orders/store/{storeId} - all orders regardless of status */
  getByStore: (storeId: number, params?: Omit<OrderQueryParams, "status">) =>
    axiosClient.get<ApiResponse<PageResponse<OrderResponse>>>(
      `/orders/store/${storeId}`,
      { params },
    ),

  /** GET /orders/store/{storeId}/status/{status} - filtered by a specific status */
  getByStoreAndStatus: (
    storeId: number,
    status: string,
    params?: Omit<OrderQueryParams, "status">,
  ) =>
    axiosClient.get<ApiResponse<PageResponse<OrderResponse>>>(
      `/orders/store/${storeId}/status/${status}`,
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
