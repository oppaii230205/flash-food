import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  Plus,
  MagnifyingGlass,
  PencilSimple,
  Trash,
  Lightning,
  LightningSlash,
  Warning,
  SmileyMeh,
  X,
  Storefront,
} from "@phosphor-icons/react";
import { AnimatePresence, motion } from "framer-motion";

import { storesApi } from "@/api/stores.api";
import { dealsApi } from "@/api/deals.api";
import { cn } from "@/utils/cn";
import { FoodItemStatusBadge } from "./StoreDashboard";
import type { FoodItemResponse, FoodItemStatus, StoreResponse } from "@/types";
import toast from "react-hot-toast";

// ─── Form schema ──────────────────────────────────────────────────────────────
const foodItemSchema = z
  .object({
    name: z.string().min(2, "Name is required"),
    description: z.string().optional(),
    imageUrl: z
      .string()
      .url("Must be a valid URL")
      .optional()
      .or(z.literal("")),
    originalPrice: z.coerce.number().positive("Must be positive"),
    flashPrice: z.coerce.number().positive("Must be positive"),
    quantity: z.coerce.number().int().positive("Must be positive"),
    saleStartTime: z.string().min(1, "Start time is required"),
    saleEndTime: z.string().min(1, "End time is required"),
  })
  .refine((d) => d.flashPrice < d.originalPrice, {
    message: "Flash price must be lower than original price",
    path: ["flashPrice"],
  });
type FoodItemFormData = z.infer<typeof foodItemSchema>;

// ─── Deal form drawer / modal ─────────────────────────────────────────────────
function FoodItemFormModal({
  open,
  onClose,
  storeId,
  editItem,
}: {
  open: boolean;
  onClose: () => void;
  storeId: number;
  editItem?: FoodItemResponse | null;
}) {
  const qc = useQueryClient();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<FoodItemFormData>({ resolver: zodResolver(foodItemSchema) });

  // Pre-fill when editing
  useEffect(() => {
    if (editItem) {
      reset({
        name: editItem.name,
        description: editItem.description ?? "",
        imageUrl: editItem.imageUrl ?? "",
        originalPrice: editItem.originalPrice,
        flashPrice: editItem.flashPrice,
        quantity: editItem.totalQuantity,
        saleStartTime: editItem.saleStartTime?.slice(0, 16) ?? "",
        saleEndTime: editItem.saleEndTime?.slice(0, 16) ?? "",
      });
    } else {
      reset({
        name: "",
        description: "",
        imageUrl: "",
        originalPrice: 0,
        flashPrice: 0,
        quantity: 1,
        saleStartTime: "",
        saleEndTime: "",
      });
    }
  }, [editItem, reset, open]);

  const onSubmit = async (data: FoodItemFormData) => {
    const payload = {
      ...data,
      imageUrl: data.imageUrl || undefined,
      description: data.description || undefined,
      saleStartTime: new Date(data.saleStartTime).toISOString(),
      saleEndTime: new Date(data.saleEndTime).toISOString(),
    };
    try {
      if (editItem) {
        await dealsApi.update(editItem.id, payload);
        toast.success("Deal updated!");
      } else {
        await dealsApi.create(storeId, payload);
        toast.success("New deal created!");
      }
      await qc.invalidateQueries({ queryKey: ["store-food-items"] });
      onClose();
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
            {/* Header */}
            <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
              <h2 className="font-extrabold text-gray-900">
                {editItem ? "Edit Deal" : "Add New Deal"}
              </h2>
              <button
                onClick={onClose}
                className="w-7 h-7 rounded-full bg-gray-100 grid place-items-center text-gray-500 hover:bg-gray-200 transition-colors"
              >
                <X size={13} weight="bold" />
              </button>
            </div>

            {/* Form */}
            <form
              onSubmit={handleSubmit(onSubmit)}
              className="flex-1 overflow-y-auto p-5 space-y-4"
            >
              <div>
                <label className="block text-xs font-bold text-gray-600 mb-1">
                  Item Name <span className="text-red-400">*</span>
                </label>
                <input
                  {...register("name")}
                  placeholder="e.g. Sourdough Bread Loaf"
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
                  Description
                </label>
                <textarea
                  {...register("description")}
                  rows={2}
                  placeholder="Short description of the item..."
                  className={cn(fieldClass(false), "resize-none")}
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-600 mb-1">
                  Image URL
                </label>
                <input
                  {...register("imageUrl")}
                  placeholder="https://..."
                  className={fieldClass(!!errors.imageUrl)}
                />
                {errors.imageUrl && (
                  <p className="text-red-500 text-[0.7rem] mt-1">
                    {errors.imageUrl.message}
                  </p>
                )}
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-gray-600 mb-1">
                    Original Price ($) <span className="text-red-400">*</span>
                  </label>
                  <input
                    {...register("originalPrice")}
                    type="number"
                    step="0.01"
                    placeholder="0.00"
                    className={fieldClass(!!errors.originalPrice)}
                  />
                  {errors.originalPrice && (
                    <p className="text-red-500 text-[0.7rem] mt-1">
                      {errors.originalPrice.message}
                    </p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-bold text-gray-600 mb-1">
                    Flash Price ($) <span className="text-red-400">*</span>
                  </label>
                  <input
                    {...register("flashPrice")}
                    type="number"
                    step="0.01"
                    placeholder="0.00"
                    className={fieldClass(!!errors.flashPrice)}
                  />
                  {errors.flashPrice && (
                    <p className="text-red-500 text-[0.7rem] mt-1">
                      {errors.flashPrice.message}
                    </p>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-600 mb-1">
                  Quantity <span className="text-red-400">*</span>
                </label>
                <input
                  {...register("quantity")}
                  type="number"
                  placeholder="e.g. 10"
                  className={fieldClass(!!errors.quantity)}
                />
                {errors.quantity && (
                  <p className="text-red-500 text-[0.7rem] mt-1">
                    {errors.quantity.message}
                  </p>
                )}
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-gray-600 mb-1">
                    Sale Start <span className="text-red-400">*</span>
                  </label>
                  <input
                    {...register("saleStartTime")}
                    type="datetime-local"
                    className={fieldClass(!!errors.saleStartTime)}
                  />
                  {errors.saleStartTime && (
                    <p className="text-red-500 text-[0.7rem] mt-1">
                      {errors.saleStartTime.message}
                    </p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-bold text-gray-600 mb-1">
                    Sale End <span className="text-red-400">*</span>
                  </label>
                  <input
                    {...register("saleEndTime")}
                    type="datetime-local"
                    className={fieldClass(!!errors.saleEndTime)}
                  />
                  {errors.saleEndTime && (
                    <p className="text-red-500 text-[0.7rem] mt-1">
                      {errors.saleEndTime.message}
                    </p>
                  )}
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
                    ? editItem
                      ? "Saving…"
                      : "Creating…"
                    : editItem
                      ? "Save Changes"
                      : "Create Deal"}
                </button>
              </div>
            </form>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}

// ─── Delete confirm dialog ────────────────────────────────────────────────────
function DeleteDialog({
  item,
  onConfirm,
  onCancel,
  loading,
}: {
  item: FoodItemResponse;
  onConfirm: () => void;
  onCancel: () => void;
  loading: boolean;
}) {
  return (
    <div className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.95 }}
        className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-sm"
      >
        <div className="w-11 h-11 bg-red-50 rounded-xl grid place-items-center mb-4">
          <Warning size={22} weight="duotone" className="text-red-500" />
        </div>
        <h3 className="font-extrabold text-gray-900 mb-1">Delete deal?</h3>
        <p className="text-sm text-gray-500 mb-5">
          <strong>{item.name}</strong> will be permanently removed.
        </p>
        <div className="flex gap-2.5">
          <button
            onClick={onCancel}
            className="flex-1 px-4 py-2 rounded-lg border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={onConfirm}
            disabled={loading}
            className="flex-1 px-4 py-2 rounded-lg bg-red-500 hover:bg-red-600 disabled:opacity-60 text-white text-sm font-bold transition-colors flex items-center justify-center gap-1.5"
          >
            {loading && (
              <span className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            )}
            Delete
          </button>
        </div>
      </motion.div>
    </div>
  );
}

// ─── Status filter options ────────────────────────────────────────────────────
const STATUS_FILTERS: { label: string; value: FoodItemStatus | "" }[] = [
  { label: "All", value: "" },
  { label: "Active", value: "ACTIVE" },
  { label: "Pending", value: "PENDING" },
  { label: "Sold Out", value: "SOLD_OUT" },
  { label: "Expired", value: "EXPIRED" },
  { label: "Inactive", value: "INACTIVE" },
];

// ─── Main page ─────────────────────────────────────────────────────────────────
export function StoreFoodItems() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<FoodItemStatus | "">("");
  const [formOpen, setFormOpen] = useState(
    searchParams.get("action") === "create",
  );
  const [editItem, setEditItem] = useState<FoodItemResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<FoodItemResponse | null>(
    null,
  );
  const [selectedStore, setSelectedStore] = useState<number | null>(null);

  const qc = useQueryClient();

  const { data: stores } = useQuery({
    queryKey: ["my-stores"],
    queryFn: () => storesApi.getMyStores().then((r) => r.data.data),
  });

  const storeList: StoreResponse[] = stores ?? [];

  // Auto-select the first store
  useEffect(() => {
    if (storeList.length > 0 && !selectedStore) {
      setSelectedStore(storeList[0].id);
    }
  }, [storeList, selectedStore]);

  const { data: itemsPage, isLoading } = useQuery({
    queryKey: ["store-food-items", selectedStore, statusFilter],
    queryFn: () =>
      dealsApi
        .getByStore(selectedStore!, {
          status: statusFilter || undefined,
          size: 50,
        })
        .then((r) => r.data.data),
    enabled: !!selectedStore,
  });

  const items: FoodItemResponse[] = (itemsPage?.content ?? []).filter(
    (i) => !search || i.name.toLowerCase().includes(search.toLowerCase()),
  );

  // ── Mutations ───────────────────────────────────────────────────────────────
  const deleteMutation = useMutation({
    mutationFn: (id: number) => dealsApi.delete(id),
    onSuccess: () => {
      toast.success("Deal deleted.");
      qc.invalidateQueries({ queryKey: ["store-food-items"] });
      setDeleteTarget(null);
    },
    onError: () => toast.error("Failed to delete."),
  });

  const activateMutation = useMutation({
    mutationFn: (id: number) => dealsApi.activate(id),
    onSuccess: () => {
      toast.success("Deal activated.");
      qc.invalidateQueries({ queryKey: ["store-food-items"] });
    },
    onError: () => toast.error("Failed to activate."),
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: number) => dealsApi.deactivate(id),
    onSuccess: () => {
      toast.success("Deal deactivated.");
      qc.invalidateQueries({ queryKey: ["store-food-items"] });
    },
    onError: () => toast.error("Failed to deactivate."),
  });

  const openEdit = (item: FoodItemResponse) => {
    setEditItem(item);
    setFormOpen(true);
  };

  const closeForm = () => {
    setFormOpen(false);
    setEditItem(null);
    setSearchParams((p) => {
      p.delete("action");
      return p;
    });
  };

  // No stores state
  if (!isLoading && storeList.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center px-4">
        <Storefront size={40} weight="duotone" className="text-gray-300 mb-3" />
        <p className="text-gray-500 text-sm">
          Create a store first to manage food items.
        </p>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-6xl">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center gap-3 mb-5">
        <div className="flex-1">
          <h2 className="font-extrabold text-gray-900">Food Items</h2>
          <p className="text-xs text-gray-400 mt-0.5">
            Manage your flash deals and their availability.
          </p>
        </div>
        <button
          onClick={() => {
            setEditItem(null);
            setFormOpen(true);
          }}
          className="inline-flex items-center gap-2 bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg text-sm font-bold transition-colors"
        >
          <Plus size={15} weight="bold" /> Add New Deal
        </button>
      </div>

      {/* Store tabs */}
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

      {/* Filters bar */}
      <div className="flex flex-col sm:flex-row gap-2 mb-4">
        <div className="relative flex-1">
          <MagnifyingGlass
            size={15}
            weight="bold"
            className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
          />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search items…"
            className="w-full pl-8 pr-3 py-2 rounded-lg border border-gray-200 text-sm outline-none focus:border-green-400 focus:ring-2 focus:ring-green-100 transition"
          />
        </div>
        <div className="flex gap-1.5 overflow-x-auto pb-0.5">
          {STATUS_FILTERS.map((f) => (
            <button
              key={f.value}
              onClick={() => setStatusFilter(f.value)}
              className={cn(
                "px-3 py-2 rounded-lg text-xs font-semibold border whitespace-nowrap transition-colors",
                statusFilter === f.value
                  ? "bg-green-600 text-white border-green-600"
                  : "bg-white text-gray-600 border-gray-200 hover:border-green-300",
              )}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-5 space-y-3">
            {[1, 2, 3, 4, 5].map((i) => (
              <div
                key={i}
                className="h-14 bg-gray-50 rounded-lg animate-pulse"
              />
            ))}
          </div>
        ) : items.length === 0 ? (
          <div className="flex flex-col items-center py-16 text-center">
            <SmileyMeh size={36} className="text-gray-300 mb-2" />
            <p className="text-sm text-gray-400">
              {search || statusFilter
                ? "No items match your filters."
                : "No food items yet."}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-50">
                  <th className="text-left px-4 py-3 text-xs font-bold text-gray-400 uppercase tracking-wide">
                    Item
                  </th>
                  <th className="text-right px-4 py-3 text-xs font-bold text-gray-400 uppercase tracking-wide hidden sm:table-cell">
                    Price
                  </th>
                  <th className="text-center px-4 py-3 text-xs font-bold text-gray-400 uppercase tracking-wide hidden md:table-cell">
                    Qty Left
                  </th>
                  <th className="text-center px-4 py-3 text-xs font-bold text-gray-400 uppercase tracking-wide hidden lg:table-cell">
                    Sale Ends
                  </th>
                  <th className="text-center px-4 py-3 text-xs font-bold text-gray-400 uppercase tracking-wide">
                    Status
                  </th>
                  <th className="text-right px-4 py-3 text-xs font-bold text-gray-400 uppercase tracking-wide">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {items.map((item) => (
                  <tr
                    key={item.id}
                    className="hover:bg-gray-50/50 transition-colors"
                  >
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-lg bg-green-50 grid place-items-center shrink-0">
                          {item.imageUrl ? (
                            <img
                              src={item.imageUrl}
                              alt={item.name}
                              className="w-8 h-8 object-cover rounded-lg"
                            />
                          ) : (
                            <Lightning
                              size={15}
                              weight="fill"
                              className="text-green-500"
                            />
                          )}
                        </div>
                        <div className="min-w-0">
                          <p className="font-semibold text-gray-800 truncate max-w-[180px]">
                            {item.name}
                          </p>
                          <p className="text-xs text-gray-400 sm:hidden">
                            ${item.flashPrice.toFixed(2)}
                          </p>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3 text-right hidden sm:table-cell">
                      <p className="font-bold text-gray-800">
                        ${item.flashPrice.toFixed(2)}
                      </p>
                      <p className="text-xs text-gray-400 line-through">
                        ${item.originalPrice.toFixed(2)}
                      </p>
                    </td>
                    <td className="px-4 py-3 text-center hidden md:table-cell">
                      <span
                        className={cn(
                          "font-bold",
                          item.availableQuantity <= 2
                            ? "text-red-500"
                            : item.availableQuantity <= 5
                              ? "text-amber-500"
                              : "text-gray-700",
                        )}
                      >
                        {item.availableQuantity}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-center hidden lg:table-cell">
                      <p className="text-xs text-gray-500">
                        {new Date(item.saleEndTime).toLocaleDateString(
                          "en-US",
                          {
                            month: "short",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                          },
                        )}
                      </p>
                    </td>
                    <td className="px-4 py-3 text-center">
                      <FoodItemStatusBadge status={item.status} />
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1">
                        {/* Toggle active/inactive */}
                        {item.status === "ACTIVE" ? (
                          <button
                            onClick={() => deactivateMutation.mutate(item.id)}
                            disabled={deactivateMutation.isPending}
                            title="Deactivate"
                            className="w-7 h-7 rounded-lg grid place-items-center text-amber-500 hover:bg-amber-50 transition-colors"
                          >
                            <LightningSlash size={15} weight="bold" />
                          </button>
                        ) : item.status === "INACTIVE" ||
                          item.status === "PENDING" ? (
                          <button
                            onClick={() => activateMutation.mutate(item.id)}
                            disabled={activateMutation.isPending}
                            title="Activate"
                            className="w-7 h-7 rounded-lg grid place-items-center text-green-600 hover:bg-green-50 transition-colors"
                          >
                            <Lightning size={15} weight="bold" />
                          </button>
                        ) : null}

                        <button
                          onClick={() => openEdit(item)}
                          title="Edit"
                          className="w-7 h-7 rounded-lg grid place-items-center text-gray-400 hover:bg-gray-100 hover:text-gray-700 transition-colors"
                        >
                          <PencilSimple size={14} weight="bold" />
                        </button>

                        <button
                          onClick={() => setDeleteTarget(item)}
                          title="Delete"
                          className="w-7 h-7 rounded-lg grid place-items-center text-gray-400 hover:bg-red-50 hover:text-red-500 transition-colors"
                        >
                          <Trash size={14} weight="bold" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Form drawer */}
      {selectedStore && (
        <FoodItemFormModal
          open={formOpen}
          onClose={closeForm}
          storeId={selectedStore}
          editItem={editItem}
        />
      )}

      {/* Delete confirm */}
      <AnimatePresence>
        {deleteTarget && (
          <DeleteDialog
            item={deleteTarget}
            onConfirm={() => deleteMutation.mutate(deleteTarget.id)}
            onCancel={() => setDeleteTarget(null)}
            loading={deleteMutation.isPending}
          />
        )}
      </AnimatePresence>
    </div>
  );
}
