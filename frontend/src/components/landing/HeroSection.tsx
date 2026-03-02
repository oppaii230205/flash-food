import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import {
  Leaf,
  ForkKnife,
  Heart,
  Fire,
  MapPin,
  Bell,
  MagnifyingGlass,
  Lightning,
  Storefront,
  Clock,
  BatteryFull,
} from "@phosphor-icons/react";
import { useCountdown } from "@/hooks/useCountdown";
import { useAuthStore } from "@/store/authStore";
import { Button } from "@/components/ui/Button";

const fadeDown = (delay = 0) => ({
  initial: { opacity: 0, y: -20 },
  animate: { opacity: 1, y: 0 },
  transition: { duration: 0.7, delay, ease: "easeOut" },
});

const fadeUp = (delay = 0) => ({
  initial: { opacity: 0, y: 30 },
  animate: { opacity: 1, y: 0 },
  transition: { duration: 0.9, delay, ease: "easeOut" },
});

const AVATAR_INITIALS = ["M", "J", "S", "P"];
const AVATAR_COLORS = ["#52b788", "#2d6a4f", "#80ded9", "#c9a07a"];

export function HeroSection() {
  const { formatted } = useCountdown();
  const { isAuthenticated } = useAuthStore();

  return (
    <section
      id="home"
      className="relative min-h-svh flex items-center pt-28 pb-20 overflow-hidden"
      style={{
        background:
          "radial-gradient(ellipse 80% 60% at 20% 0%, #d8f3dc 0%, transparent 60%)," +
          "radial-gradient(ellipse 60% 50% at 80% 100%, #d4f4ee 0%, transparent 55%)," +
          "#fdfaf5",
      }}
    >
      {/* Floating background orb */}
      <div
        className="absolute w-[600px] h-[600px] -top-48 -right-48 rounded-full pointer-events-none animate-float-bg"
        style={{
          background: "radial-gradient(circle, #d8f3dc 0%, transparent 70%)",
        }}
      />

      <div className="max-w-6xl mx-auto px-5 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-14 items-center">
          {/* ── Left: copy ── */}
          <div>
            {/* Badge */}
            <motion.div {...fadeDown(0)} className="w-fit mb-5">
              <span className="inline-flex items-center gap-2 bg-gradient-to-r from-green-50 to-mint-100 border border-green-200 rounded-full pl-2 pr-4 py-1.5 text-[0.78rem] font-extrabold text-green-700">
                <span className="w-6 h-6 bg-green-500 rounded-full grid place-items-center text-white animate-pulse-ring">
                  <Leaf size={13} weight="fill" />
                </span>
                New deals drop every evening at 9 PM
              </span>
            </motion.div>

            {/* Headline */}
            <motion.h1
              {...fadeDown(0.1)}
              className="text-[clamp(2rem,5.5vw,3.6rem)] font-extrabold text-green-900 leading-[1.15]"
            >
              Rescue Food.
              <br />
              <span className="text-green-500 relative">
                Save Money.
                <span
                  className="absolute bottom-0.5 left-0 right-0 h-1 rounded opacity-40"
                  style={{
                    background: "linear-gradient(90deg,#74c69d,#80ded9)",
                  }}
                />
              </span>
              <br />
              Protect the Planet.
            </motion.h1>

            <motion.p
              {...fadeDown(0.2)}
              className="mt-4 text-base text-green-700/70 max-w-[480px] leading-relaxed"
            >
              Flash Food connects you with surplus meals from local restaurants
              and bakeries — up to{" "}
              <strong className="text-green-700">70% off</strong>, pickup
              between 9–10 PM, before food goes to waste.
            </motion.p>

            <motion.div
              {...fadeDown(0.3)}
              className="flex flex-wrap gap-3 mt-7"
            >
              <Link to="/deals">
                <Button size="lg" className="inline-flex items-center gap-2">
                  <ForkKnife size={18} weight="bold" /> View Tonight's Deals
                </Button>
              </Link>
              <Link to="/#how">
                <Button variant="secondary" size="lg">
                  How it works →
                </Button>
              </Link>
            </motion.div>

            {/* Trust signal */}
            <motion.div
              {...fadeDown(0.4)}
              className="flex items-center gap-3 mt-6"
            >
              <div className="flex">
                {AVATAR_INITIALS.map((letter, i) => (
                  <span
                    key={i}
                    className="w-8 h-8 rounded-full border-[2.5px] border-white grid place-items-center text-xs font-extrabold text-white"
                    style={{
                      marginLeft: i === 0 ? 0 : "-10px",
                      background: AVATAR_COLORS[i],
                    }}
                  >
                    {letter}
                  </span>
                ))}
              </div>
              <p className="text-[0.78rem] text-green-700/70 font-medium">
                <strong className="text-green-700">12,400+</strong> meals
                rescued this month by neighbors like you
              </p>
            </motion.div>

            {/* Auth welcome banner */}
            {isAuthenticated && (
              <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                className="mt-5 bg-green-50 border border-green-200 rounded-md px-4 py-3 flex items-center justify-between gap-3"
              >
                <span className="text-sm font-semibold text-green-700 inline-flex items-center gap-1.5">
                  <Leaf size={14} weight="fill" /> Welcome back! Deals are live
                  tonight.
                </span>
                <Link to="/deals">
                  <Button size="sm">Go to Deals ↗</Button>
                </Link>
              </motion.div>
            )}
          </div>

          {/* ── Right: phone mockup ── */}
          <motion.div
            {...fadeUp(0.2)}
            className="flex justify-center items-center"
          >
            <div className="relative inline-block">
              {/* Left float badge */}
              <motion.div
                animate={{ y: [0, -8, 0] }}
                transition={{
                  duration: 4,
                  repeat: Infinity,
                  ease: "easeInOut",
                }}
                className="absolute -left-5 top-[30%] z-10 bg-white rounded-md shadow-md px-3 py-2 flex items-center gap-2 text-[0.72rem] font-bold text-green-700"
              >
                <span className="w-7 h-7 bg-green-50 rounded-lg grid place-items-center">
                  <Heart size={16} weight="fill" className="text-green-500" />
                </span>
                <div>
                  <div className="text-[0.72rem] font-extrabold">
                    Meal Rescued!
                  </div>
                  <div className="text-[0.63rem] text-green-600/70 font-medium">
                    Saved 1.2 kg of waste
                  </div>
                </div>
              </motion.div>

              {/* Phone */}
              <div className="w-[min(280px,85vw)] bg-white rounded-[36px] shadow-lg border-[8px] border-white outline outline-2 outline-green-100">
                {/* Status bar */}
                <div className="bg-green-900 px-5 py-2 flex items-center justify-between text-[0.6rem] text-white font-semibold rounded-t-[28px]">
                  <span>9:12 PM</span>
                  <span>●●●</span>
                  <BatteryFull size={14} weight="fill" />
                </div>
                {/* Screen */}
                <div className="bg-beige-50 p-3.5 rounded-b-[28px]">
                  <div className="flex items-center justify-between mb-3.5">
                    <div className="flex items-center gap-1 text-[0.7rem] font-bold text-green-700">
                      <MapPin size={11} weight="fill" /> Midtown, NYC
                    </div>
                    <div className="w-7 h-7 bg-green-100 rounded-lg grid place-items-center">
                      <Bell
                        size={14}
                        weight="fill"
                        className="text-green-700"
                      />
                    </div>
                  </div>
                  <div className="bg-white rounded-full px-3 py-2 text-[0.68rem] text-green-400 flex items-center gap-1.5 border border-green-100 mb-3.5">
                    <MagnifyingGlass size={11} weight="bold" /> Search deals
                    near you...
                  </div>
                  <div className="text-[0.65rem] font-extrabold text-green-900 uppercase tracking-wider mb-2 flex items-center gap-1">
                    <Lightning
                      size={10}
                      weight="fill"
                      className="text-green-600"
                    />{" "}
                    Flash Deals Tonight
                  </div>

                  {[
                    {
                      emoji: "🥗",
                      bg: "#f0faf3",
                      name: "Fresh Salad Bowl",
                      store: "Green Leaf Café",
                      price: "$3.50",
                      old: "$12.00",
                    },
                    {
                      emoji: "🥐",
                      bg: "#fff3ee",
                      name: "Pastry Bundle",
                      store: "Sunrise Bakery",
                      price: "$4.00",
                      old: "$14.00",
                    },
                  ].map((item) => (
                    <div
                      key={item.name}
                      className="bg-white rounded-[10px] p-2.5 mb-2 flex items-center gap-2.5 shadow-xs"
                    >
                      <div
                        className="w-12 h-12 rounded-lg grid place-items-center text-2xl flex-shrink-0"
                        style={{ background: item.bg }}
                      >
                        {item.emoji}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="text-[0.7rem] font-bold text-green-900 truncate">
                          {item.name}
                        </div>
                        <div className="text-[0.6rem] text-green-600/70 flex items-center gap-0.5">
                          <Storefront size={9} weight="fill" /> {item.store}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-[0.7rem] font-extrabold text-green-600">
                          {item.price}
                        </div>
                        <div className="text-[0.6rem] text-green-400/70 line-through">
                          {item.old}
                        </div>
                      </div>
                    </div>
                  ))}

                  {/* Countdown */}
                  <div
                    className="rounded-[10px] p-2.5 mt-2 flex items-center justify-between"
                    style={{
                      background: "linear-gradient(135deg,#1a3d2b,#2d6a4f)",
                    }}
                  >
                    <div className="text-[0.6rem] text-green-200 font-semibold flex items-center gap-1">
                      <Clock size={10} weight="fill" /> Pickup window closes in
                    </div>
                    <div className="text-[0.88rem] text-white font-extrabold tabular-nums">
                      {formatted}
                    </div>
                  </div>
                </div>
              </div>

              {/* Right float badge */}
              <motion.div
                animate={{ y: [0, -8, 0] }}
                transition={{
                  duration: 4,
                  delay: 1.5,
                  repeat: Infinity,
                  ease: "easeInOut",
                }}
                className="absolute -right-5 bottom-[28%] z-10 bg-white rounded-md shadow-md px-3 py-2 flex items-center gap-2 text-[0.72rem] font-bold text-orange-500"
              >
                <span className="w-7 h-7 bg-[#fff3ee] rounded-lg grid place-items-center">
                  <Fire size={16} weight="fill" className="text-orange-400" />
                </span>
                <div>
                  <div className="text-[0.72rem] font-extrabold">
                    Only 2 left!
                  </div>
                  <div className="text-[0.63rem] text-orange-400/80 font-medium">
                    70% off tonight
                  </div>
                </div>
              </motion.div>
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
}
