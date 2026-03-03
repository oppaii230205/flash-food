import { cn } from '@/utils/cn'

interface SectionLabelProps {
  children: React.ReactNode
  className?: string
  dark?: boolean
}

export function SectionLabel({ children, className, dark }: SectionLabelProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 text-[0.72rem] font-extrabold tracking-widest',
        'uppercase rounded-full px-3.5 py-1 mb-3.5 border',
        dark
          ? 'bg-green-500/15 border-green-500/30 text-green-300'
          : 'bg-green-50 border-green-200 text-green-600',
        className,
      )}
    >
      {children}
    </span>
  )
}
