/** Format integer price in cents → "$12.50" */
export function formatPrice(cents: number): string {
  return `$${(cents / 100).toFixed(2)}`
}

/** Format a raw dollar integer (the backend stores prices as integers, e.g. 1200 = $12.00) */
export function formatPriceInt(value: number): string {
  if (value >= 100) return `$${(value / 100).toFixed(2)}`
  return `$${value.toFixed(2)}`
}

/** "2026-02-21T15:40:21" → "Feb 21, 2026" */
export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
  })
}

/** "2026-02-21T15:40:21" → "3:40 PM" */
export function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('en-US', {
    hour: 'numeric', minute: '2-digit', hour12: true,
  })
}

/** kilometres → "0.4 mi" or "2.1 km" */
export function formatDistance(km: number): string {
  if (km < 1) return `${Math.round(km * 1000)} m`
  return `${km.toFixed(1)} km`
}

/** Abbreviate large numbers: 1200 → "1.2K", 1400000 → "1.4M" */
export function formatCount(n: number): string {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`
  if (n >= 1_000)     return `${(n / 1_000).toFixed(1)}K`
  return String(n)
}

/** Discount percentage badge label */
export function discountLabel(pct: number): string {
  return `${pct}% OFF`
}
