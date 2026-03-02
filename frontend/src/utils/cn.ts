import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

/** Merges Tailwind classes safely (handles conflict resolution). */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
