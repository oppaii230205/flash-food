# 🚀 Flash-Food - Next Implementation Steps

## 📝 Implementation Roadmap

Dưới đây là các bước tiếp theo để hoàn thiện dự án Flash-Food:

---

## Phase 1: Core Authentication & Authorization (Week 1-2)

### 1.1 JWT Authentication Setup

**Priority:** 🔴 CRITICAL

**Files to create:**

```
src/main/java/com/flashfood/flash_food/
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── UserDetailsServiceImpl.java
└── service/
    └── AuthService.java
```

**Implementation Tasks:**

- [ ] Create `JwtTokenProvider` for token generation/validation
- [ ] Implement `JwtAuthenticationFilter` to intercept requests
- [ ] Create `UserDetailsServiceImpl` để load user từ DB
- [ ] Update `SecurityConfig` để add JWT filter
- [ ] Implement login endpoint
- [ ] Implement register endpoint
- [ ] Test authentication flow

**Sample Code Structure:**

```java
@Service
public class JwtTokenProvider {
    private String jwtSecret = "your-secret-key";
    private long jwtExpiration = 86400000; // 24h

    public String generateToken(String email) { }
    public boolean validateToken(String token) { }
    public String getUsernameFromToken(String token) { }
}
```

---

## Phase 2: Service Layer Implementation (Week 2-3)

### 2.1 User Service

**Files:**

- `UserService.java`
- `UserServiceImpl.java`

**Methods to implement:**

```java
- UserResponse createUser(RegisterRequest request)
- UserResponse updateProfile(Long userId, UpdateUserRequest request)
- UserResponse getUserById(Long userId)
- void updateLocation(Long userId, LocationUpdateRequest request)
- List<UserResponse> getAllUsers(Pageable pageable)
```

### 2.2 Store Service

**Files:**

- `StoreService.java`
- `StoreServiceImpl.java`

**Methods to implement:**

```java
- StoreResponse createStore(StoreRequest request)
- StoreResponse updateStore(Long storeId, StoreRequest request)
- StoreResponse getStoreById(Long storeId)
- List<StoreResponse> getNearbyStores(Double lat, Double lon, Double radius)
- List<StoreResponse> searchStores(String keyword)
- void activateStore(Long storeId)
- void deactivateStore(Long storeId)
```

### 2.3 Food Item Service

**Files:**

- `FoodItemService.java`
- `FoodItemServiceImpl.java`

**Methods to implement:**

```java
- FoodItemResponse createFoodItem(Long storeId, FoodItemRequest request)
- FoodItemResponse updateFoodItem(Long id, FoodItemRequest request)
- FoodItemResponse getFoodItemById(Long id)
- List<FoodItemResponse> getAvailableFoodItems()
- List<FoodItemResponse> getFoodItemsByStore(Long storeId)
- void decrementQuantity(Long id, Integer quantity) // with distributed lock
```

**Critical Implementation - High Concurrency:**

```java
@Transactional
public OrderResponse createOrder(CreateOrderRequest request) {
    String lockKey = "order:" + userId + ":" + System.currentTimeMillis();
    String lockValue = UUID.randomUUID().toString();

    // Try to acquire distributed lock
    if (!redisLockService.tryLock(lockKey, lockValue, 10)) {
        throw new InvalidOperationException("Too many requests. Please try again.");
    }

    try {
        // Get food item with pessimistic lock
        FoodItem foodItem = foodItemRepository.findByIdWithLock(foodItemId)
            .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", foodItemId));

        // Check quantity
        if (foodItem.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                foodItem.getName(),
                quantity,
                foodItem.getAvailableQuantity()
            );
        }

        // Atomically decrement quantity
        int updated = foodItemRepository.decrementQuantity(foodItemId, quantity);
        if (updated == 0) {
            throw new InsufficientStockException("Failed to reserve items");
        }

        // Create order
        Order order = createOrderEntity(request);
        orderRepository.save(order);

        return mapToResponse(order);

    } finally {
        // Always release lock
        redisLockService.releaseLock(lockKey, lockValue);
    }
}
```

### 2.4 Order Service

**Files:**

- `OrderService.java`
- `OrderServiceImpl.java`

**Methods to implement:**

```java
- OrderResponse createOrder(Long userId, CreateOrderRequest request)
- OrderResponse getOrderById(Long orderId)
- List<OrderResponse> getUserOrders(Long userId, Pageable pageable)
- List<OrderResponse> getStoreOrders(Long storeId, OrderStatus status)
- void updateOrderStatus(Long orderId, OrderStatus newStatus)
- void cancelOrder(Long orderId, String reason)
```

### 2.5 Notification Service

**Files:**

- `NotificationService.java`
- `NotificationServiceImpl.java`

**Methods to implement:**

```java
- void notifyNearbyUsers(Long storeId, String title, String message)
- void notifyUser(Long userId, NotificationMessage message)
- List<NotificationResponse> getUserNotifications(Long userId, Pageable pageable)
- Long getUnreadCount(Long userId)
- void markAsRead(Long notificationId)
- void markAllAsRead(Long userId)
```

---

## Phase 3: Controller Layer Implementation (Week 3-4)

### 3.1 Auth Controller

**File:** `AuthController.java`

**Endpoints:**

```java
POST   /api/v1/auth/register        - Register new user
POST   /api/v1/auth/login           - Login user
POST   /api/v1/auth/refresh-token   - Refresh JWT token
POST   /api/v1/auth/logout          - Logout user
GET    /api/v1/auth/me              - Get current user info
```

### 3.2 User Controller

**File:** `UserController.java`

**Endpoints:**

```java
GET    /api/v1/users/{id}              - Get user by ID
PUT    /api/v1/users/{id}              - Update user profile
PUT    /api/v1/users/{id}/location     - Update user location
GET    /api/v1/users/{id}/orders       - Get user's orders
GET    /api/v1/users/{id}/notifications - Get user's notifications
```

### 3.3 Store Controller

**File:** `StoreController.java`

**Endpoints:**

```java
POST   /api/v1/stores                  - Create store (STORE_OWNER)
GET    /api/v1/stores/{id}             - Get store details
PUT    /api/v1/stores/{id}             - Update store (STORE_OWNER)
DELETE /api/v1/stores/{id}             - Delete store (ADMIN)
GET    /api/v1/stores/nearby           - Get nearby stores
GET    /api/v1/stores/search           - Search stores
GET    /api/v1/stores/{id}/food-items  - Get store's food items
```

### 3.4 Food Item Controller

**File:** `FoodItemController.java`

**Endpoints:**

```java
POST   /api/v1/food-items              - Create food item (STORE_OWNER)
GET    /api/v1/food-items/{id}         - Get food item
PUT    /api/v1/food-items/{id}         - Update food item (STORE_OWNER)
DELETE /api/v1/food-items/{id}         - Delete food item (STORE_OWNER)
GET    /api/v1/food-items/available    - Get all available items
GET    /api/v1/food-items/flash-sale   - Get current flash sales
```

### 3.5 Order Controller

**File:** `OrderController.java`

**Endpoints:**

```java
POST   /api/v1/orders                  - Create order
GET    /api/v1/orders/{id}             - Get order details
GET    /api/v1/orders                  - Get user's orders
PUT    /api/v1/orders/{id}/status      - Update order status (STORE_OWNER)
POST   /api/v1/orders/{id}/cancel      - Cancel order
```

**Sample Controller Implementation:**

```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
        @Valid @RequestBody CreateOrderRequest request,
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        OrderResponse order = orderService.createOrder(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}
```

---

## Phase 4: Testing (Week 4-5)

### 4.1 Unit Tests

**Test files to create:**

```
src/test/java/com/flashfood/flash_food/
├── service/
│   ├── AuthServiceTest.java
│   ├── OrderServiceTest.java
│   └── FoodItemServiceTest.java
└── repository/
    ├── UserRepositoryTest.java
    └── FoodItemRepositoryTest.java
```

**Sample Test:**

```java
@SpringBootTest
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private RedisLockService redisLockService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_WithSufficientStock_ShouldSucceed() {
        // Given
        CreateOrderRequest request = ...;
        when(redisLockService.tryLock(...)).thenReturn(true);
        when(foodItemRepository.findByIdWithLock(...)).thenReturn(Optional.of(foodItem));

        // When
        OrderResponse result = orderService.createOrder(1L, request);

        // Then
        assertNotNull(result);
        verify(orderRepository).save(any());
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        // Given
        when(foodItemRepository.findByIdWithLock(...)).thenReturn(Optional.of(soldOutItem));

        // When & Then
        assertThrows(InsufficientStockException.class,
            () -> orderService.createOrder(1L, request)
        );
    }
}
```

### 4.2 Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createOrder_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

### 4.3 Load Testing (Concurrency Test)

Use JMeter or Gatling to test:

- 100 concurrent users trying to buy the last item
- Should not oversell (quantity should be exactly handled)

---

## Phase 5: Advanced Features (Week 5-6)

### 5.1 Real-time Features (WebSocket)

- [ ] Add WebSocket support
- [ ] Real-time order status updates
- [ ] Real-time flash sale countdown
- [ ] Live notification push

### 5.2 Payment Integration

- [ ] MoMo payment gateway
- [ ] ZaloPay integration
- [ ] Webhook handlers for payment callbacks

### 5.3 File Upload

- [ ] Store image upload
- [ ] Food item image upload
- [ ] User avatar upload
- [ ] AWS S3 or local storage

### 5.4 Analytics & Reporting

- [ ] Store sales dashboard
- [ ] User purchase history
- [ ] Popular items analytics
- [ ] Revenue reports

---

## 🎯 Priority Order

### MUST HAVE (Phase 1-3)

1. ✅ Authentication/Authorization
2. ✅ Order creation with concurrency control
3. ✅ Flash sale item management
4. ✅ Notification system
5. ✅ Location-based search

### SHOULD HAVE (Phase 4-5)

6. ⏳ Payment integration
7. ⏳ Real-time updates
8. ⏳ File upload
9. ⏳ Testing coverage > 80%

### NICE TO HAVE (Future)

10. ⬜ Admin dashboard
11. ⬜ Analytics & reporting
12. ⬜ Mobile app
13. ⬜ Rating & review system

---

## 📊 Performance Targets

| Metric                    | Target       |
| ------------------------- | ------------ |
| API Response Time (avg)   | < 200ms      |
| API Response Time (p95)   | < 500ms      |
| Database Query Time       | < 50ms       |
| Redis Operation Time      | < 10ms       |
| Concurrent Order Handling | 1000 req/sec |
| Notification Delivery     | < 2 seconds  |
| System Uptime             | 99.9%        |

---

## 🔍 Code Quality Checklist

Before pushing code:

- [ ] All tests pass
- [ ] Code coverage > 80%
- [ ] No critical SonarQube issues
- [ ] All endpoints documented in Swagger
- [ ] Proper error handling
- [ ] Input validation added
- [ ] Logging added for important operations
- [ ] Security reviewed
- [ ] Performance tested

---

## 📚 Learning While Building

### Week 1-2: Authentication

**Learn:**

- JWT token structure
- Spring Security filter chain
- BCrypt password hashing
- Role-based access control

**Resources:**

- Spring Security Reference
- JWT.io

### Week 2-3: High Concurrency

**Learn:**

- Distributed locks with Redis
- Pessimistic vs Optimistic locking
- Transaction isolation levels
- Race condition prevention

**Resources:**

- Redis University
- Database Locking Patterns

### Week 3-4: Messaging

**Learn:**

- RabbitMQ patterns
- Async processing
- Message acknowledgment
- Dead letter queues

**Resources:**

- RabbitMQ Tutorials
- Spring AMQP Docs

### Week 4-5: Testing

**Learn:**

- Unit testing with JUnit 5
- Mocking with Mockito
- Integration testing
- Load testing with JMeter

**Resources:**

- JUnit 5 User Guide
- Mockito Documentation

---

## 🚀 Getting Started NOW

1. **Clone the template you just created**
2. **Start with Authentication:**
   ```bash
   # Create JWT provider first
   touch src/main/java/com/flashfood/flash_food/security/JwtTokenProvider.java
   ```
3. **Test as you go:**
   - Write test first (TDD approach if possible)
   - Implement feature
   - Test manually with Postman/Swagger

4. **Commit frequently:**
   ```bash
   git add .
   git commit -m "feat: implement user authentication"
   git push
   ```

---

**Good luck! 🎉 You have a solid foundation to build upon!**
