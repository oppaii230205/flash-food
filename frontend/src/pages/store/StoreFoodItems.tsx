import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  Plus,
  MagnifyingGlass,
  PencilSimple,
  Trash,
  Lightning,
  LightningSlash,
  SmileyMeh,
  X,
  Storefront,
  Tag,
  Clock,
  ArrowsClockwise,
  CheckCircle,
  Warning,
} from "@phosphor-icons/react";
import { AnimatePresence, motion } from "framer-motion";

import { storesApi } from "@/api/stores.api";
import { dealsApi } from "@/api/deals.api";
import { categoriesApi } from "@/api/categories.api";
import { cn } from "@/utils/cn";
import { FoodItemStatusBadge } from "./StoreDashboard";
import type {
  CategoryResponse,
  FoodItemResponse,
  FoodItemStatus,
  StoreResponse,
} from "@/types";
import toast from "react-hot-toast";

// --- Helpers ---
function fmtVND(amount: number) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(amount);
}

function fmtDate(iso: string) {
  return new Date(iso).toLocaleString("vi-VN", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

// --- Form schema ---
const foodItemSchema = z
  .object({
    name: z.string().min(2, "Name is required"),
    description: z.string().optional(),
    imageUrl: z.string().url("Must be a valid URL").optional().or(z.literal("")),
    originalPrice: z.coerce.number().min(1000, "Min original price: 1,000 VND"),
    flashPrice: z.coerce.number().min(1000, "Min flash price: 1,000 VND"),
    quantity: z.coerce.number().int().min(1, "Quantity must be at least 1"),
    saleStartTime: z.string().min(1, "Sale start time is required"),
    saleEndTime: z.string().min(1, "Sale end time is required"),
    categoryId: z.coerce.number().min(1, "Please select a category"),
  })
  .refine((d) => d.flashPrice < d.originalPrice, {
    message: "Flash price must be lower than original price",
    path: ["flashPrice"],
  })
  .refine((d) => new Date(d.saleEndTime) > new Date(d.saleStartTime), {
    message: "End time must be after start time",
    path: ["saleEndTime"],
  });
type FoodItemFormData = z.infer<typeof foodItemSchema>;

const fieldCls = (err?: boolean) =>
  cn(
    "w-full rounded-lg border px-3 py-2.5 text-sm outline-none transition-all placeholder:text-gray-300",
    "focus:ring-2 focus:ring-green-100 focus:border-green-400",
    err
      ? "border-red-300 bg-red-50/40 focus:border-red-400 focus:ring-red-50"
      : "border-gray-200 bg-white",
  );

const STATUS_FILTERS: { label: string; value: FoodItemStatus | "" }[] = [
  { label: "All", value: "" },
  { label: "Active", value: "ACTIVE" },
  { label: "Pending", value: "PENDING" },
  { label: "Sold Out", value: "SOLD_OUT" },
  { label: "Expired", value: "EXPIRED" },
  { label: "Inactive", value: "INACTIVE" },
];

// --- Food Item Form Drawer ---
function FoodItemFormDrawer({
  open,
  onClose,
  storeId,
  editItem,
  categories,
}: {
  open: boolean;
  onClose: () => void;
  storeId: number;
  editItem?: FoodItemResponse | null;
  categories: CategoryResponse[];
}) {
  const qc = useQueryClient();
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting, isDirty },
    reset,
    watch,
  } = useForm<FoodItemFormData>({
    resolver: zodResolver(foodItemSchema),
    defaultValues: {
      name: "",
      description: "",
      imageUrl: "",
      originalPrice: 0,
      flashPrice: 0,
      quantity: 1,
      saleStartTime: "",
      saleEndTime: "",
      categoryId: 0,
    },
  });

  const watchedOriginal = watch("originalPrice");
  const watchedFlash = watch("flashPrice");
  const discount =
    watchedOriginal > 0 && watchedFlash > 0
      ? Math.round(((watchedOriginal - watchedFlash) / watchedOriginal) * 100)
      : 0;

  useEffect(() => {
    if (open && editItem) {
      reset({
        name: editItem.name,
        description: editItem.description ?? "",
        imageUrl: editItem.imageUrl ?? "",
        originalPrice: editItem.originalPrice,
        flashPrice: editItem.flashPrice,
        quantity: editItem.totalQuantity,
        saleStartTime: editItem.saleStartTime?.slice(0, 16) ?? "",
        saleEndTime: editItem.saleEndTime?.slice(0, 16) ?? "",
        categoryId: editItem.categoryId ?? 0,
      });
    } else if (open && !editItem) {
      reset({ name: "", description: "", imageUrl: "", originalPrice: 0, flashPrice: 0, quantity: 1, saleStartTime: "", saleEndTime: "", categoryId: 0 });
    }
  }, [editItem, open, reset]);

  const onSubmit = async (data: FoodItemFormData) => {
    const payload = {
      name: data.name,
      description: data.description || undefined,
      imageUrl: data.imageUrl || undefined,
      originalPrice: data.originalPrice,
      flashPrice: data.flashPrice,
      quantity: data.quantity,
      saleStartTime: new Date(data.saleStartTime).toISOString(),
      saleEndTime: new Date(data.saleEndTime).toISOString(),
      categoryId: data.categoryId,
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
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? "Something went wrong.";
      toast.error(msg);
    }
  };

  const FormLabel = ({ children, required }: { children: React.ReactNode; required?: boolean }) => (
    <label className="block text-xs font-bold text-gray-500 mb-1.5 uppercase tracking-wide">
      {children}{required && <span className="text-red-400 normal-case tracking-normal ml-1">*</span>}
    </label>
  );

  const FieldError = ({ msg }: { msg?: string }) =>
    msg ? (
      <p className="text-red-500 text-[0.7rem] mt-1 flex items-center gap-1">
        <Warning size={11} weight="fill" />{msg}
      </p>
    ) : null;

  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            key="overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.15 }}
            className="fixed inset-0 bg-black/40 backdrop-blur-[2px] z-40"
            onClick={onClose}
          />
          <motion.div
            key="drawer"
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", stiffness: 320, damping: 32 }}
            className="fixed inset-y-0 right-0 w-full max-w-lg bg-white shadow-2xl z-50 flex flex-col"
          >
            {/* Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 shrink-0">
              <div>
                <h2 className="font-extrabold text-gray-900">
                  {editItem ? "Edit Flash Deal" : "Add New Flash Deal"}
                </h2>
                <p className="text-xs text-gray-400 mt-0.5">
                  {editItem ? "Update deal details below." : "Create a flash sale item for your store."}
                </p>
              </div>
              <button onClick={onClose} className="w-8 h-8 rounded-full bg-gray-100 grid place-items-center text-gray-500 hover:bg-gray-200 transition-colors">
                <X size={14} weight="bold" />
              </button>
            </div>

            {/* Form body */}
            <div className="flex-1 overflow-y-auto p-6 space-y-5">
              {/* Name */}
              <div>
                <FormLabel required>Item Name</FormLabel>
                <input {...register("name")} placeholder="e.g. Banh mi thit nguoi" className={fieldCls(!!errors.name)} />
                <FieldError msg={errors.name?.message} />
              </div>

              {/* Category */}
              <div>
                <FormLabel required>Category</FormLabel>
                {categories.length === 0 ? (
                  <div className="flex items-center gap-2 px-3 py-2.5 rounded-lg border border-amber-200 bg-amber-50 text-xs text-amber-700">
                    <Warning size={13} weight="duotone" />
                    No categories available. Ask admin to create categories first.
                  </div>
                ) : (
                  <Controller
                    name="categoryId"
                    control={control}
                    render={({ field: f }) => (
                      <select {...f} value={f.value || ""} onChange={(e) => f.onChange(Number(e.target.value))} className={cn(fieldCls(!!errors.categoryId), "cursor-pointer")}>
                        <option value="">Select a category…</option>
                        {categories.map((cat) => (
                          <option key={cat.id} value={cat.id}>{cat.name}</option>
                        ))}
                      </select>
                    )}
                  />
                )}
                <FieldError msg={errors.categoryId?.message} />
              </div>

              {/* Description */}
              <div>
                <FormLabel>Description</FormLabel>
                <textarea {...register("description")} rows={2} placeholder="Brief description…" className={cn(fieldCls(), "resize-none")} />
              </div>

              {/* Image URL */}
              <div>
                <FormLabel>Image URL</FormLabel>
                <input {...register("imageUrl")} placeholder="https://…" className={fieldCls(!!errors.imageUrl)} />
                <FieldError msg={errors.imageUrl?.message} />
              </div>

              {/* Pricing */}
              <div>
                <FormLabel required>Pricing (VND)</FormLabel>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-[0.7rem] text-gray-400 mb-1">Original Price</label>
                    <input {...register("originalPrice")} type="number" step="1000" min="1000" placeholder="0" className={fieldCls(!!errors.originalPrice)} />
                    <FieldError msg={errors.originalPrice?.message} />
                  </div>
                  <div>
                    <label className="block text-[0.7rem] text-gray-400 mb-1">Flash Price</label>
                    <input {...register("flashPrice")} type="number" step="1000" min="1000" placeholder="0" className={fieldCls(!!errors.flashPrice)} />
                    <FieldError msg={errors.flashPrice?.message} />
                  </div>
                </div>
                {discount > 0 && (
                  <div className="mt-2 flex items-center gap-1.5 text-xs text-green-700 bg-green-50 px-2.5 py-1.5 rounded-lg w-fit">
                    <CheckCircle size={12} weight="fill" />
                    <strong>{discount}%</strong> off — saving {fmtVND(watchedOriginal - watchedFlash)}
                  </div>
                )}
              </div>

              {/* Quantity */}
              <div>
                <FormLabel required>Available Quantity</FormLabel>
                <input {...register("quantity")} type="number" min="1" placeholder="e.g. 10" className={fieldCls(!!errors.quantity)} />
                <FieldError msg={errors.quantity?.message} />
              </div>

              {/* Sale window */}
              <div>
                <FormLabel required>Sale Window</FormLabel>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-[0.7rem] text-gray-400 mb-1">Starts at</label>
                    <input {...register("saleStartTime")} type="datetime-local" className={fieldCls(!!errors.saleStartTime)} />
                    <FieldError msg={errors.saleStartTime?.message} />
                  </div>
                  <div>
                    <label className="block text-[0.7rem] text-gray-400 mb-1">Ends at</label>
                    <input {...register("saleEndTime")} type="datetime-local" className={fieldCls(!!errors.saleEndTime)} />
                    <FieldError msg={errors.saleEndTime?.message} />
                  </div>
                </div>
              </div>
            </div>

            {/* Footer actions */}
            <div className="shrink-0 px-6 py-4 border-t border-gray-100 flex gap-3">
              <button type="button" onClick={onClose} className="flex-1 px-4 py-2.5 rounded-lg border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors">
                Cancel
              </button>
              <button
                type="button"
                onClick={handleSubmit(onSubmit)}
                disabled={isSubmitting || (!isDirty && !!editItem)}
                className="flex-1 bg-green-600 hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-lg px-4 py-2.5 text-sm font-bold transition-colors flex items-center justify-center gap-2"
              >
                {isSubmitting && <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />}
                {isSubmitting ? (editItem ? "Saving…" : "Creating…") : (editItem ? "Save Changes" : "Create Deal")}
              </button>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}

// --- Delete confirm dialog ---
function DeleteDialog({
  item, onConfirm, onCancel, loading,
}: {
  item: FoodItemResponse;
  onConfirm: () => void;
  onCancel: () => void;
  loading: boolean;
}) {
  return (
    <div className="fixed inset-0 bg-black/40 backdrop-blur-[2px] z-50 flex items-center justify-center p-4">
      <motion.div
        initial={{ opacity: 0, scale: 0.96, y: 8 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.96, y: 8 }}
        transition={{ duration: 0.15 }}
        className="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm"
      >
        <div className="w-12 h-12 bg-red-50 rounded-2xl grid place-items-center mb-4">
          <Trash size={22} weight="duotone" className="text-red-500" />
        </div>
        <h3 className="font-extrabold text-gray-900 mb-1.5">Delete this deal?</h3>
        <p className="text-sm text-gray-500 mb-5 leading-relaxed">
          <strong className="text-gray-700">{item.name}</strong> will be permanently removed.
        </p>
        <div className="flex gap-2.5">
          <button onClick={onCancel} className="flex-1 px-4 py-2.5 rounded-xl border border-gray-200 text-sm font-semibold text-gray-600 hover:bg-gray-50 transition-colors">
            Cancel
          </button>
          <button onClick={onConfirm} disabled={loading} className="flex-1 px-4 py-2.5 rounded-xl bg-red-500 hover:bg-red-600 disabled:opacity-60 text-white text-sm font-bold transition-colors flex items-center justify-center gap-2">
            {loading && <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />}
            Delete
          </button>
        </div>
      </motion.div>
    </div>
  );
}

// --- Main Page ---
export function StoreFoodItems() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<FoodItemStatus | "">("");
  const [formOpen, setFormOpen] = useState(searchParams.get("action") === "create");
  const [editItem, setEditItem] = useState<FoodItemResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<FoodItemResponse | null>(null);
  const [selectedStore, setSelectedStore] = useState<number | null>(null);

  const qc = useQueryClient();

  const { data: stores } = useQuery({
    queryKey: ["my-stores"],
    queryFn: () => storesApi.getMyStores().then((r) => r.data.data),
  });
  const storeList: StoreResponse[] = stores ?? [];

  const { data: categories } = useQuery({
    queryKey: ["categories-active"],
    queryFn: () => categoriesApi.getActive().then((r) => r.data.data),
    staleTime: 5 * 60 * 1000,
  });
  const categoryList: CategoryResponse[] = categories ?? [];

  useEffect(() => {
    if (storeList.length > 0 && !selectedStore) {
      setSelectedStore(storeList[0].id);
    }
  }, [storeList, selectedStore]);

  const { data: itemsPage, isLoading } = useQuery({
    queryKey: ["store-food-items", selectedStore, statusFilter],
    queryFn: () =>
      dealsApi.getByStore(selectedStore!, { status: statusFilter || undefined, size: 100, sort: "createdAt,desc" }).then((r) => r.data.data),
    enabled: !!selectedStore,
  });

  const items: FoodItemResponse[] = (itemsPage?.content ?? []).filter(
    (i) => !search || i.name.toLowerCase().includes(search.toLowerCase()) || i.categoryName?.toLowerCase().includes(search.toLowerCase()),
  );

  const deleteMutation = useMutation({
    mutationFn: (id: number) => dealsApi.delete(id),
    onSuccess: () => { toast.success("Deal deleted."); qc.invalidateQueries({ queryKey: ["store-food-items"] }); setDeleteTarget(null); },
    onError: () => toast.error("Failed to delete."),
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: FoodItemStatus }) => dealsApi.updateStatus(id, status),
    onSuccess: (_, { status }) => {
      toast.success(status === "ACTIVE" ? "Deal is now live!" : "Deal deactivated.");
      qc.invalidateQueries({ queryKey: ["store-food-items"] });
    },
    onError: () => toast.error("Failed to update status."),
  });

  const openEdit = (item: FoodItemResponse) => { setEditItem(item); setFormOpen(true); };
  const closeForm = () => {
    setFormOpen(false); setEditItem(null);
    setSearchParams((p) => { p.delete("action"); return p; });
  };

  if (!isLoading && storeList.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center px-4">
        <div className="w-16 h-16 bg-gray-50 rounded-2xl grid place-items-center mb-4">
          <Storefront size={28} weight="duotone" className="text-gray-300" />
        </div>
        <p className="font-semibold text-gray-500">No store found</p>
        <p className="text-sm text-gray-400 mt-1">Create a store first to manage food items.</p>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-6xl space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center gap-3">
        <div className="flex-1">
          <h2 className="font-extrabold text-gray-900">Flash Deals</h2>
          <p className="text-xs text-gray-400 mt-0.5">Manage your flash sale items and their availability.</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={() => qc.invalidateQueries({ queryKey: ["store-food-items"] })} className="w-8 h-8 grid place-items-center rounded-lg border border-gray-200 text-gray-400 hover:bg-gray-50 hover:text-gray-700 transition-colors" title="Refresh">
            <ArrowsClockwise size={14} weight="bold" />
          </button>
          <button onClick={() => { setEditItem(null); setFormOpen(true); }} className="inline-flex items-center gap-2 bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg text-sm font-bold transition-colors">
            <Plus size={15} weight="bold" /> Add Deal
          </button>
        </div>
      </div>

      {/* Store tabs — only if multiple stores */}
      {storeList.length > 1 && (
        <div className="flex gap-2 overflow-x-auto pb-1">
          {storeList.map((s) => (
            <button key={s.id} onClick={() => setSelectedStore(s.id)}
              className={cn("px-3.5 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap border transition-colors",
                selectedStore === s.id ? "bg-green-600 text-white border-green-600" : "bg-white text-gray-600 border-gray-200 hover:border-green-300")}>
              {s.name}
            </button>
          ))}
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-2">
        <div className="relative max-w-xs flex-1">
          <MagnifyingGlass size={14} weight="bold" className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search name or category…"
            className="w-full pl-8 pr-9 py-2 rounded-lg border border-gray-200 text-sm outline-none focus:border-green-400 focus:ring-2 focus:ring-green-100 transition" />
          {search && (
            <button onClick={() => setSearch("")} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
              <X size={13} weight="bold" />
            </button>
          )}
        </div>
        <div className="flex gap-1.5 overflow-x-auto pb-0.5">
          {STATUS_FILTERS.map((f) => (
            <button key={f.value} onClick={() => setStatusFilter(f.value)}
              className={cn("px-3 py-2 rounded-lg text-xs font-semibold border whitespace-nowrap transition-colors",
                statusFilter === f.value ? "bg-green-600 text-white border-green-600" : "bg-white text-gray-600 border-gray-200 hover:border-green-300")}>
              {f.label}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-5 space-y-2.5">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-14 bg-gray-50 rounded-lg animate-pulse" />
            ))}
          </div>
        ) : items.length === 0 ? (
          <div className="flex flex-col items-center py-16 text-center px-4">
            {search || statusFilter ? (
              <>
                <MagnifyingGlass size={32} className="text-gray-300 mb-3" />
                <p className="font-semibold text-gray-500 text-sm">No results found</p>
                <p className="text-xs text-gray-400 mt-1">Try adjusting your filters.</p>
              </>
            ) : (
              <>
                <SmileyMeh size={36} className="text-gray-300 mb-3" />
                <p className="font-semibold text-gray-500 text-sm">No food items yet</p>
                <button onClick={() => setFormOpen(true)} className="mt-3 inline-flex items-center gap-1.5 bg-green-600 text-white px-4 py-2 rounded-full text-xs font-bold hover:bg-green-700 transition-colors">
                  <Plus size={13} weight="bold" /> Add First Deal
                </button>
              </>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100 bg-gray-50/60">
                  <th className="text-left px-4 py-3 text-[0.7rem] font-bold text-gray-400 uppercase tracking-wide">Item</th>
                  <th className="text-left px-4 py-3 text-[0.7rem] font-bold text-gray-400 uppercase tracking-wide hidden md:table-cell">Category</th>
                  <th className="text-right px-4 py-3 text-[0.7rem] font-bold text-gray-400 uppercase tracking-wide hidden sm:table-cell">Price</th>
                  <th className="text-center px-4 py-3 text-[0.7rem] font-bold text-gray-400 uppercase tracking-wide hidden md:table-cell">Stock</th>
                  <th className="text-center px-4 py-3 text-[0.7rem] font-bold text-gray-400 uppercase tracking-wide hidden lg:table-cell">Sale Ends</th>
                  <th className="text-center px-4 py-3 text-[0.7rem] font-bold text-gray-400 uppercase tracking-wide">Status</th>
                  <th className="text-right px-4 py-3 text-[0.7rem] font-bold text-gray-400 uppercase tracking-wide">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {items.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-lg bg-green-50 grid place-items-center shrink-0 overflow-hidden">
                          {item.imageUrl ? (
                            <img src={item.imageUrl} alt={item.name} className="w-9 h-9 object-cover" />
                          ) : (
                            <Lightning size={16} weight="fill" className="text-green-500" />
                          )}
                        </div>
                        <div className="min-w-0">
                          <p className="font-semibold text-gray-800 truncate max-w-[180px]">{item.name}</p>
                          <p className="text-xs text-gray-400 sm:hidden">{fmtVND(item.flashPrice)}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3 hidden md:table-cell">
                      {item.categoryName ? (
                        <span className="inline-flex items-center gap-1 text-xs text-gray-500">
                          <Tag size={11} weight="fill" className="text-gray-400" />{item.categoryName}
                        </span>
                      ) : <span className="text-xs text-gray-300">—</span>}
                    </td>
                    <td className="px-4 py-3 text-right hidden sm:table-cell">
                      <p className="font-bold text-gray-800">{fmtVND(item.flashPrice)}</p>
                      <p className="text-[0.7rem] text-gray-400 line-through">{fmtVND(item.originalPrice)}</p>
                      <p className="text-[0.65rem] font-bold text-green-600">-{item.discountPercentage}%</p>
                    </td>
                    <td className="px-4 py-3 text-center hidden md:table-cell">
                      <span className={cn("inline-flex items-center gap-0.5 text-sm font-bold px-2 py-0.5 rounded-md",
                        item.availableQuantity === 0 ? "text-red-600 bg-red-50" : item.availableQuantity <= 3 ? "text-amber-600 bg-amber-50" : "text-gray-700 bg-gray-100")}>
                        {item.availableQuantity}
                        <span className="text-[0.65rem] font-normal text-gray-400">/{item.totalQuantity}</span>
                      </span>
                    </td>
                    <td className="px-4 py-3 text-center hidden lg:table-cell">
                      <p className="text-xs text-gray-500 flex items-center justify-center gap-1">
                        <Clock size={11} weight="fill" className="text-gray-400" />{fmtDate(item.saleEndTime)}
                      </p>
                      {item.isExpired && <p className="text-[0.65rem] text-red-400 mt-0.5">Expired</p>}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <FoodItemStatusBadge status={item.status} />
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1">
                        {item.status === "ACTIVE" ? (
                          <button onClick={() => updateStatusMutation.mutate({ id: item.id, status: "INACTIVE" })}
                            disabled={updateStatusMutation.isPending} title="Deactivate"
                            className="w-7 h-7 rounded-lg grid place-items-center text-amber-500 hover:bg-amber-50 transition-colors disabled:opacity-50">
                            <LightningSlash size={14} weight="bold" />
                          </button>
                        ) : item.status === "INACTIVE" || item.status === "PENDING" ? (
                          <button onClick={() => updateStatusMutation.mutate({ id: item.id, status: "ACTIVE" })}
                            disabled={updateStatusMutation.isPending} title="Activate"
                            className="w-7 h-7 rounded-lg grid place-items-center text-green-600 hover:bg-green-50 transition-colors disabled:opacity-50">
                            <Lightning size={14} weight="bold" />
                          </button>
                        ) : null}
                        <button onClick={() => openEdit(item)} title="Edit"
                          className="w-7 h-7 rounded-lg grid place-items-center text-gray-400 hover:bg-gray-100 hover:text-gray-700 transition-colors">
                          <PencilSimple size={14} weight="bold" />
                        </button>
                        <button onClick={() => setDeleteTarget(item)} title="Delete"
                          className="w-7 h-7 rounded-lg grid place-items-center text-gray-400 hover:bg-red-50 hover:text-red-500 transition-colors">
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
        {itemsPage && itemsPage.totalElements > 100 && (
          <div className="px-4 py-3 border-t border-gray-50 text-xs text-gray-400 text-center">
            Showing first 100 of {itemsPage.totalElements} items.
          </div>
        )}
      </div>

      {selectedStore && (
        <FoodItemFormDrawer open={formOpen} onClose={closeForm} storeId={selectedStore} editItem={editItem} categories={categoryList} />
      )}

      <AnimatePresence>
        {deleteTarget && (
          <DeleteDialog item={deleteTarget} onConfirm={() => deleteMutation.mutate(deleteTarget.id)} onCancel={() => setDeleteTarget(null)} loading={deleteMutation.isPending} />
        )}
      </AnimatePresence>
    </div>
  );
}
