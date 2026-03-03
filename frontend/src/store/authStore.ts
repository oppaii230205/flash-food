import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { UserResponse } from "@/types";

interface AuthState {
  user: UserResponse | null;
  token: string | null;
  /** Epoch ms when the access token expires (null = unknown / no expiry stored) */
  tokenExpiry: number | null;
  isAuthenticated: boolean;
  login: (token: string, user: UserResponse, expiresIn?: number) => void;
  updateUser: (partial: Partial<UserResponse>) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      tokenExpiry: null,
      isAuthenticated: false,

      login: (token, user, expiresIn) =>
        set({
          token,
          user,
          isAuthenticated: true,
          tokenExpiry: expiresIn ? Date.now() + expiresIn * 1000 : null,
        }),

      updateUser: (partial) =>
        set((state) => ({
          user: state.user ? { ...state.user, ...partial } : null,
        })),

      logout: () =>
        set({
          user: null,
          token: null,
          tokenExpiry: null,
          isAuthenticated: false,
        }),
    }),
    {
      name: "flash-food-auth",
      partialize: (state) => ({
        token: state.token,
        tokenExpiry: state.tokenExpiry,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);
