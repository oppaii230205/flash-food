import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { UserResponse } from '@/types'

interface AuthState {
  user: UserResponse | null
  token: string | null
  isAuthenticated: boolean
  login: (token: string, user: UserResponse) => void
  updateUser: (user: UserResponse) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: (token, user) =>
        set({ token, user, isAuthenticated: true }),

      updateUser: (user) =>
        set({ user }),

      logout: () =>
        set({ user: null, token: null, isAuthenticated: false }),
    }),
    {
      name: 'flash-food-auth',
      partialize: (state) => ({
        token: state.token,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
)
