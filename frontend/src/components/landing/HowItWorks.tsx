import { motion } from "framer-motion";
import { MapTrifold, Lightning, Leaf, Sparkle } from "@phosphor-icons/react";
import { useScrollReveal } from "@/hooks/useScrollReveal";
import { SectionLabel } from "@/components/ui/SectionLabel";

const steps = [
  {
    number: 1,
    icon: <MapTrifold size={36} weight="duotone" className="text-green-600" />,
    bg: "bg-green-100",
    title: "Discover Nearby Deals",
    desc: "Open the app at 9 PM and browse surplus food from restaurants, cafés, and bakeries within walking distance.",
  },
  {
    number: 2,
    icon: <Lightning size={36} weight="duotone" className="text-green-600" />,
    bg: "bg-mint-100",
    title: "Rescue in One Tap",
    desc: "Select your meal, checkout instantly, and pay securely. You'll receive a QR code confirmation within seconds.",
  },
  {
    number: 3,
    icon: <Leaf size={36} weight="duotone" className="text-green-600" />,
    bg: "bg-beige-200",
    title: "Pick Up & Feel Good",
    desc: "Swing by the store between 9–10 PM, show your QR code, and collect your rescued meal. Planet saved.",
  },
];

export function HowItWorks() {
  const { ref, isVisible } = useScrollReveal<HTMLElement>();

  return (
    <section
      ref={ref}
      id="how"
      className="py-20"
      style={{
        background:
          "radial-gradient(ellipse 70% 80% at 0% 50%, #f0faf3 0%, transparent 60%)," +
          "#fdfaf5",
      }}
    >
      <div className="max-w-6xl mx-auto px-5">
        <div className="text-center mb-14">
          <motion.div
            initial={{ opacity: 0, y: 28 }}
            animate={isVisible ? { opacity: 1, y: 0 } : {}}
            transition={{ duration: 0.7 }}
          >
            <SectionLabel>
              <Sparkle size={12} weight="fill" /> Simple as 1–2–3
            </SectionLabel>
            <h2 className="text-[clamp(1.6rem,4vw,2.4rem)] font-extrabold text-green-900 leading-snug">
              How Flash Food Works
            </h2>
            <p className="text-green-700/60 text-sm mt-2 max-w-md mx-auto">
              From discovery to pickup in under 3 minutes. No subscriptions, no
              waste, no fuss.
            </p>
          </motion.div>
        </div>

        <div className="relative grid grid-cols-1 md:grid-cols-3 gap-5">
          {/* Connector line (desktop) */}
          <div
            className="hidden md:block absolute top-9 pointer-events-none"
            style={{
              left: "calc(16.67% + 36px)",
              right: "calc(16.67% + 36px)",
              height: 2,
              background: "linear-gradient(90deg,#b7e4c7,#b5ead7,#b7e4c7)",
              borderRadius: 2,
            }}
          />

          {steps.map((step, i) => (
            <motion.div
              key={step.number}
              initial={{ opacity: 0, y: 28 }}
              animate={isVisible ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.7, delay: i * 0.12 }}
              whileHover={{
                y: -4,
                boxShadow: "0 4px 20px rgba(52,120,80,.13)",
              }}
              className="relative bg-white rounded-lg border border-green-100 shadow-sm p-8 flex flex-col items-center text-center gap-4"
            >
              {/* Step number pill */}
              <span className="absolute -top-3.5 left-1/2 -translate-x-1/2 w-7 h-7 bg-green-600 text-white rounded-full text-[0.72rem] font-extrabold grid place-items-center border-[3px] border-white">
                {step.number}
              </span>

              <div
                className={`w-[72px] h-[72px] ${step.bg} rounded-md grid place-items-center`}
              >
                {step.icon}
              </div>
              <h3 className="text-base font-extrabold text-green-900">
                {step.title}
              </h3>
              <p className="text-[0.86rem] text-green-700/60 leading-relaxed">
                {step.desc}
              </p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}
