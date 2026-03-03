import { cn } from '@/utils/cn'

type BadgeVariant = 'discount' | 'quantity' | 'status' | 'category' | 'tag'

interface BadgeProps {
  variant?: BadgeVariant
  children: React.ReactNode
  className?: string
}

const styles: Record<BadgeVariant, string> = {
  discount: 'bg-orange-500 text-white text-[0.68rem] font-extrabold tracking-wide px-2.5 py-1',
  quantity: 'bg-green-900/85 text-green-200 text-[0.63rem] font-bold px-2.5 py-1 backdrop-blur-sm',
  status:   'bg-green-100 text-green-700 text-[0.72rem] font-bold px-3 py-1',
  category: 'bg-beige-200 text-beige-700 text-[0.72rem] font-bold px-3 py-1',
  tag:      'bg-green-50 text-green-700 text-[0.68rem] font-bold px-2.5 py-1 border border-green-200',
}

export function Badge({ variant = 'status', children, className }: BadgeProps) {
  return (
    <span className={cn('inline-flex items-center gap-1 rounded-full', styles[variant], className)}>
      {children}
    </span>
  )
}
