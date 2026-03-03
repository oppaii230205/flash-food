import { Link } from "react-router-dom";
import {
  Leaf,
  InstagramLogo,
  XLogo,
  TiktokLogo,
  LinkedinLogo,
  Heart,
  Plant,
  Recycle,
  Trophy,
} from "@phosphor-icons/react";

const footerCols = [
  {
    title: "Product",
    links: [
      { label: "How It Works", href: "/#how" },
      { label: "Tonight's Deals", href: "/deals" },
      { label: "For Businesses", href: "/#business" },
      { label: "Partner Stores", href: "/#stores" },
    ],
  },
  {
    title: "Company",
    links: [
      { label: "About Us", href: "/#about" },
      { label: "Our Mission", href: "/#mission" },
      { label: "Impact Report", href: "/#impact" },
      { label: "Blog", href: "/#blog" },
      { label: "Careers", href: "/#careers" },
    ],
  },
  {
    title: "Support",
    links: [
      { label: "Help Center", href: "/#help" },
      { label: "Contact Us", href: "/#contact" },
      { label: "Privacy Policy", href: "/#privacy" },
      { label: "Terms of Service", href: "/#terms" },
    ],
  },
];

export function Footer() {
  return (
    <footer className="bg-green-900 text-green-300 pt-12 pb-7">
      <div className="max-w-6xl mx-auto px-5">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-9 mb-10">
          {/* Brand */}
          <div>
            <Link
              to="/"
              className="flex items-center gap-2 font-extrabold text-xl text-green-300 mb-3"
            >
              <span className="w-9 h-9 bg-gradient-to-br from-green-500 to-mint-400 rounded-[10px] grid place-items-center text-white">
                <Leaf size={18} weight="bold" />
              </span>
              Flash Food
            </Link>
            <p className="text-green-400 text-sm leading-relaxed max-w-[220px]">
              A real-time food rescue marketplace connecting surplus meals with
              conscious eaters.
            </p>
            <div className="flex gap-2.5 mt-4">
              {[
                { Icon: InstagramLogo, label: "Instagram" },
                { Icon: XLogo, label: "X / Twitter" },
                { Icon: TiktokLogo, label: "TikTok" },
                { Icon: LinkedinLogo, label: "LinkedIn" },
              ].map(({ Icon, label }) => (
                <a
                  key={label}
                  href="#"
                  aria-label={label}
                  className="w-8 h-8 rounded-lg bg-white/8 hover:bg-white/16 grid place-items-center transition-colors text-green-300 hover:text-white"
                >
                  <Icon size={16} weight="bold" />
                </a>
              ))}
            </div>
          </div>

          {/* Columns */}
          {footerCols.map((col) => (
            <div key={col.title}>
              <h4 className="text-white text-[0.78rem] font-extrabold uppercase tracking-widest mb-3.5">
                {col.title}
              </h4>
              <ul className="flex flex-col gap-2">
                {col.links.map((l) => (
                  <li key={l.label}>
                    <Link
                      to={l.href}
                      className="text-green-400 text-[0.84rem] hover:text-green-200 transition-colors"
                    >
                      {l.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="border-t border-white/8 pt-6 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-green-500">
          <span className="inline-flex items-center gap-1">
            &copy; 2026 Flash Food Inc. · Made with{" "}
            <Heart size={12} weight="fill" className="text-green-400" /> for the
            planet
          </span>
          <div className="flex gap-2.5 flex-wrap justify-center">
            {[
              {
                icon: <Plant size={11} weight="fill" />,
                label: "Carbon Neutral",
              },
              { icon: <Recycle size={11} weight="fill" />, label: "B-Corp" },
              {
                icon: <Trophy size={11} weight="fill" />,
                label: "2025 Sustainability Award",
              },
            ].map(({ icon, label }) => (
              <span
                key={label}
                className="inline-flex items-center gap-1 bg-white/7 rounded-full px-2.5 py-0.5 text-[0.7rem]"
              >
                {icon} {label}
              </span>
            ))}
          </div>
        </div>
      </div>
    </footer>
  );
}
