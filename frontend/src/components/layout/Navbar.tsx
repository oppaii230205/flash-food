import { useState, useEffect } from "react";
import { Link, useLocation } from "react-router-dom";
import {
  List,
  X,
  Leaf,
  CaretDown,
  SignOut,
  User,
  ShoppingBag,
} from "@phosphor-icons/react";
import { motion, AnimatePresence } from "framer-motion";
import { useAuthStore } from "@/store/authStore";
import { Button } from "@/components/ui/Button";
import { cn } from "@/utils/cn";

const navLinks = [
  { label: "Deals", href: "/deals" },
  { label: "How It Works", href: "/#how" },
  { label: "Impact", href: "/#impact" },
  { label: "Community", href: "/#community" },
];

interface NavbarProps {
  onLoginClick?: () => void;
  onSignupClick?: () => void;
}

export function Navbar({ onLoginClick, onSignupClick }: NavbarProps = {}) {
  const { isAuthenticated, user, logout } = useAuthStore();
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const location = useLocation();

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 50);
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  // Close mobile menu on route change
  useEffect(() => {
    setMenuOpen(false);
  }, [location]);

  return (
    <>
      <nav
        className={cn(
          "fixed top-0 left-0 right-0 z-50 py-3.5 transition-all duration-300",
          scrolled || menuOpen
            ? "bg-beige-50/96 backdrop-blur-md shadow-sm"
            : "bg-transparent",
        )}
        role="navigation"
      >
        <div className="max-w-6xl mx-auto px-5 flex items-center justify-between">
          {/* Logo */}
          <Link
            to="/"
            className="flex items-center gap-2 font-extrabold text-xl text-green-700"
          >
            <span className="w-9 h-9 bg-gradient-to-br from-green-500 to-mint-400 rounded-[10px] grid place-items-center text-white text-base">
              <Leaf size={18} weight="bold" />
            </span>
            Flash Food
          </Link>

          {/* Desktop links */}
          <ul className="hidden md:flex items-center gap-7">
            {navLinks.map((l) => (
              <li key={l.href}>
                <Link
                  to={l.href}
                  className="text-sm font-semibold text-green-800/80 hover:text-green-600 transition-colors"
                >
                  {l.label}
                </Link>
              </li>
            ))}
          </ul>

          {/* Desktop CTA / user menu */}
          <div className="hidden md:flex items-center gap-3">
            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center gap-2 bg-green-50 border border-green-200 rounded-full px-3 py-2 text-sm font-semibold text-green-700 hover:bg-green-100 transition-colors"
                >
                  <span className="w-6 h-6 rounded-full bg-green-200 grid place-items-center text-xs font-extrabold text-green-800">
                    {user?.fullName?.[0]?.toUpperCase() ?? "U"}
                  </span>
                  {user?.fullName?.split(" ")[0]}
                  <CaretDown
                    size={14}
                    weight="bold"
                    className={cn(
                      "transition-transform",
                      userMenuOpen && "rotate-180",
                    )}
                  />
                </button>

                <AnimatePresence>
                  {userMenuOpen && (
                    <motion.div
                      initial={{ opacity: 0, y: 8, scale: 0.97 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, y: 8, scale: 0.97 }}
                      transition={{ duration: 0.18 }}
                      className="absolute right-0 top-full mt-2 w-48 bg-white rounded-md border border-green-100 shadow-md py-1.5"
                    >
                      <Link
                        to="/profile"
                        className="flex items-center gap-2.5 px-4 py-2 text-sm text-green-800 hover:bg-green-50 font-semibold"
                      >
                        <User size={15} weight="bold" /> My Profile
                      </Link>
                      <Link
                        to="/orders"
                        className="flex items-center gap-2.5 px-4 py-2 text-sm text-green-800 hover:bg-green-50 font-semibold"
                      >
                        <ShoppingBag size={15} weight="bold" /> My Orders
                      </Link>
                      <hr className="my-1 border-green-100" />
                      <button
                        onClick={() => {
                          logout();
                          setUserMenuOpen(false);
                        }}
                        className="flex items-center gap-2.5 px-4 py-2 text-sm text-red-600 hover:bg-red-50 font-semibold w-full text-left"
                      >
                        <SignOut size={15} weight="bold" /> Sign Out
                      </button>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            ) : (
              <>
                <Button variant="secondary" size="sm" onClick={onLoginClick}>
                  Sign In
                </Button>
                <Button size="sm" onClick={onSignupClick}>
                  Join Free ↗
                </Button>
              </>
            )}
          </div>

          {/* Hamburger */}
          <button
            className="md:hidden p-1.5 text-green-700"
            onClick={() => setMenuOpen(!menuOpen)}
            aria-label="Toggle menu"
          >
            {menuOpen ? (
              <X size={24} weight="bold" />
            ) : (
              <List size={24} weight="bold" />
            )}
          </button>
        </div>
      </nav>

      {/* Mobile menu */}
      <AnimatePresence>
        {menuOpen && (
          <motion.div
            key="mobile-menu"
            initial={{ opacity: 0, y: -12 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -12 }}
            transition={{ duration: 0.22 }}
            className="fixed top-[60px] left-0 right-0 z-40 bg-beige-50/98 backdrop-blur-md border-b border-green-100 px-5 py-4 flex flex-col gap-1"
          >
            {navLinks.map((l) => (
              <Link
                key={l.href}
                to={l.href}
                className="py-3 px-4 font-semibold text-green-800 rounded-[10px] hover:bg-green-50 transition-colors"
              >
                {l.label}
              </Link>
            ))}
            <div className="pt-2 border-t border-green-100 mt-1 flex gap-2">
              {isAuthenticated ? (
                <>
                  <Link to="/orders" className="flex-1">
                    <Button variant="secondary" size="sm" className="w-full">
                      My Orders
                    </Button>
                  </Link>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={logout}
                    className="flex-1"
                  >
                    Sign Out
                  </Button>
                </>
              ) : (
                <>
                  <Button
                    variant="secondary"
                    size="sm"
                    className="flex-1"
                    onClick={() => {
                      setMenuOpen(false);
                      onLoginClick?.();
                    }}
                  >
                    Sign In
                  </Button>
                  <Button
                    size="sm"
                    className="flex-1"
                    onClick={() => {
                      setMenuOpen(false);
                      onSignupClick?.();
                    }}
                  >
                    Join Free ↗
                  </Button>
                </>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
