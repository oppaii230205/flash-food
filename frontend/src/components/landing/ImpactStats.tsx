import { useEffect, useRef, useState } from 'react'
import { motion } from 'framer-motion'
import { useScrollReveal } from '@/hooks/useScrollReveal'
import { SectionLabel } from '@/components/ui/SectionLabel'

const STATS = [
  { icon: '🥡', target: 148400, suffix: '+', label: 'Meals Rescued', format: 'abbr' },
  { icon: '🌱', target: 103,    suffix: ' tons', label: 'Food Waste Prevented', format: 'plain' },
  { icon: '🏪', target: 340,    suffix: '+', label: 'Partner Stores', format: 'plain' },
  { icon: '💰', target: 620000, prefix: '$', label: 'Saved by Rescuers', format: 'abbr' },
]

const BARS = [
  { label: 'CO₂ Emissions Avoided',   pct: 78 },
  { label: 'Water Waste Reduced',      pct: 65 },
  { label: 'Landfill Diversion Rate',  pct: 91 },
  { label: 'Partner Satisfaction',     pct: 96 },
]

function useCounter(target: number, isVisible: boolean, format: string) {
  const [value, setValue] = useState(0)
  const started = useRef(false)

  useEffect(() => {
    if (!isVisible || started.current) return
    started.current = true
    const duration = 1600
    const start    = performance.now()

    const animate = (now: number) => {
      const progress = Math.min((now - start) / duration, 1)
      const ease     = 1 - Math.pow(1 - progress, 3)
      setValue(Math.floor(ease * target))
      if (progress < 1) requestAnimationFrame(animate)
    }
    requestAnimationFrame(animate)
  }, [isVisible, target])

  if (!isVisible && !started.current) return '0'
  const abbr = (n: number) => n >= 1_000_000 ? `${(n/1_000_000).toFixed(1)}M` : n >= 1_000 ? `${Math.round(n/1_000)}K` : String(n)
  return format === 'abbr' ? abbr(value) : value.toLocaleString()
}

function StatCard({ stat, isVisible }: { stat: typeof STATS[0]; isVisible: boolean }) {
  const val = useCounter(stat.target, isVisible, stat.format)
  return (
    <div className="bg-white/7 border border-white/12 rounded-lg p-7 text-center backdrop-blur-sm hover:bg-white/11 transition-all hover:-translate-y-1 duration-200">
      <div className="text-3xl mb-2.5">{stat.icon}</div>
      <div className="text-[clamp(1.5rem,3.5vw,2.4rem)] font-extrabold text-white leading-none mb-1.5 tabular-nums">
        {stat.prefix ?? ''}{val}<span className="text-green-400">{stat.suffix}</span>
      </div>
      <div className="text-[0.78rem] text-green-300 font-medium">{stat.label}</div>
    </div>
  )
}

function BarRow({ bar, isVisible }: { bar: typeof BARS[0]; isVisible: boolean }) {
  return (
    <div>
      <div className="flex justify-between text-[0.77rem] text-green-300 font-semibold mb-1.5">
        <span>{bar.label}</span><span>{bar.pct}%</span>
      </div>
      <div className="h-2 bg-white/10 rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-[1400ms] ease-out"
          style={{
            width: isVisible ? `${bar.pct}%` : '0%',
            background: 'linear-gradient(90deg,#52b788,#80ded9)',
          }}
        />
      </div>
    </div>
  )
}

export function ImpactStats() {
  const { ref, isVisible } = useScrollReveal<HTMLElement>()

  return (
    <section
      ref={ref}
      id="impact"
      className="py-20 relative overflow-hidden"
      style={{ background: 'linear-gradient(160deg,#1a3d2b 0%,#1e4d35 100%)' }}
    >
      {/* Background orbs */}
      <div className="absolute w-[700px] h-[700px] -top-48 -left-48 rounded-full pointer-events-none"
        style={{ background: 'radial-gradient(circle,rgba(82,183,136,.12) 0%,transparent 65%)' }} />
      <div className="absolute w-[400px] h-[400px] -bottom-24 -right-24 rounded-full pointer-events-none"
        style={{ background: 'radial-gradient(circle,rgba(128,222,217,.10) 0%,transparent 65%)' }} />

      <div className="max-w-6xl mx-auto px-5 relative z-10">
        <div className="text-center mb-14">
          <motion.div
            initial={{ opacity: 0, y: 28 }}
            animate={isVisible ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.7 }}
          >
            <SectionLabel dark>🌍 Real Numbers, Real Change</SectionLabel>
            <h2 className="text-[clamp(1.6rem,4vw,2.4rem)] font-extrabold text-white leading-snug">
              Our Community's Impact
            </h2>
            <p className="text-green-300 text-sm mt-2 max-w-md mx-auto">
              Every rescue reduces methane emissions, supports local businesses, and feeds families in need.
            </p>
          </motion.div>
        </div>

        {/* Stat grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-14">
          {STATS.map((s, i) => (
            <motion.div
              key={s.label}
              initial={{ opacity: 0, y: 28 }}
              animate={isVisible ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.7, delay: i * 0.1 }}
            >
              <StatCard stat={s} isVisible={isVisible} />
            </motion.div>
          ))}
        </div>

        {/* Progress bars */}
        <motion.div
          initial={{ opacity: 0, y: 28 }}
          animate={isVisible ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.7, delay: 0.3 }}
          className="bg-white/7 border border-white/10 rounded-xl p-8"
        >
          <h3 className="text-[0.88rem] font-bold text-green-200 mb-5 flex items-center gap-2">
            📊 Environmental Breakdown
          </h3>
          <div className="flex flex-col gap-[14px]">
            {BARS.map((b) => (
              <BarRow key={b.label} bar={b} isVisible={isVisible} />
            ))}
          </div>
        </motion.div>
      </div>
    </section>
  )
}
