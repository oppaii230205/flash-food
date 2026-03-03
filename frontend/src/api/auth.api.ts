import axiosClient from "./axiosClient";
import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from "@/types";

export const authApi = {
  register: (data: RegisterRequest) =>
    axiosClient.post<ApiResponse<AuthResponse>>("/auth/register", data),

  login: (data: LoginRequest) =>
    axiosClient.post<ApiResponse<AuthResponse>>("/auth/login", data),

  /**
   * Refresh token is read from the httpOnly cookie automatically.
   * No request body is sent — the browser attaches the cookie.
   */
  refreshToken: () =>
    axiosClient.post<ApiResponse<AuthResponse>>("/auth/refresh-token"),

  /**
   * Logout: revokes the refresh token stored in the httpOnly cookie.
   * Requires a valid access token in the Authorization header.
   * No request body — the backend reads the cookie directly.
   */
  logout: () => axiosClient.post<ApiResponse<void>>("/auth/logout"),
};
