import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  Storefront,
  Plus,
  PencilSimple,
  CheckCircle,
  Warning,
  X,
} from "@phosphor-icons/react";
import { AnimatePresence, motion } from "framer-motion";
import { storesApi } from "@/api/stores.api";
import { cn } from "@/utils/cn";
import type { StoreResponse, StoreType } from "@/types";
import toast from "react-hot-toast";

// ─── Form schema ──────────────────────────────────────────────────────────────
const storeSchema = z.object({
  name: z.string().min(2, "Store name is required"),
  address: z.string().min(5, "Address is required"),
  phoneNumber: z.string().min(7, "Phone number is required"),
  type: z.enum([
    "RESTAURANT",
    "BAKERY",
    "CAFE",
    "GROCERY",
    "FOOD_TRUCK",
    "OTHER",
  ] as const),
  description: z.string().optional(),
  latitude: z.coerce.number().optional(),
  longitude: z.coerce.number().optional(),
  openTime: z.string().optional(),
  closeTime: z.string().optional(),
});
type StoreFormData = z.infer<typeof storeSchema>;

const STORE_TYPES: { value: StoreType; label: string }[] = [
  { value: "RESTAURANT", label: "Restaurant" },
  { value: "BAKERY", label: "Bakery" },
  { value: "CAFE", label: "Café" },
  { value: "GROCERY", label: "Grocery" },
  { value: "FOOD_TRUCK", label: "Food Truck" },
  { value: "OTHER", label: "Other" },
];

const STATUS_BADGE: Record<string, string> = {
  ACTIVE: "bg-green-50 text-green-700 border-green-200",
  PENDING_APPROVAL: "bg-amber-50 text-amber-700 border-amber-200",
  INACTIVE: "bg-gray-100 text-gray-500 border-gray-200",
  SUSPENDED: "bg-red-50 text-red-600 border-red-200",
  REJECTED: "bg-red-50 text-red-600 border-red-200",
};
const STATUS_LABEL: Record<string, string> = {
  ACTIVE: "Active",
  PENDING_APPROVAL: "Pending Approval",
  INACTIVE: "Inactive",
  SUSPENDED: "Suspended",
  REJECTED: "Rejected",
};

// ─── Store form modal ─────────────────────────────────────────────────────────
function StoreFormModal({
  open,
  onClose,
  editStore,
}: {
  open: boolean;
  onClose: () => void;
  editStore?: StoreResponse | null;
}) {
  const qc = useQueryClient();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<StoreFormData>({
    resolver: zodResolver(storeSchema),
    defaultValues: editStore
      ? {
          name: editStore.name,
          address: editStore.address,
          phoneNumber: editStore.phoneNumber,
          type: editStore.type,
          description: editStore.description ?? "",
          latitude: editStore.latitude,
          longitude: editStore.longitude,
          openTime: editStore.openTime ?? "",
          closeTime: editStore.closeTime ?? "",
        }
      : { type: "RESTAURANT" },
  });

  const onSubmit = async (data: StoreFormData) => {
    try {
      if (editStore) {
        await storesApi.update(editStore.id, data);
        toast.success("Store updated!");
      } else {
        await storesApi.create({
          ...data,
          latitude: data.latitude ?? 0,
          longitude: data.longitude ?? 0,
        });
        toast.success("Store created! Pending admin approval.");
      }
      await qc.invalidateQueries({ queryKey: ["my-stores"] });
      onClose();
      reset();
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message ?? "Something went wrong";
      toast.error(msg);
    }
  };

  const fieldClass = (hasError?: boolean) =>
    cn(
      "w-full rounded-lg border px-3 py-2 text-sm outline-none transition",
      "focus:ring-2 focus:ring-green-100",
      hasError
        ? "border-red-300 focus:border-red-400"
        : "border-gray-200 focus:border-green-400",
    );

  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            key="overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/30 backdrop-blur-sm z-40"
            onClick={onClose}
          />
          <motion.div
            key="drawer"
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", stiffness: 300, damping: 30 }}
            className="fixed inset-y-0 right-0 w-full max-w-md bg-white shadow-2xl z-50 flex flex-col"
          >
            <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
              <h2 className="font-extrabold text-gray-900">
                {editStore ? "Edit Store" : "Create Store"}
              </h2>
              <button
                onClick={onClose}
                className="w-7 h-7 rounded-full bg-gray-100 grid place-items-center text-gray-500 hover:bg-gray-200 transition-colors"
              >
                <X size={13} weight="bold" />
              </button>
            </div>

            <form
              onSubmit={handleSubmit(onSubmit)}
              className="flex-1 overflow-y-auto p-5 space-y-4"
            >
              <div>
                <label className="block text-xs font-bold text-gray-600 mb-1">
                  Store Name <span className="text-red-400">*</span>
                </label>
                <input
                  {...register("name")}
                  placeholder="e.g. The Green Bakery"
                  className={fieldClass(!!errors.name)}
                />
                {errors.name && (
                  <p className="text-red-500 text-[0.7rem] mt-1">
                    {errors.name.message}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-600 mb-1">
                  Type <span className="text-red-400">*</span>
                </label>
                <select
                  {...register("type")}
                  className={fieldClass(!!errors.type)}
                >
                  {STORE_TYPES.map((t) => (
                    <option key={t.value} value={t.value}>
                      {t.label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-600 mb-1">
                  Address <span className="text-red-400">*</span>
                </label>
                <input
                  {...register("address")}
                  placeholder="123 Food Lane"
                  className={fieldClass(!!errors.address)}
                />
                {errors.address && (
                  <p className="text-red-500 text-[0.7rem] mt-1">
                    {errors.address.message}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-600 mb-1">
                  Phone Number <span className="text-red-400">*</span>
                </label>
                <input
                  {...register("phoneNumber")}
                  type="tel"
                  placeholder="+84 90 000 0000"
                  className={fieldClass(!!errors.phoneNumber)}
                />
                {errors.phoneNumber && (
                  <p className="text-red-500 text-[0.7rem] mt-1">
                    {errors.phoneNumber.message}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-600 mb-1">
                  Description
                </label>
                <textarea
                  {...register("description")}
                  rows={2}
                  placeholder="Tell customers about your store…"
                  className={cn(fieldClass(false), "resize-none")}
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-gray-600 mb-1">
                    Latitude
                  </label>
                  <input
                    {...register("latitude")}
                    type="number"
                    step="any"
                    placeholder="10.7769"
                    className={fieldClass()}
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-gray-600 mb-1">
                    Longitude
                  </label>
                  <input
                    {...register("longitude")}
                    type="number"
                    step="any"
                    placeholder="106.7009"
                    className={fieldClass()}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-gray-600 mb-1">
                    Opens at
                  </label>
                  <input
                    {...register("openTime")}
                    type="time"
                    className={fieldClass()}
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-gray-600 mb-1">
                    Closes at
                  </label>
                  <input
                    {...register("closeTime")}
                    type="time"
                    className={fieldClass()}
                  />
                </div>
              </div>

              <div className="pt-2">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full bg-green-600 hover:bg-green-700 disabled:opacity-60 text-white rounded-lg px-4 py-2.5 text-sm font-bold transition-colors flex items-center justify-center gap-2"
                >
                  {isSubmitting && (
                    <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  )}
                  {isSubmitting
                    ? editStore
                      ? "Saving…"
                      : "Creating…"
                    : editStore
                      ? "Save Changes"
                      : "Create Store"}
                </button>
              </div>
            </form>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}

// ─── Main page ─────────────────────────────────────────────────────────────────
export function StoreSettings() {
  const [searchParams] = useSearchParams();
  const [formOpen, setFormOpen] = useState(
    searchParams.get("action") === "create",
  );
  const [editStore, setEditStore] = useState<StoreResponse | null>(null);

  const {
    data: stores,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["my-stores"],
    queryFn: () => storesApi.getMyStores().then((r) => r.data.data),
  });

  const storeList: StoreResponse[] = stores ?? [];

  const openCreate = () => {
    setEditStore(null);
    setFormOpen(true);
  };
  const openEdit = (store: StoreResponse) => {
    setEditStore(store);
    setFormOpen(true);
  };

  return (
    <div className="p-6 max-w-3xl">
      <div className="flex flex-col sm:flex-row sm:items-center gap-3 mb-6">
        <div className="flex-1">
          <h2 className="font-extrabold text-gray-900">My Store</h2>
          <p className="text-xs text-gray-400 mt-0.5">
            Manage your store profile and information.
          </p>
        </div>
        <button
          onClick={openCreate}
          className="inline-flex items-center gap-2 bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg text-sm font-bold transition-colors"
        >
          <Plus size={15} weight="bold" /> New Store
        </button>
      </div>

      {isLoading ? (
        <div className="space-y-4">
          {[1, 2].map((i) => (
            <div
              key={i}
              className="h-28 bg-white rounded-xl border border-gray-100 animate-pulse"
            />
          ))}
        </div>
      ) : isError ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center">
          <Warning size={32} className="text-red-400 mx-auto mb-2" />
          <p className="text-sm text-gray-400">Could not load stores.</p>
        </div>
      ) : storeList.length === 0 ? (
        <div className="bg-white rounded-xl border border-dashed border-gray-200 p-14 text-center">
          <div className="w-14 h-14 bg-green-50 rounded-2xl grid place-items-center mx-auto mb-4">
            <Storefront size={28} weight="duotone" className="text-green-500" />
          </div>
          <h3 className="font-bold text-gray-700 mb-1">No stores yet</h3>
          <p className="text-sm text-gray-400 mb-5">
            Create your first store to start listing flash deals.
          </p>
          <button
            onClick={openCreate}
            className="inline-flex items-center gap-2 bg-green-600 text-white px-5 py-2 rounded-full text-sm font-bold hover:bg-green-700 transition-colors"
          >
            <Plus size={14} weight="bold" /> Create Store
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {storeList.map((store) => (
            <div
              key={store.id}
              className="bg-white rounded-xl border border-gray-100 shadow-sm p-5"
            >
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 bg-green-50 rounded-xl grid place-items-center shrink-0">
                  <Storefront
                    size={24}
                    weight="duotone"
                    className="text-green-600"
                  />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex flex-wrap items-center gap-2 mb-1">
                    <h3 className="font-bold text-gray-900 text-base">
                      {store.name}
                    </h3>
                    <span
                      className={cn(
                        "inline-flex items-center px-2 py-0.5 rounded-full text-[0.7rem] font-semibold border",
                        STATUS_BADGE[store.status] ??
                          "bg-gray-100 text-gray-500 border-gray-200",
                      )}
                    >
                      {STATUS_LABEL[store.status] ?? store.status}
                    </span>
                  </div>
                  <p className="text-sm text-gray-500 mb-0.5">
                    {store.address}
                  </p>
                  <p className="text-xs text-gray-400">
                    {store.phoneNumber} ·{" "}
                    {STORE_TYPES.find((t) => t.value === store.type)?.label ??
                      store.type}
                    {store.openTime &&
                      store.closeTime &&
                      ` · ${store.openTime} – ${store.closeTime}`}
                  </p>
                  {store.status === "PENDING_APPROVAL" && (
                    <div className="flex items-center gap-1.5 mt-2 text-xs text-amber-600 bg-amber-50 px-2.5 py-1.5 rounded-lg w-fit">
                      <CheckCircle size={13} weight="duotone" />
                      Awaiting admin approval before going live.
                    </div>
                  )}
                </div>
                <button
                  onClick={() => openEdit(store)}
                  className="w-8 h-8 rounded-lg grid place-items-center text-gray-400 hover:bg-gray-100 hover:text-gray-700 transition-colors shrink-0"
                  title="Edit store"
                >
                  <PencilSimple size={15} weight="bold" />
                </button>
              </div>

              {store.description && (
                <p className="text-sm text-gray-400 mt-3 pt-3 border-t border-gray-50">
                  {store.description}
                </p>
              )}
            </div>
          ))}
        </div>
      )}

      <StoreFormModal
        open={formOpen}
        onClose={() => {
          setFormOpen(false);
          setEditStore(null);
        }}
        editStore={editStore}
      />
    </div>
  );
}
