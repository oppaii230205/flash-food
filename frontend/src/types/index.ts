// ─── Generic Wrapper ─────────────────────────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean
  message?: string
  httpCode: number
  data: T
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// ─── Enums (matching backend display names) ──────────────────────────────────
export type UserRole   = 'CUSTOMER' | 'STORE_OWNER' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED'

export type StoreType   = 'RESTAURANT' | 'BAKERY' | 'CAFE' | 'GROCERY' | 'FOOD_TRUCK' | 'OTHER'
export type StoreStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'REJECTED'

export type FoodItemStatus = 'PENDING' | 'ACTIVE' | 'SOLD_OUT' | 'EXPIRED' | 'INACTIVE' | 'DELETED'

export type OrderStatus  = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CANCELLED'
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED'
export type PaymentMethod = 'CASH' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'DIGITAL_WALLET'

// ─── Auth ─────────────────────────────────────────────────────────────────────
export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  fullName: string
  phoneNumber: string
  latitude?: number
  longitude?: number
}

export interface AuthResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: UserResponse
}

// ─── User ─────────────────────────────────────────────────────────────────────
export interface UserResponse {
  id: number
  email: string
  roles: UserRole[]
  status: UserStatus
  fullName: string
  phoneNumber?: string
  address?: string
  avatarUrl?: string
  latitude?: number
  longitude?: number
  notificationEnabled?: boolean
  notificationRadius?: number
  createdAt: string
}

export interface UpdateProfileRequest {
  email?: string
  fullName?: string
  phoneNumber?: string
  address?: string
  avatarUrl?: string
  notificationEnabled?: boolean
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface UpdateLocationRequest {
  latitude: number
  longitude: number
  notificationRadius?: number
}

// ─── Category ─────────────────────────────────────────────────────────────────
export interface CategoryResponse {
  id: number
  name: string
  slug: string
  description?: string
  imageUrl?: string
  displayOrder: number
  active: boolean
  createdAt: string
}

export interface CategoryRequest {
  name: string
  slug?: string
  description?: string
  imageUrl?: string
  displayOrder?: number
  active?: boolean
}

// ─── Store ────────────────────────────────────────────────────────────────────
export interface StoreResponse {
  id: number
  name: string
  address: string
  phoneNumber: string
  latitude: number
  longitude: number
  type: StoreType
  description?: string
  imageUrl?: string
  openTime?: string
  closeTime?: string
  flashSaleTime?: string
  status: StoreStatus
  rating: number
  totalRatings: number
  ownerId: number
  ownerName?: string
  createdAt: string
  distanceKm?: number
}

export interface CreateStoreRequest {
  name: string
  address: string
  phoneNumber: string
  latitude: number
  longitude: number
  type: StoreType
  description?: string
  imageUrl?: string
  openTime?: string
  closeTime?: string
  flashSaleTime?: string
}

export interface UpdateStoreRequest extends Partial<CreateStoreRequest> {}

// ─── Food Item ────────────────────────────────────────────────────────────────
export interface FoodItemResponse {
  id: number
  storeId: number
  storeName: string
  storeAddress?: string
  categoryId?: number
  categoryName?: string
  name: string
  description?: string
  imageUrl?: string
  originalPrice: number
  flashPrice: number
  discountPercentage: number
  totalQuantity: number
  availableQuantity: number
  saleStartTime: string
  saleEndTime: string
  status: FoodItemStatus
  isExpired: boolean
  createdAt: string
}

export interface FoodItemRequest {
  name: string
  description?: string
  imageUrl?: string
  originalPrice: number
  flashPrice: number
  quantity: number
  saleStartTime: string
  saleEndTime: string
  categoryId?: number
}

export interface FoodItemQueryParams {
  page?: number
  size?: number
  sort?: string
  storeId?: number
  categoryId?: number
  status?: FoodItemStatus
  keyword?: string
}

// ─── Order ────────────────────────────────────────────────────────────────────
export interface OrderItemResponse {
  id: number
  foodItemId: number
  foodItemName: string
  foodItemImageUrl?: string
  quantity: number
  unitPrice: number
  subtotal: number
}

export interface OrderResponse {
  id: number
  orderNumber: string
  userId: number
  storeId: number
  storeName: string
  storeAddress?: string
  items: OrderItemResponse[]
  totalAmount: number
  originalAmount: number
  status: OrderStatus
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  pickupTime?: string
  specialInstructions?: string
  cancellationReason?: string
  cancelledAt?: string
  createdAt: string
  updatedAt: string
}

export interface CreateOrderRequest {
  storeId: number
  items: { foodItemId: number; quantity: number }[]
  paymentMethod: string
  pickupTime?: string
  specialInstructions?: string
}

export interface CancelOrderRequest {
  reason?: string
}

// ─── Query Params ─────────────────────────────────────────────────────────────
export interface StoreQueryParams {
  page?: number
  size?: number
  sort?: string
  keyword?: string
  type?: StoreType
  lat?: number
  lon?: number
  radius?: number
}

export interface OrderQueryParams {
  page?: number
  size?: number
  sort?: string
  status?: OrderStatus
}
