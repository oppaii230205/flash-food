import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface SavedDealsState {
  savedIds: number[]
  toggle: (id: number) => void
  isSaved: (id: number) => boolean
  clearAll: () => void
}

export const useSavedDealsStore = create<SavedDealsState>()(
  persist(
    (set, get) => ({
      savedIds: [],

      toggle: (id) =>
        set((state) => ({
          savedIds: state.savedIds.includes(id)
            ? state.savedIds.filter((s) => s !== id)
            : [...state.savedIds, id],
        })),

      isSaved: (id) => get().savedIds.includes(id),

      clearAll: () => set({ savedIds: [] }),
    }),
    { name: 'flash-food-saved-deals' },
  ),
)
