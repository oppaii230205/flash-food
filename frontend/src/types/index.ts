// ─── Generic Wrapper ─────────────────────────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  httpCode: number;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// ─── Enums (matching backend display names) ──────────────────────────────────
export type UserRole = "CUSTOMER" | "STORE_OWNER" | "ADMIN";
export type UserStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED" | "DELETED";

export type StoreType =
  | "RESTAURANT"
  | "BAKERY"
  | "CAFE"
  | "GROCERY"
  | "FOOD_TRUCK"
  | "OTHER";
export type StoreStatus =
  | "PENDING_APPROVAL"
  | "ACTIVE"
  | "INACTIVE"
  | "SUSPENDED"
  | "REJECTED";

export type FoodItemStatus =
  | "PENDING"
  | "ACTIVE"
  | "SOLD_OUT"
  | "EXPIRED"
  | "INACTIVE"
  | "DELETED";

export type OrderStatus =
  | "PENDING"
  | "CONFIRMED"
  | "PREPARING"
  | "READY"
  | "COMPLETED"
  | "CANCELLED";
export type PaymentStatus = "PENDING" | "COMPLETED" | "FAILED" | "REFUNDED";
export type PaymentMethod =
  | "CASH"
  | "CREDIT_CARD"
  | "DEBIT_CARD"
  | "DIGITAL_WALLET";

// ─── Auth ─────────────────────────────────────────────────────────────────────
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phoneNumber: string;
  latitude?: number;
  longitude?: number;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: UserResponse;
}

// ─── User ─────────────────────────────────────────────────────────────────────
export interface UserResponse {
  id: number;
  email: string;
  fullName: string;
  phoneNumber?: string;
  address?: string;
  avatarUrl?: string;
  latitude?: number;
  longitude?: number;
  notificationEnabled?: boolean;
  notificationRadius?: number;
  /** Single role string as serialized by the backend (e.g. "CUSTOMER", "STORE_OWNER", "ADMIN") */
  role: string;
  status: string;
  createdAt: string;
}

export interface UpdateProfileRequest {
  email?: string;
  fullName?: string;
  phoneNumber?: string;
  address?: string;
  avatarUrl?: string;
  notificationEnabled?: boolean;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UpdateLocationRequest {
  latitude: number;
  longitude: number;
  notificationRadius?: number;
}

// ─── Category ─────────────────────────────────────────────────────────────────
export interface CategoryResponse {
  id: number;
  name: string;
  slug: string;
  description?: string;
  /** Icon URL (field name on backend is iconUrl, not imageUrl) */
  iconUrl?: string;
  displayOrder: number;
  isActive: boolean;
  parentId?: number;
  parentName?: string;
  /** Nesting depth: 0 = root, 1 = child, etc. */
  level?: number;
}

export interface CategoryRequest {
  name: string;
  slug?: string;
  description?: string;
  iconUrl?: string;
  displayOrder?: number;
  isActive?: boolean;
  parentId?: number;
}

// ─── Store ────────────────────────────────────────────────────────────────────
export interface StoreResponse {
  id: number;
  name: string;
  address: string;
  phoneNumber: string;
  latitude: number;
  longitude: number;
  type: StoreType;
  description?: string;
  imageUrl?: string;
  openTime?: string;
  closeTime?: string;
  flashSaleTime?: string;
  status: StoreStatus;
  rating: number;
  totalRatings: number;
  ownerId: number;
  ownerName?: string;
  /** Whether the store is currently within its declared operating hours */
  isOpen?: boolean;
  /** Distance from the requester in metres (only for nearby queries) */
  distance?: number;
  createdAt: string;
}

export interface CreateStoreRequest {
  name: string;
  address: string;
  phoneNumber: string;
  latitude: number;
  longitude: number;
  type: StoreType;
  description?: string;
  imageUrl?: string;
  openTime?: string;
  closeTime?: string;
  flashSaleTime?: string;
}

export interface UpdateStoreRequest extends Partial<CreateStoreRequest> {}

// ─── Food Item ────────────────────────────────────────────────────────────────
export interface FoodItemResponse {
  id: number;
  storeId: number;
  storeName: string;
  categoryId?: number;
  categoryName?: string;
  categorySlug?: string;
  name: string;
  description?: string;
  imageUrl?: string;
  originalPrice: number;
  flashPrice: number;
  discountPercentage: number;
  totalQuantity: number;
  availableQuantity: number;
  saleStartTime: string;
  saleEndTime: string;
  status: FoodItemStatus;
  isExpired: boolean;
  /** True when item is available, in stock, and within its sale window right now */
  isAvailable?: boolean;
  /** Seconds until sale starts; null if already started or expired */
  timeUntilSaleStart?: number;
  /** Seconds until sale ends; null if not yet started or expired */
  timeUntilSaleEnd?: number;
  createdAt: string;
  updatedAt?: string;
}

export interface FoodItemRequest {
  name: string;
  description?: string;
  imageUrl?: string;
  originalPrice: number;
  flashPrice: number;
  quantity: number;
  saleStartTime: string;
  saleEndTime: string;
  categoryId?: number;
}

export interface FoodItemQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  storeId?: number;
  categoryId?: number;
  status?: FoodItemStatus;
  keyword?: string;
}

// ─── Order ────────────────────────────────────────────────────────────────────
export interface OrderItemResponse {
  id: number;
  foodItemId: number;
  foodItemName: string;
  /** Backend field name is foodItemImage (no URL suffix) */
  foodItemImage?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface OrderResponse {
  id: number;
  orderNumber: string;
  userId: number;
  storeId: number;
  storeName: string;
  storeAddress?: string;
  items: OrderItemResponse[];
  totalAmount: number;
  originalAmount: number;
  status: OrderStatus;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  pickupTime?: string;
  specialInstructions?: string;
  cancellationReason?: string;
  cancelledAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderRequest {
  storeId: number;
  items: { foodItemId: number; quantity: number }[];
  paymentMethod: string;
  pickupTime?: string;
  specialInstructions?: string;
}

export interface CancelOrderRequest {
  reason?: string;
}

// ─── Query Params ─────────────────────────────────────────────────────────────
export interface StoreQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  keyword?: string;
  type?: StoreType;
  lat?: number;
  lon?: number;
  radius?: number;
}

export interface OrderQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  status?: OrderStatus;
  storeId?: number;
}
