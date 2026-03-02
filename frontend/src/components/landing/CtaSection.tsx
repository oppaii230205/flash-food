import { useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useScrollReveal } from '@/hooks/useScrollReveal'
import { Button } from '@/components/ui/Button'
import { useAuthStore } from '@/store/authStore'
import toast from 'react-hot-toast'

export function CtaSection() {
  const { ref, isVisible } = useScrollReveal<HTMLElement>()
  const { isAuthenticated } = useAuthStore()
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!email) return
    setLoading(true)
    try {
      // Just show success — full signup uses modal flow
      await new Promise((r) => setTimeout(r, 600))
      toast.success(`🌿 Check ${email} for your early access link!`)
      setEmail('')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section
      ref={ref}
      id="get-started"
      className="py-20"
      style={{
        background:
          'radial-gradient(ellipse 60% 80% at 100% 50%,#d4f4ee 0%,transparent 55%),' +
          'radial-gradient(ellipse 50% 60% at 0% 50%,#d8f3dc 0%,transparent 55%),' +
          '#fdfaf5',
      }}
    >
      <div className="max-w-6xl mx-auto px-5">
        <motion.div
          initial={{ opacity: 0, y: 28 }}
          animate={isVisible ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.7 }}
          className="relative rounded-xl overflow-hidden text-center px-8 py-16"
          style={{ background: 'linear-gradient(135deg,#1a3d2b,#1e4d35)' }}
        >
          {/* Orb */}
          <div
            className="absolute w-[500px] h-[500px] -top-64 -right-36 rounded-full pointer-events-none"
            style={{ background: 'radial-gradient(circle,rgba(82,183,136,.18) 0%,transparent 65%)' }}
          />

          <div className="relative z-10">
            <div className="text-5xl mb-4">🌿</div>
            <h2 className="text-[clamp(1.6rem,4vw,2.8rem)] font-extrabold text-white leading-tight mb-3">
              Ready to Rescue<br />Your First Meal?
            </h2>
            <p className="text-green-300 text-base mb-8 max-w-md mx-auto">
              Download the app or sign up online — save up to 70%&nbsp;on delicious surplus food tonight.
            </p>

            {/* App store buttons */}
            <div className="flex flex-wrap gap-3 justify-center mb-6">
              {[
                { icon: '🍎', sub: 'Download on the', label: 'App Store' },
                { icon: '▶️', sub: 'Get it on',        label: 'Google Play' },
              ].map((s) => (
                <button
                  key={s.label}
                  className="flex items-center gap-2.5 bg-white/12 border border-white/20 rounded-md px-5 py-3 text-white font-bold text-[0.88rem] hover:bg-white/20 hover:-translate-y-0.5 transition-all backdrop-blur-sm"
                >
                  <span className="text-2xl">{s.icon}</span>
                  <span className="text-left">
                    <span className="block text-[0.64rem] font-medium opacity-75">{s.sub}</span>
                    {s.label}
                  </span>
                </button>
              ))}
            </div>

            {/* Divider */}
            <div className="flex items-center gap-3.5 justify-center my-4 text-green-400 text-[0.78rem] font-semibold">
              <span className="flex-1 max-w-[80px] h-px bg-white/15" />
              or sign up with email
              <span className="flex-1 max-w-[80px] h-px bg-white/15" />
            </div>

            {isAuthenticated ? (
              <Link to="/deals">
                <Button size="lg" className="mx-auto">Go to Tonight's Deals ↗</Button>
              </Link>
            ) : (
              <form onSubmit={handleSubmit} className="flex gap-2 max-w-sm mx-auto flex-wrap justify-center">
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="your@email.com"
                  required
                  className="flex-1 min-w-[200px] px-5 py-3 rounded-full border border-white/20 bg-white/10 text-white placeholder-green-400 font-medium text-[0.9rem] outline-none focus:border-green-400 backdrop-blur-sm"
                />
                <Button type="submit" loading={loading} className="whitespace-nowrap">
                  Get Early Access
                </Button>
              </form>
            )}

            {/* Perks */}
            <div className="flex flex-wrap gap-3 justify-center mt-7">
              {['✅ Free to join', '🔒 No spam, ever', '🌿 1 tree planted per signup', '⚡ Instant access'].map((p) => (
                <span key={p} className="flex items-center gap-1.5 bg-white/8 rounded-full px-3.5 py-1.5 text-[0.77rem] text-green-200 font-semibold">
                  {p}
                </span>
              ))}
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  )
}
