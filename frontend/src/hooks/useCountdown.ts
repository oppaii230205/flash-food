import { useEffect, useState } from 'react'

interface CountdownResult {
  hours: number
  minutes: number
  seconds: number
  formatted: string         // HH:MM:SS
  formattedShort: string    // MM:SS
  totalSeconds: number
  isUrgent: boolean         // true when < 30 min remain
}

/**
 * Returns a live countdown to the next 10 PM pickup window.
 * Resets automatically once the target time passes.
 */
export function useCountdown(targetHour = 22): CountdownResult {
  const getTarget = () => {
    const now    = new Date()
    const target = new Date(now)
    target.setHours(targetHour, 0, 0, 0)
    if (now >= target) target.setDate(target.getDate() + 1)
    return target
  }

  const compute = (): CountdownResult => {
    const diff = Math.max(0, Math.floor((getTarget().getTime() - Date.now()) / 1000))
    const h    = Math.floor(diff / 3600)
    const m    = Math.floor((diff % 3600) / 60)
    const s    = diff % 60
    const pad  = (n: number) => String(n).padStart(2, '0')
    return {
      hours: h,
      minutes: m,
      seconds: s,
      formatted: `${pad(h)}:${pad(m)}:${pad(s)}`,
      formattedShort: `${pad(m)}:${pad(s)}`,
      totalSeconds: diff,
      isUrgent: diff < 30 * 60,
    }
  }

  const [value, setValue] = useState<CountdownResult>(compute)

  useEffect(() => {
    const id = setInterval(() => setValue(compute()), 1000)
    return () => clearInterval(id)
  })

  return value
}
