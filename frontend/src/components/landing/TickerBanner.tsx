const items = [
  '12,400 meals rescued this month',
  '340+ partner restaurants',
  '8.6 tons of food waste prevented',
  'Average 63% savings per rescue',
  'Available in 40+ neighborhoods',
  '9–10 PM pickup window daily',
  'Join 52,000 food rescuers',
]

export function TickerBanner() {
  const doubled = [...items, ...items]

  return (
    <div className="bg-green-900 text-green-200 py-2.5 overflow-hidden" aria-hidden="true">
      <div className="inline-flex gap-12 animate-ticker whitespace-nowrap">
        {doubled.map((text, i) => (
          <span key={i} className="inline-flex items-center gap-2.5 text-[0.78rem] font-semibold flex-shrink-0">
            <span className="w-1.5 h-1.5 rounded-full bg-green-500 flex-shrink-0" />
            {text}
          </span>
        ))}
      </div>
    </div>
  )
}
