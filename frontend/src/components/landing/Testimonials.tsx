import { type ReactNode } from "react";
import {
  Star,
  Leaf,
  Lightning,
  Heart,
  Storefront,
  Plant,
  Globe,
  ChatCircle,
} from "@phosphor-icons/react";
import { useScrollReveal } from "@/hooks/useScrollReveal";
import { SectionLabel } from "@/components/ui/SectionLabel";
import { motion } from "framer-motion";

const TESTIMONIALS: {
  text: string;
  name: string;
  role: string;
  color: string;
  tag: { icon: ReactNode; label: string };
}[] = [
  {
    text: "\"I rescue 3–4 meals a week now. The savings are incredible — and I love knowing I'm reducing food waste in my city. It's become a ritual.\"",
    name: "Maya R.",
    role: "Designer · Midtown",
    color: "#52b788",
    tag: { icon: <Leaf size={11} weight="fill" />, label: "Planet Hero" },
  },
  {
    text: '"Flash Food genuinely changed how I eat. Restaurant-level meals for the price of a snack. Completely addicted."',
    name: "James T.",
    role: "Developer · Brooklyn",
    color: "#2d6a4f",
    tag: {
      icon: <Lightning size={11} weight="fill" />,
      label: "Power Rescuer",
    },
  },
  {
    text: '"As a single mom, the savings matter enormously. My kids love \"rescue night\" — we try something new every week and it feels meaningful too."',
    name: "Sofia M.",
    role: "Teacher · Queens",
    color: "#80ded9",
    tag: { icon: <Heart size={11} weight="fill" />, label: "Family Saver" },
  },
  {
    text: '"Flash Food helps us sell surplus we\'d otherwise throw out. The platform is simple, the community is wonderful."',
    name: "Chen W.",
    role: "Bakery Owner · SoHo",
    color: "#c9a07a",
    tag: {
      icon: <Storefront size={11} weight="fill" />,
      label: "Partner Store",
    },
  },
  {
    text: '"I was skeptical at first — sushi, pastries, Thai — all fresh, all at 70% off. Absolutely brilliant concept."',
    name: "Priya K.",
    role: "Nurse · Upper East Side",
    color: "#e57373",
    tag: { icon: <Plant size={11} weight="fill" />, label: "New Rescuer" },
  },
  {
    text: '"The app I recommend to literally everyone. Simple, fast, meaningful. Flash Food is what conscious consumption looks like."',
    name: "Leon D.",
    role: "Environmental Consultant",
    color: "#3a8a62",
    tag: { icon: <Globe size={11} weight="fill" />, label: "Ambassador" },
  },
];

function TestimonialCard({ t }: { t: (typeof TESTIMONIALS)[0] }) {
  return (
    <div className="bg-beige-50 border border-green-100 rounded-lg p-6 w-[300px] flex-shrink-0 hover:-translate-y-0.5 hover:shadow-md transition-all duration-200">
      <div className="flex gap-0.5 mb-3">
        {Array.from({ length: 5 }).map((_, i) => (
          <Star key={i} size={14} weight="fill" className="text-[#f4b942]" />
        ))}
      </div>
      <p className="text-[0.86rem] text-green-800/70 leading-relaxed mb-4">
        {t.text}
      </p>
      <div className="flex items-center gap-2.5">
        <span
          className="w-10 h-10 rounded-full grid place-items-center text-sm flex-shrink-0 font-extrabold text-white"
          style={{ background: t.color }}
        >
          {t.name[0]}
        </span>
        <div>
          <div className="text-[0.84rem] font-bold text-green-900">
            {t.name}
          </div>
          <div className="text-[0.71rem] text-green-600/70">{t.role}</div>
          <span className="inline-flex items-center gap-1 bg-green-50 text-green-700 rounded-full px-2 py-0.5 text-[0.66rem] font-bold border border-green-200 mt-1">
            {t.tag.icon} {t.tag.label}
          </span>
        </div>
      </div>
    </div>
  );
}

export function Testimonials() {
  const { ref, isVisible } = useScrollReveal<HTMLElement>();
  const doubled = [...TESTIMONIALS, ...TESTIMONIALS];

  return (
    <section
      ref={ref}
      id="community"
      className="py-20 bg-white overflow-hidden"
    >
      <div className="max-w-6xl mx-auto px-5">
        <div className="text-center mb-11">
          <motion.div
            initial={{ opacity: 0, y: 28 }}
            animate={isVisible ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.7 }}
          >
            <SectionLabel>
              <ChatCircle size={12} weight="fill" /> Community Stories
            </SectionLabel>
            <h2 className="text-[clamp(1.6rem,4vw,2.4rem)] font-extrabold text-green-900 leading-snug">
              Neighbors Who Rescue
            </h2>
            <p className="text-green-700/60 text-sm mt-2 max-w-md mx-auto">
              Join thousands of conscious eaters making a difference one meal at
              a time.
            </p>
          </motion.div>
        </div>
      </div>

      {/* Scrolling track — full bleed */}
      <div className="overflow-hidden -mx-5 px-5">
        <div
          className="flex gap-4 animate-scroll-cards"
          style={{ width: "max-content" }}
          onMouseEnter={(e) =>
            ((e.currentTarget as HTMLDivElement).style.animationPlayState =
              "paused")
          }
          onMouseLeave={(e) =>
            ((e.currentTarget as HTMLDivElement).style.animationPlayState =
              "running")
          }
        >
          {doubled.map((t, i) => (
            <TestimonialCard key={i} t={t} />
          ))}
        </div>
      </div>
    </section>
  );
}
