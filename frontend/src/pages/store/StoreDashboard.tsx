import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import {
  Storefront,
  Lightning,
  ClipboardText,
  Plus,
  ArrowRight,
  Warning,
  SmileyMeh,
} from "@phosphor-icons/react";
import { useAuthStore } from "@/store/authStore";
import { storesApi } from "@/api/stores.api";
import { dealsApi } from "@/api/deals.api";
import { cn } from "@/utils/cn";
import type { StoreResponse, FoodItemResponse } from "@/types";

// ─── helpers ─────────────────────────────────────────────────────────────────
function StatCard({
  label,
  value,
  sub,
  icon,
  color,
  loading,
}: {
  label: string;
  value: string | number;
  sub?: string;
  icon: React.ReactNode;
  color: string;
  loading?: boolean;
}) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 p-5 shadow-sm flex items-start gap-4">
      <div
        className={cn(
          "w-11 h-11 rounded-xl grid place-items-center shrink-0",
          color,
        )}
      >
        {icon}
      </div>
      <div className="min-w-0">
        <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">
          {label}
        </p>
        {loading ? (
          <div className="h-7 w-16 bg-gray-100 rounded animate-pulse mt-1" />
        ) : (
          <p className="text-2xl font-extrabold text-gray-900 leading-tight mt-0.5">
            {value}
          </p>
        )}
        {sub && <p className="text-xs text-gray-400 mt-0.5 truncate">{sub}</p>}
      </div>
    </div>
  );
}

function StatusBadge({ status }: { status: string }) {
  const map: Record<string, string> = {
    ACTIVE: "bg-green-50 text-green-700 border-green-200",
    PENDING_APPROVAL: "bg-amber-50 text-amber-700 border-amber-200",
    INACTIVE: "bg-gray-100 text-gray-500 border-gray-200",
    SUSPENDED: "bg-red-50 text-red-600 border-red-200",
    REJECTED: "bg-red-50 text-red-600 border-red-200",
  };
  const label: Record<string, string> = {
    ACTIVE: "Active",
    PENDING_APPROVAL: "Pending Approval",
    INACTIVE: "Inactive",
    SUSPENDED: "Suspended",
    REJECTED: "Rejected",
  };
  return (
    <span
      className={cn(
        "inline-flex items-center px-2 py-0.5 rounded-full text-[0.7rem] font-semibold border",
        map[status] ?? "bg-gray-100 text-gray-500 border-gray-200",
      )}
    >
      {label[status] ?? status}
    </span>
  );
}

// ─── Empty state ─────────────────────────────────────────────────────────────
function NoStoreState() {
  return (
    <div className="flex flex-col items-center justify-center py-24 text-center px-4">
      <div className="w-16 h-16 bg-green-50 rounded-2xl grid place-items-center mb-5">
        <Storefront size={32} weight="duotone" className="text-green-500" />
      </div>
      <h2 className="text-xl font-extrabold text-gray-900 mb-2">
        No store yet
      </h2>
      <p className="text-gray-400 text-sm max-w-xs mb-6">
        Create your first store to start listing flash deals and reduce food
        waste.
      </p>
      <Link
        to="/store/settings?action=create"
        className="inline-flex items-center gap-2 bg-green-600 text-white px-5 py-2.5 rounded-full text-sm font-bold hover:bg-green-700 transition-colors"
      >
        <Plus size={15} weight="bold" /> Create your store
      </Link>
    </div>
  );
}

// ─── Main page ────────────────────────────────────────────────────────────────
export function StoreDashboard() {
  const { user } = useAuthStore();

  const {
    data: storesData,
    isLoading: storesLoading,
    isError: storesError,
  } = useQuery({
    queryKey: ["my-stores"],
    queryFn: () => storesApi.getMyStores().then((r) => r.data.data),
  });

  const stores: StoreResponse[] = storesData ?? [];
  const activeStore = stores[0] ?? null;

  const { data: itemsData, isLoading: itemsLoading } = useQuery({
    queryKey: ["store-food-items", activeStore?.id],
    queryFn: () =>
      dealsApi
        .getByStore(activeStore!.id, { size: 5 })
        .then((r) => r.data.data),
    enabled: !!activeStore,
  });

  const recentItems: FoodItemResponse[] = itemsData?.content ?? [];
  const totalItems = itemsData?.totalElements ?? 0;
  const activeItems = recentItems.filter((i) => i.status === "ACTIVE").length;

  if (storesError) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center px-4">
        <Warning size={40} className="text-red-400 mb-3" />
        <p className="text-gray-500 text-sm">
          Unable to load your stores. Please try again.
        </p>
      </div>
    );
  }

  if (!storesLoading && stores.length === 0) {
    return <NoStoreState />;
  }

  return (
    <div className="p-6 space-y-7 max-w-6xl">
      {/* Welcome */}
      <div>
        <h2 className="text-xl font-extrabold text-gray-900">
          Good to see you,{" "}
          <span className="text-green-600">
            {user?.fullName?.split(" ")[0] ?? "there"}
          </span>{" "}
          👋
        </h2>
        <p className="text-sm text-gray-400 mt-0.5">
          Here's what's happening with your store today.
        </p>
      </div>

      {/* KPI cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="My Stores"
          value={storesLoading ? "..." : stores.length}
          sub={activeStore?.name}
          icon={
            <Storefront
              size={22}
              weight="duotone"
              className="text-indigo-600"
            />
          }
          color="bg-indigo-50"
          loading={storesLoading}
        />
        <StatCard
          label="Total Deals"
          value={itemsLoading ? "..." : totalItems}
          sub="All food items"
          icon={
            <Lightning size={22} weight="duotone" className="text-amber-600" />
          }
          color="bg-amber-50"
          loading={itemsLoading}
        />
        <StatCard
          label="Active Now"
          value={itemsLoading ? "..." : activeItems}
          sub="Live flash deals"
          icon={
            <Lightning size={22} weight="fill" className="text-green-600" />
          }
          color="bg-green-50"
          loading={itemsLoading}
        />
        <StatCard
          label="Orders"
          value="—"
          sub="View all orders"
          icon={
            <ClipboardText
              size={22}
              weight="duotone"
              className="text-violet-600"
            />
          }
          color="bg-violet-50"
        />
      </div>

      {/* My stores list */}
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-50">
          <h3 className="font-extrabold text-gray-800 text-sm">My Stores</h3>
          <Link
            to="/store/settings"
            className="text-xs font-semibold text-green-600 hover:text-green-700 flex items-center gap-1"
          >
            Manage <ArrowRight size={13} weight="bold" />
          </Link>
        </div>

        {storesLoading ? (
          <div className="p-5 space-y-3">
            {[1, 2].map((i) => (
              <div
                key={i}
                className="h-14 bg-gray-50 rounded-lg animate-pulse"
              />
            ))}
          </div>
        ) : (
          <ul className="divide-y divide-gray-50">
            {stores.map((store) => (
              <li
                key={store.id}
                className="flex items-center gap-3 px-5 py-3.5 hover:bg-gray-50/60 transition-colors"
              >
                <div className="w-9 h-9 rounded-lg bg-green-50 grid place-items-center shrink-0">
                  <Storefront
                    size={18}
                    weight="duotone"
                    className="text-green-600"
                  />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-sm text-gray-900 truncate">
                    {store.name}
                  </p>
                  <p className="text-xs text-gray-400 truncate">
                    {store.address}
                  </p>
                </div>
                <StatusBadge status={store.status} />
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Recent food items */}
      {activeStore && (
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-gray-50">
            <h3 className="font-extrabold text-gray-800 text-sm">
              Recent Food Items
            </h3>
            <Link
              to="/store/food-items"
              className="text-xs font-semibold text-green-600 hover:text-green-700 flex items-center gap-1"
            >
              See all <ArrowRight size={13} weight="bold" />
            </Link>
          </div>

          {itemsLoading ? (
            <div className="p-5 space-y-3">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="h-12 bg-gray-50 rounded-lg animate-pulse"
                />
              ))}
            </div>
          ) : recentItems.length === 0 ? (
            <div className="flex flex-col items-center py-12 text-center">
              <SmileyMeh size={32} className="text-gray-300 mb-2" />
              <p className="text-sm text-gray-400">No food items yet.</p>
              <Link
                to="/store/food-items?action=create"
                className="mt-3 text-xs font-semibold text-green-600 hover:text-green-700 flex items-center gap-1"
              >
                <Plus size={12} weight="bold" /> Add your first deal
              </Link>
            </div>
          ) : (
            <ul className="divide-y divide-gray-50">
              {recentItems.map((item) => (
                <li
                  key={item.id}
                  className="flex items-center gap-3 px-5 py-3 hover:bg-gray-50/60 transition-colors"
                >
                  <div className="w-8 h-8 rounded-lg bg-green-50 grid place-items-center shrink-0 text-base">
                    {item.imageUrl ? (
                      <img
                        src={item.imageUrl}
                        alt={item.name}
                        className="w-8 h-8 object-cover rounded-lg"
                      />
                    ) : (
                      <Lightning
                        size={16}
                        weight="fill"
                        className="text-green-500"
                      />
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-sm text-gray-900 truncate">
                      {item.name}
                    </p>
                    <p className="text-xs text-gray-400">
                      ${item.flashPrice.toFixed(2)}{" "}
                      <span className="line-through text-gray-300">
                        ${item.originalPrice.toFixed(2)}
                      </span>
                      {" · "}
                      {item.availableQuantity} left
                    </p>
                  </div>
                  <FoodItemStatusBadge status={item.status} />
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {/* Quick actions */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <Link
          to="/store/food-items?action=create"
          className="flex items-center gap-3 bg-green-600 hover:bg-green-700 transition-colors text-white rounded-xl px-5 py-4"
        >
          <div className="w-9 h-9 bg-white/20 rounded-lg grid place-items-center">
            <Plus size={20} weight="bold" />
          </div>
          <div>
            <p className="font-bold text-sm">Add New Deal</p>
            <p className="text-xs text-green-200/80">List a flash food item</p>
          </div>
        </Link>

        <Link
          to="/store/orders"
          className="flex items-center gap-3 bg-white border border-gray-100 hover:border-green-200 transition-colors rounded-xl px-5 py-4"
        >
          <div className="w-9 h-9 bg-amber-50 rounded-lg grid place-items-center">
            <ClipboardText
              size={20}
              weight="duotone"
              className="text-amber-600"
            />
          </div>
          <div>
            <p className="font-bold text-sm text-gray-800">View Orders</p>
            <p className="text-xs text-gray-400">Manage incoming orders</p>
          </div>
        </Link>
      </div>
    </div>
  );
}

// ─── re-exported helper used in other pages ───────────────────────────────────
export function FoodItemStatusBadge({ status }: { status: string }) {
  const map: Record<string, string> = {
    ACTIVE: "bg-green-50 text-green-700 border-green-200",
    PENDING: "bg-amber-50 text-amber-700 border-amber-200",
    SOLD_OUT: "bg-red-50 text-red-600 border-red-200",
    EXPIRED: "bg-gray-100 text-gray-400 border-gray-200",
    INACTIVE: "bg-gray-100 text-gray-400 border-gray-200",
    DELETED: "bg-red-50 text-red-400 border-red-100",
  };
  const pretty: Record<string, string> = {
    ACTIVE: "Active",
    PENDING: "Pending",
    SOLD_OUT: "Sold Out",
    EXPIRED: "Expired",
    INACTIVE: "Inactive",
    DELETED: "Deleted",
  };
  return (
    <span
      className={cn(
        "inline-flex items-center px-2 py-0.5 rounded-full text-[0.7rem] font-semibold border",
        map[status] ?? "bg-gray-100 text-gray-500 border-gray-200",
      )}
    >
      {pretty[status] ?? status}
    </span>
  );
}
