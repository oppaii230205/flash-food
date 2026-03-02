import { forwardRef, type ButtonHTMLAttributes } from 'react'
import { cn } from '@/utils/cn'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'
type Size    = 'sm' | 'md' | 'lg'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: Size
  loading?: boolean
}

const variants: Record<Variant, string> = {
  primary: [
    'bg-gradient-to-br from-green-600 to-green-500 text-white',
    'shadow-[0_4px_20px_rgba(58,138,98,.35)]',
    'hover:shadow-[0_8px_30px_rgba(58,138,98,.45)] hover:-translate-y-0.5',
  ].join(' '),

  secondary: [
    'bg-white text-green-700 border-2 border-green-200',
    'hover:-translate-y-0.5 hover:border-green-400 hover:shadow-sm',
  ].join(' '),

  ghost: 'text-green-700 hover:bg-green-50',

  danger: 'bg-red-500 text-white hover:bg-red-600',
}

const sizes: Record<Size, string> = {
  sm:  'px-4 py-2 text-sm gap-1.5',
  md:  'px-6 py-3 text-sm gap-2',
  lg:  'px-7 py-3.5 text-base gap-2',
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ variant = 'primary', size = 'md', loading, className, disabled, children, ...rest }, ref) => (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={cn(
        'inline-flex items-center justify-center font-bold rounded-full',
        'transition-all duration-200 cursor-pointer select-none',
        'disabled:opacity-60 disabled:cursor-not-allowed disabled:transform-none',
        variants[variant],
        sizes[size],
        className,
      )}
      {...rest}
    >
      {loading && (
        <span className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
      )}
      {children}
    </button>
  ),
)

Button.displayName = 'Button'
