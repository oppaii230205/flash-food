import {
  useState,
  useEffect,
  createContext,
  useContext,
  type ReactNode,
} from "react";
import { NavLink, Outlet, useNavigate, useLocation } from "react-router-dom";
import {
  House,
  Storefront,
  Lightning,
  ClipboardText,
  List,
  X,
  SignOut,
  Leaf,
  CaretLeft,
  CaretRight,
  User,
} from "@phosphor-icons/react";
import { motion, AnimatePresence } from "framer-motion";
import { useAuthStore } from "@/store/authStore";
import { authApi } from "@/api/auth.api";
import { cn } from "@/utils/cn";

// ─── Sidebar context ──────────────────────────────────────────────────────────
interface SidebarCtx {
  collapsed: boolean;
  mobileOpen: boolean;
  toggleCollapse: () => void;
  toggleMobile: () => void;
}
const Sidebar = createContext<SidebarCtx>({
  collapsed: false,
  mobileOpen: false,
  toggleCollapse: () => {},
  toggleMobile: () => {},
});
const useSidebar = () => useContext(Sidebar);

// ─── Nav items ────────────────────────────────────────────────────────────────
const NAV = [
  { label: "Dashboard", icon: House, href: "/store" },
  { label: "Food Items", icon: Lightning, href: "/store/food-items" },
  { label: "Orders", icon: ClipboardText, href: "/store/orders" },
  { label: "My Store", icon: Storefront, href: "/store/settings" },
];

// ─── Sidebar component ────────────────────────────────────────────────────────
function SidebarPanel() {
  const { collapsed, mobileOpen, toggleCollapse, toggleMobile } = useSidebar();
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch {
      // ignore
    } finally {
      logout();
      navigate("/");
    }
  };

  const navContent = (
    <nav className="flex flex-col h-full">
      {/* Logo */}
      <div
        className={cn(
          "flex items-center gap-2.5 px-4 py-5 border-b border-white/10",
          collapsed && "justify-center px-0",
        )}
      >
        <span className="w-8 h-8 bg-gradient-to-br from-green-400 to-green-500 rounded-lg grid place-items-center shrink-0">
          <Leaf size={16} weight="bold" className="text-white" />
        </span>
        {!collapsed && (
          <span className="font-extrabold text-white text-base leading-tight">
            Flash Food
            <span className="block text-[0.65rem] font-medium text-green-300/80 leading-none mt-0.5">
              Store Portal
            </span>
          </span>
        )}
      </div>

      {/* Nav links */}
      <div className="flex-1 overflow-y-auto py-3 space-y-0.5 px-2">
        {NAV.map(({ label, icon: Icon, href }) => {
          const isActive =
            href === "/store"
              ? location.pathname === "/store"
              : location.pathname.startsWith(href);

          return (
            <NavLink
              key={href}
              to={href}
              className={cn(
                "flex items-center gap-3 rounded-lg px-3 py-2.5 text-[0.85rem] font-semibold transition-colors relative group",
                isActive
                  ? "bg-white/12 text-white"
                  : "text-green-200/70 hover:bg-white/6 hover:text-white",
                collapsed && "justify-center px-0",
              )}
              title={collapsed ? label : undefined}
            >
              {isActive && (
                <span className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-5 bg-green-400 rounded-r-full" />
              )}
              <Icon
                size={18}
                weight={isActive ? "fill" : "regular"}
                className="shrink-0"
              />
              {!collapsed && <span>{label}</span>}
            </NavLink>
          );
        })}
      </div>

      {/* User footer */}
      <div
        className={cn(
          "border-t border-white/10 p-3",
          collapsed && "flex justify-center",
        )}
      >
        {collapsed ? (
          <button
            onClick={handleLogout}
            title="Sign Out"
            className="w-9 h-9 rounded-lg grid place-items-center text-green-200/60 hover:bg-white/10 hover:text-white transition-colors"
          >
            <SignOut size={18} weight="bold" />
          </button>
        ) : (
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-green-700 grid place-items-center text-sm font-extrabold text-white shrink-0">
              {user?.fullName?.[0]?.toUpperCase() ?? <User size={14} />}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-white text-[0.82rem] font-semibold truncate">
                {user?.fullName ?? "Store Owner"}
              </p>
              <p className="text-green-300/60 text-[0.68rem] truncate">
                {user?.email}
              </p>
            </div>
            <button
              onClick={handleLogout}
              title="Sign Out"
              className="w-7 h-7 rounded-md grid place-items-center text-green-200/50 hover:bg-white/10 hover:text-white transition-colors shrink-0"
            >
              <SignOut size={15} weight="bold" />
            </button>
          </div>
        )}
      </div>
    </nav>
  );

  return (
    <>
      {/* Desktop sidebar */}
      <aside
        className={cn(
          "hidden md:flex flex-col bg-green-950 shrink-0 relative transition-all duration-200 ease-in-out",
          collapsed ? "w-[4.25rem]" : "w-60",
        )}
      >
        {navContent}

        {/* Desktop collapse toggle */}
        <button
          onClick={toggleCollapse}
          className="absolute -right-3 top-[72px] w-6 h-6 rounded-full bg-green-800 border border-green-700 grid place-items-center text-green-300 hover:text-white hover:bg-green-700 transition-colors shadow-md z-10"
          title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
        >
          {collapsed ? (
            <CaretRight size={11} weight="bold" />
          ) : (
            <CaretLeft size={11} weight="bold" />
          )}
        </button>
      </aside>

      {/* Mobile drawer */}
      <AnimatePresence>
        {mobileOpen && (
          <>
            <motion.div
              key="backdrop"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="md:hidden fixed inset-0 bg-black/50 z-40"
              onClick={toggleMobile}
            />
            <motion.aside
              key="drawer"
              initial={{ x: "-100%" }}
              animate={{ x: 0 }}
              exit={{ x: "-100%" }}
              transition={{ type: "spring", stiffness: 300, damping: 30 }}
              className="md:hidden fixed inset-y-0 left-0 w-60 bg-green-950 z-50 flex flex-col"
            >
              <button
                onClick={toggleMobile}
                className="absolute top-4 right-4 w-7 h-7 rounded-full bg-white/10 grid place-items-center text-green-200 hover:bg-white/20 transition-colors"
              >
                <X size={14} weight="bold" />
              </button>
              {navContent}
            </motion.aside>
          </>
        )}
      </AnimatePresence>
    </>
  );
}

// ─── Top header ───────────────────────────────────────────────────────────────
function TopHeader({ title }: { title: string }) {
  const { toggleMobile } = useSidebar();

  return (
    <header className="h-14 bg-white border-b border-gray-100 flex items-center gap-3 px-4 shrink-0">
      <button
        onClick={toggleMobile}
        className="md:hidden w-8 h-8 rounded-lg grid place-items-center text-gray-500 hover:bg-gray-100 transition-colors"
      >
        <List size={20} weight="bold" />
      </button>
      <h1 className="font-extrabold text-green-900 text-[0.95rem]">{title}</h1>
    </header>
  );
}

// ─── Page title mapping ───────────────────────────────────────────────────────
function usePageTitle(): string {
  const { pathname } = useLocation();
  if (pathname === "/store") return "Dashboard";
  if (pathname.startsWith("/store/food-items")) return "Food Items";
  if (pathname.startsWith("/store/orders")) return "Orders";
  if (pathname.startsWith("/store/settings")) return "My Store";
  return "Store Portal";
}

// ─── Root layout ──────────────────────────────────────────────────────────────
export function StoreDashboardLayout({ children }: { children?: ReactNode }) {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const title = usePageTitle();

  // Close mobile menu on resize to desktop
  useEffect(() => {
    const handler = () => {
      if (window.innerWidth >= 768) setMobileOpen(false);
    };
    window.addEventListener("resize", handler);
    return () => window.removeEventListener("resize", handler);
  }, []);

  return (
    <Sidebar.Provider
      value={{
        collapsed,
        mobileOpen,
        toggleCollapse: () => setCollapsed((v) => !v),
        toggleMobile: () => setMobileOpen((v) => !v),
      }}
    >
      <div className="flex h-screen overflow-hidden bg-gray-50">
        <SidebarPanel />

        <div className="flex-1 flex flex-col overflow-hidden min-w-0">
          <TopHeader title={title} />
          <main className="flex-1 overflow-y-auto">
            {children ?? <Outlet />}
          </main>
        </div>
      </div>
    </Sidebar.Provider>
  );
}
