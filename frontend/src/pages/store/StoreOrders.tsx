import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  ClipboardText,
  CheckCircle,
  CookingPot,
  BellRinging,
  Checks,
  SmileyMeh,
  Warning,
} from "@phosphor-icons/react";
import { storesApi } from "@/api/stores.api";
import { ordersApi } from "@/api/orders.api";
import { cn } from "@/utils/cn";
import type { OrderResponse, OrderStatus, StoreResponse } from "@/types";
import toast from "react-hot-toast";

// ─── Status config ────────────────────────────────────────────────────────────
const ORDER_STATUS_CONFIG: Record<
  OrderStatus,
  { label: string; badge: string; dot: string }
> = {
  PENDING: {
    label: "Pending",
    badge: "bg-amber-50 text-amber-700 border-amber-200",
    dot: "bg-amber-400",
  },
  CONFIRMED: {
    label: "Confirmed",
    badge: "bg-blue-50 text-blue-700 border-blue-200",
    dot: "bg-blue-400",
  },
  PREPARING: {
    label: "Preparing",
    badge: "bg-violet-50 text-violet-700 border-violet-200",
    dot: "bg-violet-400",
  },
  READY: {
    label: "Ready",
    badge: "bg-green-50 text-green-700 border-green-200",
    dot: "bg-green-500",
  },
  COMPLETED: {
    label: "Completed",
    badge: "bg-gray-100 text-gray-500 border-gray-200",
    dot: "bg-gray-400",
  },
  CANCELLED: {
    label: "Cancelled",
    badge: "bg-red-50 text-red-600 border-red-200",
    dot: "bg-red-400",
  },
};

function OrderStatusBadge({ status }: { status: OrderStatus }) {
  const config = ORDER_STATUS_CONFIG[status] ?? {
    label: status,
    badge: "bg-gray-100 text-gray-500 border-gray-200",
    dot: "bg-gray-400",
  };
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[0.7rem] font-semibold border",
        config.badge,
      )}
    >
      <span className={cn("w-1.5 h-1.5 rounded-full", config.dot)} />
      {config.label}
    </span>
  );
}

// ─── Next action button for each order status ─────────────────────────────────
function OrderActions({
  order,
  onAction,
  loading,
}: {
  order: OrderResponse;
  onAction: (action: string, id: number) => void;
  loading: boolean;
}) {
  const btnClass =
    "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-colors disabled:opacity-50";

  switch (order.status) {
    case "PENDING":
      return (
        <button
          onClick={() => onAction("confirm", order.id)}
          disabled={loading}
          className={cn(btnClass, "bg-blue-600 hover:bg-blue-700 text-white")}
        >
          <CheckCircle size={13} weight="bold" /> Confirm
        </button>
      );
    case "CONFIRMED":
      return (
        <button
          onClick={() => onAction("preparing", order.id)}
          disabled={loading}
          className={cn(
            btnClass,
            "bg-violet-600 hover:bg-violet-700 text-white",
          )}
        >
          <CookingPot size={13} weight="bold" /> Preparing
        </button>
      );
    case "PREPARING":
      return (
        <button
          onClick={() => onAction("ready", order.id)}
          disabled={loading}
          className={cn(btnClass, "bg-green-600 hover:bg-green-700 text-white")}
        >
          <BellRinging size={13} weight="bold" /> Mark Ready
        </button>
      );
    case "READY":
      return (
        <button
          onClick={() => onAction("complete", order.id)}
          disabled={loading}
          className={cn(btnClass, "bg-gray-600 hover:bg-gray-700 text-white")}
        >
          <Checks size={13} weight="bold" /> Complete
        </button>
      );
    default:
      return null;
  }
}

// ─── Status tab filters ───────────────────────────────────────────────────────
const TABS: { label: string; value: OrderStatus | "" }[] = [
  { label: "All", value: "" },
  { label: "Pending", value: "PENDING" },
  { label: "Confirmed", value: "CONFIRMED" },
  { label: "Preparing", value: "PREPARING" },
  { label: "Ready", value: "READY" },
  { label: "Completed", value: "COMPLETED" },
];

// ─── Main page ────────────────────────────────────────────────────────────────
export function StoreOrders() {
  const [activeTab, setActiveTab] = useState<OrderStatus | "">("");
  const [selectedStore, setSelectedStore] = useState<number | null>(null);
  const qc = useQueryClient();

  const { data: stores } = useQuery({
    queryKey: ["my-stores"],
    queryFn: () => storesApi.getMyStores().then((r) => r.data.data),
  });

  const storeList: StoreResponse[] = stores ?? [];

  useEffect(() => {
    if (storeList.length > 0 && !selectedStore) {
      setSelectedStore(storeList[0].id);
    }
  }, [storeList, selectedStore]);

  const {
    data: ordersPage,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["store-orders", selectedStore, activeTab],
    queryFn: () =>
      ordersApi
        .getByStore(selectedStore!, {
          status: activeTab || undefined,
          size: 50,
        })
        .then((r) => r.data.data),
    enabled: !!selectedStore,
    refetchInterval: 30_000, // poll every 30s for new orders
  });

  const orders: OrderResponse[] = ordersPage?.content ?? [];

  // ── Action mutations ────────────────────────────────────────────────────────
  const actionMutation = useMutation({
    mutationFn: ({ action, id }: { action: string; id: number }) => {
      switch (action) {
        case "confirm":
          return ordersApi.confirm(id);
        case "preparing":
          return ordersApi.markPreparing(id);
        case "ready":
          return ordersApi.markReady(id);
        case "complete":
          return ordersApi.complete(id);
        default:
          return Promise.reject(new Error("Unknown action"));
      }
    },
    onSuccess: (_, { action }) => {
      const labels: Record<string, string> = {
        confirm: "Order confirmed!",
        preparing: "Order is being prepared.",
        ready: "Order marked as ready!",
        complete: "Order completed.",
      };
      toast.success(labels[action] ?? "Updated.");
      qc.invalidateQueries({ queryKey: ["store-orders"] });
    },
    onError: () => toast.error("Failed to update order status."),
  });

  const handleAction = (action: string, id: number) => {
    actionMutation.mutate({ action, id });
  };

  const filteredOrders = activeTab
    ? orders.filter((o) => o.status === activeTab)
    : orders;

  if (!isLoading && storeList.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center px-4">
        <ClipboardText
          size={40}
          weight="duotone"
          className="text-gray-300 mb-3"
        />
        <p className="text-gray-500 text-sm">No store found.</p>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-6xl">
      <div className="mb-5">
        <h2 className="font-extrabold text-gray-900">Orders</h2>
        <p className="text-xs text-gray-400 mt-0.5">
          Manage and progress incoming customer orders.
        </p>
      </div>

      {/* Store selector */}
      {storeList.length > 1 && (
        <div className="flex gap-2 mb-4 overflow-x-auto pb-1">
          {storeList.map((s) => (
            <button
              key={s.id}
              onClick={() => setSelectedStore(s.id)}
              className={cn(
                "px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap border transition-colors",
                selectedStore === s.id
                  ? "bg-green-600 text-white border-green-600"
                  : "bg-white text-gray-600 border-gray-200 hover:border-green-300",
              )}
            >
              {s.name}
            </button>
          ))}
        </div>
      )}

      {/* Status tabs */}
      <div className="flex gap-1 overflow-x-auto pb-1 mb-4 border-b border-gray-100">
        {TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => setActiveTab(tab.value)}
            className={cn(
              "px-3.5 py-2 text-xs font-semibold whitespace-nowrap border-b-2 transition-colors -mb-px",
              activeTab === tab.value
                ? "border-green-600 text-green-700"
                : "border-transparent text-gray-400 hover:text-gray-700",
            )}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Orders list */}
      <div className="space-y-3">
        {isLoading ? (
          Array.from({ length: 4 }).map((_, i) => (
            <div
              key={i}
              className="h-20 bg-white rounded-xl border border-gray-100 animate-pulse"
            />
          ))
        ) : isError ? (
          <div className="bg-white rounded-xl border border-gray-100 p-12 text-center">
            <Warning size={36} className="text-red-400 mx-auto mb-2" />
            <p className="text-sm text-gray-400">
              Could not load orders. Please try again.
            </p>
          </div>
        ) : filteredOrders.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-100 p-16 text-center">
            <SmileyMeh size={36} className="text-gray-300 mx-auto mb-2" />
            <p className="text-sm text-gray-400">No orders here yet.</p>
          </div>
        ) : (
          filteredOrders.map((order) => (
            <div
              key={order.id}
              className="bg-white rounded-xl border border-gray-100 shadow-sm px-5 py-4 flex items-start gap-4"
            >
              {/* Status dot */}
              <div
                className={cn(
                  "w-2 h-2 rounded-full mt-1.5 shrink-0",
                  ORDER_STATUS_CONFIG[order.status]?.dot ?? "bg-gray-400",
                )}
              />

              {/* Main info */}
              <div className="flex-1 min-w-0">
                <div className="flex flex-wrap items-center gap-2 mb-1">
                  <span className="font-bold text-sm text-gray-900">
                    #{order.orderNumber}
                  </span>
                  <OrderStatusBadge status={order.status} />
                </div>

                {/* Items */}
                <p className="text-xs text-gray-500 mb-1.5 truncate">
                  {order.items
                    .map((i) => `${i.quantity}× ${i.foodItemName}`)
                    .join(", ")}
                </p>

                <div className="flex flex-wrap items-center gap-3 text-xs text-gray-400">
                  <span>
                    Total:{" "}
                    <strong className="text-gray-700">
                      ${order.totalAmount.toFixed(2)}
                    </strong>
                  </span>
                  <span>
                    {new Date(order.createdAt).toLocaleString("en-US", {
                      month: "short",
                      day: "numeric",
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </span>
                  {order.pickupTime && (
                    <span>
                      Pickup:{" "}
                      {new Date(order.pickupTime).toLocaleTimeString("en-US", {
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </span>
                  )}
                </div>

                {order.specialInstructions && (
                  <p className="text-xs text-amber-600 mt-1.5 bg-amber-50 px-2 py-1 rounded-md inline-block">
                    Note: {order.specialInstructions}
                  </p>
                )}
              </div>

              {/* Action */}
              <div className="shrink-0">
                <OrderActions
                  order={order}
                  onAction={handleAction}
                  loading={actionMutation.isPending}
                />
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
