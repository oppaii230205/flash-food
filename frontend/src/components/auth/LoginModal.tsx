import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { motion, AnimatePresence } from 'framer-motion'
import { X } from 'lucide-react'
import { authApi } from '@/api/auth.api'
import { useAuthStore } from '@/store/authStore'
import { Button } from '@/components/ui/Button'
import toast from 'react-hot-toast'

const schema = z.object({
  email:    z.string().email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
})
type FormData = z.infer<typeof schema>

interface Props {
  open:           boolean
  onClose:        () => void
  onSwitchSignup: () => void
}

export function LoginModal({ open, onClose, onSwitchSignup }: Props) {
  const { login } = useAuthStore()

  const { register, handleSubmit, formState: { errors }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: FormData) => {
    try {
      const res = await authApi.login(data)
      const auth = res.data.data
      login(auth.accessToken, auth.user)
      toast.success(`🌿 Welcome back, ${auth.user.email.split('@')[0]}!`)
      reset()
      onClose()
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? 'Invalid credentials')
    }
  }

  return (
    <AnimatePresence>
      {open && (
        <>
          {/* Overlay */}
          <motion.div
            key="overlay"
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40"
            onClick={onClose}
          />

          {/* Modal */}
          <motion.div
            key="modal"
            initial={{ opacity: 0, y: 32, scale: 0.97 }}
            animate={{ opacity: 1, y: 0,  scale: 1 }}
            exit={{   opacity: 0, y: 16, scale: 0.96 }}
            transition={{ duration: 0.28, ease: 'easeOut' }}
            className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-4"
          >
            <div className="bg-white rounded-t-2xl sm:rounded-2xl w-full max-w-sm shadow-xl border border-green-100">
              {/* Header */}
              <div className="flex items-center justify-between p-6 pb-0">
                <div>
                  <h2 className="text-xl font-extrabold text-green-900">Welcome back 🌿</h2>
                  <p className="text-[0.8rem] text-green-600/70 mt-0.5">Sign in to rescue your deals.</p>
                </div>
                <button
                  onClick={onClose}
                  className="w-8 h-8 rounded-full bg-green-50 grid place-items-center hover:bg-green-100 transition-colors"
                >
                  <X size={14} className="text-green-600" />
                </button>
              </div>

              <form onSubmit={handleSubmit(onSubmit)} className="p-6 flex flex-col gap-4">
                {/* Email */}
                <div>
                  <label className="block text-[0.8rem] font-bold text-green-800 mb-1">Email</label>
                  <input
                    {...register('email')}
                    type="email"
                    placeholder="you@example.com"
                    className="w-full rounded-full border border-green-200 px-4 py-2.5 text-[0.9rem] outline-none focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
                  />
                  {errors.email && <p className="text-red-500 text-[0.72rem] mt-1">{errors.email.message}</p>}
                </div>

                {/* Password */}
                <div>
                  <label className="block text-[0.8rem] font-bold text-green-800 mb-1">Password</label>
                  <input
                    {...register('password')}
                    type="password"
                    placeholder="••••••••"
                    className="w-full rounded-full border border-green-200 px-4 py-2.5 text-[0.9rem] outline-none focus:border-green-500 focus:ring-2 focus:ring-green-100 transition"
                  />
                  {errors.password && <p className="text-red-500 text-[0.72rem] mt-1">{errors.password.message}</p>}
                </div>

                <Button type="submit" className="w-full mt-1">Sign In</Button>

                <p className="text-center text-[0.8rem] text-green-600/70">
                  New here?{' '}
                  <button type="button" onClick={onSwitchSignup} className="text-green-600 font-bold hover:underline">
                    Create a free account
                  </button>
                </p>
              </form>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}
