import axios, { type AxiosRequestConfig } from "axios";
import type { ApiResponse, AuthResponse } from "@/types";
import { useAuthStore } from "@/store/authStore";

// ── Axios instance ────────────────────────────────────────────────────────────
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "/api/v1",
  withCredentials: true, // browser attaches the httpOnly refresh-token cookie
  headers: { "Content-Type": "application/json" },
});

// ── Silent token-refresh state ────────────────────────────────────────────────
type PendingItem = {
  resolve: (token: string) => void;
  reject: (err: unknown) => void;
};

let isRefreshing = false;
let pendingQueue: PendingItem[] = [];

function drainQueue(token: string | null, err: unknown = null) {
  pendingQueue.forEach((item) =>
    token ? item.resolve(token) : item.reject(err),
  );
  pendingQueue = [];
}

// ── Request: attach access token ─────────────────────────────────────────────
axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ── Response: silent refresh on 401, hard-logout if refresh also fails ────────
axiosClient.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config as AxiosRequestConfig & { _retry?: boolean };
    const status: number | undefined = error.response?.status;

    // Don't attempt refresh for auth endpoints (prevents infinite loops)
    const isAuthEndpoint = (original.url ?? "").includes("/auth/");

    if (status === 401 && !original._retry && !isAuthEndpoint) {
      original._retry = true;

      if (isRefreshing) {
        // Another refresh is in-flight — queue and wait
        return new Promise<string>((resolve, reject) => {
          pendingQueue.push({ resolve, reject });
        }).then((newToken) => {
          original.headers = {
            ...original.headers,
            Authorization: `Bearer ${newToken}`,
          };
          return axiosClient(original);
        });
      }

      isRefreshing = true;

      try {
        const { data } = await axiosClient.post<ApiResponse<AuthResponse>>(
          "/auth/refresh-token",
        );
        const { accessToken, expiresIn, user } = data.data;
        useAuthStore
          .getState()
          .login(accessToken, user, expiresIn ?? undefined);
        drainQueue(accessToken);
        original.headers = {
          ...original.headers,
          Authorization: `Bearer ${accessToken}`,
        };
        return axiosClient(original);
      } catch (refreshErr) {
        drainQueue(null, refreshErr);
        useAuthStore.getState().logout();
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  },
);

export default axiosClient;
