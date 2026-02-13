# Flash-Food Backend - Package Structure

## 📦 Tổng Quan Package Structure

Project được tổ chức theo **Clean Architecture** và **Domain-Driven Design** principles:

```
com.flashfood.flash_food/
│
├── 📁 entity/                      # Domain Entities (JPA Entities)
│   ├── User.java
│   ├── Store.java
│   ├── FoodItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Notification.java
│   └── [Enums]                     # All enum types
│
├── 📁 repository/                  # Data Access Layer
│   ├── UserRepository.java
│   ├── StoreRepository.java
│   ├── FoodItemRepository.java    # Includes pessimistic locking
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   └── NotificationRepository.java
│
├── 📁 dto/                         # Data Transfer Objects
│   ├── request/                    # Request DTOs
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── StoreRequest.java
│   │   ├── FoodItemRequest.java
│   │   ├── CreateOrderRequest.java
│   │   └── LocationUpdateRequest.java
│   ├── response/                   # Response DTOs
│   │   ├── ApiResponse.java       # Generic API wrapper
│   │   ├── AuthResponse.java
│   │   ├── UserResponse.java
│   │   ├── StoreResponse.java
│   │   ├── FoodItemResponse.java
│   │   ├── OrderResponse.java
│   │   └── NotificationResponse.java
│   └── message/                    # RabbitMQ Message DTOs
│       └── NotificationMessage.java
│
├── 📁 service/                     # Business Logic Layer
│   ├── MessagePublisher.java      # RabbitMQ Producer
│   ├── NotificationConsumer.java  # RabbitMQ Consumer
│   ├── RedisGeoService.java       # Redis Geo-spatial operations
│   └── RedisLockService.java      # Distributed locking
│
├── 📁 controller/                  # REST API Controllers
│   └── [TODO: Implement controllers]
│
├── 📁 config/                      # Configuration Classes
│   ├── RedisConfig.java           # Redis + Caching setup
│   ├── RabbitMQConfig.java        # RabbitMQ queues/exchanges
│   ├── SecurityConfig.java        # Spring Security config
│   ├── AsyncConfig.java           # Async processing
│   └── OpenAPIConfig.java         # Swagger documentation
│
├── 📁 scheduler/                   # Scheduled Tasks
│   └── StatusUpdateScheduler.java # Auto-expire items/orders
│
├── 📁 exception/                   # Exception Handling
│   ├── FlashFoodException.java
│   ├── ResourceNotFoundException.java
│   ├── ResourceAlreadyExistsException.java
│   ├── InsufficientStockException.java
│   ├── InvalidOperationException.java
│   └── GlobalExceptionHandler.java
│
└── 📁 util/                        # Utility Classes
    ├── AppConstants.java
    └── HelperUtils.java

```

---

## 🎯 Các Tính Năng Kỹ Thuật Đã Implement

### 1. **Redis Geo-spatial** 🗺️

**File:** `RedisGeoService.java`

```java
// Tìm cửa hàng gần user
List<Long> storeIds = redisGeoService.findNearbyStores(longitude, latitude, 1.0);

// Tìm user gần cửa hàng (để gửi notification)
List<Long> userIds = redisGeoService.findNearbyUsers(storeLon, storeLat, 1.0);
```

**Ứng dụng:**

- User đứng ở KTX A chỉ nhận thông báo từ quán trong bán kính 1km
- Tốc độ tra cứu cực nhanh nhờ Redis Geo

---

### 2. **High Concurrency - Distributed Lock** 🔒

**File:** `RedisLockService.java` + `FoodItemRepository.java`

```java
// Pessimistic Lock trong database
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<FoodItem> findByIdWithLock(Long id);

// Distributed Lock với Redis
boolean locked = redisLockService.tryLock("food_item:" + id, uuid);
try {
    // Critical section - update quantity
} finally {
    redisLockService.releaseLock("food_item:" + id, uuid);
}
```

**Ứng dụng:**

- 100 người cùng mua 1 cái bánh mì cuối cùng
- Đảm bảo không bị overselling

---

### 3. **RabbitMQ - Message Queue** 📨

**Files:** `RabbitMQConfig.java`, `MessagePublisher.java`, `NotificationConsumer.java`

```java
// Publisher: Gửi thông báo flash sale
NotificationMessage msg = NotificationMessage.builder()
    .userIds(nearbyUserIds)
    .title("Flash Sale 70% OFF!")
    .build();
messagePublisher.publishFlashSaleNotification(msg);

// Consumer: Lắng nghe và lưu vào DB
@RabbitListener(queues = FLASH_SALE_QUEUE)
public void handleFlashSaleNotification(NotificationMessage msg) {
    // Save to database
}
```

**Ứng dụng:**

- Bắn notification cho 5,000 user cùng lúc mà không làm sập server
- Xử lý đơn hàng bất đồng bộ

---

### 4. **Spring Scheduler - Auto Tasks** ⏰

**File:** `StatusUpdateScheduler.java`

```java
// Chạy mỗi 5 phút
@Scheduled(cron = "0 */5 * * * *")
public void markExpiredFoodItems() {
    // Tự động chuyển status sang EXPIRED nếu quá giờ
}

// Chạy mỗi 10 phút
@Scheduled(cron = "0 */10 * * * *")
public void expireUnclaimedOrders() {
    // Hủy đơn hàng không được nhận sau 2 giờ
}
```

**Ứng dụng:**

- Tự động hủy món ăn hết hạn
- Tự động expire đơn hàng không được pickup

---

## 🔧 Tech Stack

| Technology            | Purpose                                  |
| --------------------- | ---------------------------------------- |
| **Spring Boot 4.0.2** | Core framework                           |
| **PostgreSQL**        | Main database                            |
| **Redis**             | Caching + Geo-spatial + Distributed Lock |
| **RabbitMQ**          | Message queue for notifications          |
| **Spring Data JPA**   | ORM with Hibernate                       |
| **Spring Security**   | Authentication & Authorization           |
| **Lombok**            | Reduce boilerplate code                  |
| **Swagger/OpenAPI**   | API documentation                        |

---

## 🚀 Next Steps (Để Implement)

### 1. **Service Layer** (Business Logic)

Cần tạo các service classes như:

- `UserService.java`
- `AuthService.java`
- `StoreService.java`
- `FoodItemService.java`
- `OrderService.java`

### 2. **Controller Layer** (REST API)

Cần tạo các REST controllers:

- `AuthController.java` - `/api/auth/**`
- `UserController.java` - `/api/users/**`
- `StoreController.java` - `/api/stores/**`
- `FoodItemController.java` - `/api/food-items/**`
- `OrderController.java` - `/api/orders/**`

### 3. **JWT Authentication**

- `JwtTokenProvider.java`
- `JwtAuthenticationFilter.java`
- `UserDetailsServiceImpl.java`

### 4. **Testing**

- Unit tests
- Integration tests
- Performance tests cho high concurrency

---

## 📝 Application Properties

File `application.properties` đã được cấu hình đầy đủ cho:

- Database connection (PostgreSQL)
- Redis connection
- RabbitMQ connection
- Logging
- Swagger UI
- Custom properties

**Để chạy ứng dụng, cần:**

1. PostgreSQL chạy tại `localhost:5432`
2. Redis chạy tại `localhost:6379`
3. RabbitMQ chạy tại `localhost:5672`

---

## 🎨 Best Practices Đã Áp Dụng

✅ **Separation of Concerns** - Rõ ràng giữa các layer  
✅ **DTOs** - Không expose entities ra ngoài API  
✅ **Global Exception Handling** - Xử lý lỗi tập trung  
✅ **Validation** - Bean Validation với `@Valid`  
✅ **Pessimistic Locking** - Tránh race condition  
✅ **Distributed Lock** - Redis lock cho critical sections  
✅ **Async Processing** - RabbitMQ cho heavy tasks  
✅ **Scheduled Tasks** - Auto cleanup và status update  
✅ **Configuration Management** - Tách biệt config  
✅ **API Documentation** - Swagger/OpenAPI

---

## 📚 Tài Liệu Tham Khảo

- Redis Geo: https://redis.io/docs/data-types/geospatial/
- RabbitMQ: https://www.rabbitmq.com/tutorials/tutorial-one-spring-amqp.html
- Spring Scheduler: https://spring.io/guides/gs/scheduling-tasks/
- JPA Locking: https://www.baeldung.com/jpa-pessimistic-locking

---

**Created by:** Flash-Food Development Team  
**Last Updated:** 2026-02-13
