import { Clock } from "@phosphor-icons/react";
import { useCountdown } from "@/hooks/useCountdown";
import { cn } from "@/utils/cn";

interface CountdownTimerProps {
  /** compact: shows MM:SS only */
  compact?: boolean;
  targetHour?: number;
  className?: string;
}

export function CountdownTimer({
  compact,
  targetHour,
  className,
}: CountdownTimerProps) {
  const { formatted, formattedShort, isUrgent } = useCountdown(targetHour);

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 font-bold tabular-nums",
        isUrgent ? "text-orange-500" : "text-orange-400",
        className,
      )}
    >
      <Clock size={12} weight="bold" className="flex-shrink-0" />
      {compact ? formattedShort : formatted}
    </span>
  );
}
