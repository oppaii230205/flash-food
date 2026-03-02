import { useState } from 'react'
import { Navbar } from '@/components/layout/Navbar'
import { Footer } from '@/components/layout/Footer'
import { HeroSection } from '@/components/landing/HeroSection'
import { TickerBanner } from '@/components/landing/TickerBanner'
import { DealsPreview } from '@/components/landing/DealsPreview'
import { HowItWorks } from '@/components/landing/HowItWorks'
import { ImpactStats } from '@/components/landing/ImpactStats'
import { Testimonials } from '@/components/landing/Testimonials'
import { CtaSection } from '@/components/landing/CtaSection'
import { LoginModal } from '@/components/auth/LoginModal'
import { SignupModal } from '@/components/auth/SignupModal'

export type AuthModal = 'login' | 'signup' | null

export function LandingPage() {
  const [authModal, setAuthModal] = useState<AuthModal>(null)

  const openLogin  = () => setAuthModal('login')
  const openSignup = () => setAuthModal('signup')
  const close      = () => setAuthModal(null)

  return (
    <div className="min-h-screen bg-beige-50 font-body">
      <Navbar onLoginClick={openLogin} onSignupClick={openSignup} />

      <main>
        <HeroSection />
        <TickerBanner />
        <DealsPreview />
        <HowItWorks />
        <ImpactStats />
        <Testimonials />
        <CtaSection />
      </main>

      <Footer />

      <LoginModal
        open={authModal === 'login'}
        onClose={close}
        onSwitchSignup={() => setAuthModal('signup')}
      />
      <SignupModal
        open={authModal === 'signup'}
        onClose={close}
        onSwitchLogin={() => setAuthModal('login')}
      />
    </div>
  )
}
