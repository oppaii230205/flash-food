import { useState } from "react";
import { Link } from "react-router-dom";
import {
  Heart,
  Lightning,
  Storefront,
  Leaf,
  Bread,
  Coffee,
  ForkKnife,
} from "@phosphor-icons/react";
import { motion } from "framer-motion";
import { useScrollReveal } from "@/hooks/useScrollReveal";
import { CountdownTimer } from "@/components/ui/CountdownTimer";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { SectionLabel } from "@/components/ui/SectionLabel";
import { useSavedDealsStore } from "@/store/savedDealsStore";
import toast from "react-hot-toast";

// Static preview data (replaced by real API on /deals page)
const PREVIEW_DEALS = [
  {
    id: 1,
    emoji: "🥗",
    bg: "#f0faf3",
    badge: "71% OFF",
    qty: 3,
    store: "Green Leaf Café",
    name: "Garden Salad Bag",
    desc: "Mixed greens, grilled vegetables, quinoa & 2 dressings.",
    price: 3.5,
    originalPrice: 12.0,
    category: "cafe",
  },
  {
    id: 2,
    emoji: "🥐",
    bg: "#fff3ee",
    badge: "70% OFF",
    qty: 5,
    store: "Sunrise Bakery",
    name: "Pastry Rescue Bundle",
    desc: "Assorted croissants, muffins & danish pastries.",
    price: 4.2,
    originalPrice: 14.0,
    category: "bakery",
  },
  {
    id: 3,
    emoji: "🍱",
    bg: "#f3e9d8",
    badge: "65% OFF",
    qty: 2,
    store: "Sakura Kitchen",
    name: "Bento Dinner Box",
    desc: "Rice, teriyaki protein, pickled veggies & miso soup.",
    price: 5.25,
    originalPrice: 15.0,
    category: "restaurant",
  },
  {
    id: 4,
    emoji: "🍕",
    bg: "#fff8f0",
    badge: "68% OFF",
    qty: 4,
    store: "Slice of Life",
    name: "Pizza Rescue Pack",
    desc: "4 slices of assorted wood-fired pizzas.",
    price: 4.8,
    originalPrice: 15.0,
    category: "restaurant",
  },
  {
    id: 5,
    emoji: "☕",
    bg: "#d4f4ee",
    badge: "60% OFF",
    qty: 6,
    store: "Morning Grounds",
    name: "Café Snack Pack",
    desc: "Granola bar, banana bread, trail mix + drink voucher.",
    price: 2.8,
    originalPrice: 7.0,
    category: "cafe",
  },
  {
    id: 6,
    emoji: "🍜",
    bg: "#f0f8ff",
    badge: "72% OFF",
    qty: 1,
    store: "Noodle House",
    name: "Ramen Surprise Bowl",
    desc: "Chef's noodle bowl with broth, toppings & a side.",
    price: 3.9,
    originalPrice: 14.0,
    category: "restaurant",
  },
];

const FILTERS = [
  { label: "All", icon: null, value: "all" },
  { label: "Bakery", icon: <Bread size={13} weight="bold" />, value: "bakery" },
  { label: "Café", icon: <Coffee size={13} weight="bold" />, value: "cafe" },
  {
    label: "Restaurant",
    icon: <ForkKnife size={13} weight="bold" />,
    value: "restaurant",
  },
];

function DealCard({ deal }: { deal: (typeof PREVIEW_DEALS)[0] }) {
  const { toggle, isSaved } = useSavedDealsStore();
  const saved = isSaved(deal.id);

  return (
    <motion.div
      whileHover={{ y: -4, boxShadow: "0 8px 40px rgba(52,120,80,.18)" }}
      transition={{ duration: 0.22 }}
      className="bg-beige-50 rounded-lg border border-green-100 overflow-hidden cursor-pointer"
    >
      {/* Thumbnail */}
      <div
        className="h-40 relative flex items-center justify-center text-6xl"
        style={{ background: deal.bg }}
      >
        {deal.emoji}
        <Badge variant="discount" className="absolute top-3 left-3">
          {deal.badge}
        </Badge>
        <Badge
          variant="quantity"
          className="absolute top-3 right-3 inline-flex items-center gap-1"
        >
          <Lightning size={10} weight="fill" /> {deal.qty} left
        </Badge>
      </div>

      <div className="p-4">
        <div className="text-[0.72rem] text-green-600 font-semibold mb-1 inline-flex items-center gap-1">
          <Storefront size={12} weight="bold" /> {deal.store}
        </div>
        <div className="text-[1rem] font-extrabold text-green-900 mb-1.5">
          {deal.name}
        </div>
        <p className="text-[0.78rem] text-green-700/60 mb-3 leading-relaxed">
          {deal.desc}
        </p>

        <div className="flex items-center justify-between mb-3">
          <div className="flex items-baseline gap-1.5">
            <span className="text-xl font-extrabold text-green-600">
              ${deal.price.toFixed(2)}
            </span>
            <span className="text-[0.8rem] text-green-400/70 line-through">
              ${deal.originalPrice.toFixed(2)}
            </span>
          </div>
          <div className="text-[0.72rem] font-bold bg-[#fff3ee] text-orange-500 rounded-full px-2.5 py-1">
            <CountdownTimer compact className="text-[0.72rem]" />
          </div>
        </div>

        <div className="flex gap-2">
          <Link to="/deals" className="flex-1">
            <Button
              size="sm"
              className="w-full text-[0.82rem] inline-flex items-center justify-center gap-1.5"
            >
              <Leaf size={14} weight="fill" /> Rescue This Meal
            </Button>
          </Link>
          <button
            onClick={() => {
              toggle(deal.id);
              toast.success(
                saved ? "Removed from saved" : "💚 Saved for later!",
              );
            }}
            className="w-9 h-9 flex-shrink-0 bg-white border-[1.5px] border-green-200 rounded-full grid place-items-center hover:bg-green-50 hover:border-green-400 transition-colors"
          >
            <Heart
              size={15}
              weight={saved ? "fill" : "regular"}
              className={saved ? "text-green-500" : "text-green-400"}
            />
          </button>
        </div>
      </div>
    </motion.div>
  );
}

export function DealsPreview() {
  const { ref, isVisible } = useScrollReveal<HTMLElement>();
  const [activeFilter, setActiveFilter] = useState("all");

  const filtered =
    activeFilter === "all"
      ? PREVIEW_DEALS
      : PREVIEW_DEALS.filter((d) => d.category === activeFilter);

  return (
    <section ref={ref} id="deals" className="py-20 bg-white">
      <div className="max-w-6xl mx-auto px-5">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-5 mb-9">
          <div>
            <motion.div
              initial={{ opacity: 0, y: 28 }}
              animate={isVisible ? { opacity: 1, y: 0 } : {}}
              transition={{ duration: 0.7 }}
            >
              <SectionLabel>
                <Lightning size={12} weight="fill" /> Tonight's Rescue Deals
              </SectionLabel>
              <h2 className="text-[clamp(1.6rem,4vw,2.4rem)] font-extrabold text-green-900 leading-snug">
                Surplus food. Real savings.
                <br />
                Ready for pickup.
              </h2>
              <p className="text-green-700/60 text-sm mt-2 max-w-md">
                Deals refresh daily at 9 PM. Quantities are limited — rescue
                yours before they're gone.
              </p>
            </motion.div>
          </div>

          <div className="flex gap-2 flex-wrap">
            {FILTERS.map((f) => (
              <button
                key={f.value}
                onClick={() => setActiveFilter(f.value)}
                className={`inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full text-[0.82rem] font-semibold border transition-all duration-200 ${
                  activeFilter === f.value
                    ? "bg-green-600 border-green-600 text-white"
                    : "bg-transparent border-green-200 text-green-600 hover:bg-green-600 hover:border-green-600 hover:text-white"
                }`}
              >
                {f.icon}
                {f.label}
              </button>
            ))}
          </div>
        </div>

        {/* Grid */}
        <motion.div
          key={activeFilter}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3 }}
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5"
        >
          {filtered.map((deal) => (
            <DealCard key={deal.id} deal={deal} />
          ))}
        </motion.div>

        <div className="text-center mt-10">
          <Link to="/deals">
            <Button variant="secondary">See All Live Deals →</Button>
          </Link>
        </div>
      </div>
    </section>
  );
}
