# Flash-Food System Architecture

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTS                                  │
│  (Web App, Mobile App, Admin Dashboard)                         │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     │ HTTPS/REST API
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT API                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Controllers  │  │  Security    │  │  Exception   │          │
│  │  (REST API)  │→ │   Filter     │→ │   Handler    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                                                        │
│         ▼                                                        │
│  ┌──────────────────────────────────────────────────┐          │
│  │           SERVICE LAYER (Business Logic)          │          │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐  │          │
│  │  │  User  │ │ Store  │ │  Food  │ │  Order   │  │          │
│  │  │Service │ │Service │ │ Service│ │ Service  │  │          │
│  │  └────────┘ └────────┘ └────────┘ └──────────┘  │          │
│  └──────────────────────────────────────────────────┘          │
│         │                      │                                │
│         ▼                      ▼                                │
│  ┌─────────────┐        ┌─────────────┐                        │
│  │ Repository  │        │   Redis     │                        │
│  │   Layer     │        │  Services   │                        │
│  └─────────────┘        └─────────────┘                        │
└────────┬────────────────────┬────────────────────┬─────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌────────────────┐   ┌───────────────┐   ┌──────────────────┐
│   PostgreSQL   │   │     Redis     │   │    RabbitMQ      │
│   (Main DB)    │   │  (Cache+Geo)  │   │  (Messaging)     │
└────────────────┘   └───────────────┘   └──────────────────┘
```

---

## 📊 Component Interaction Flow

### 1. Flash Sale Creation Flow

```
Store Owner → POST /api/v1/food-items
              ↓
       FoodItemService
              ↓
       ┌──────┴──────┐
       ▼             ▼
  Save to DB    Add to Redis Geo
       ▼             ▼
  PostgreSQL    Redis (location index)
       ▼
  Publish to RabbitMQ
       ▼
  NotificationConsumer
       ▼
  Find nearby users (Redis Geo)
       ▼
  Send notifications to users
```

### 2. High Concurrency Order Flow

```
User → POST /api/v1/orders
         ↓
    OrderService
         ↓
    Try acquire Redis Lock
         ↓
    ┌────┴────┐
    │ Locked? │
    └────┬────┘
         │ Yes
         ▼
    Get FoodItem with Pessimistic Lock
         ↓
    Check quantity
         ▼
    ┌─────────┴─────────┐
    │ Quantity > 0?     │
    └─────────┬─────────┘
              │ Yes
              ▼
    Decrement quantity atomically
              ▼
    Create Order
              ▼
    Release Redis Lock
              ▼
    Return Order Response
```

### 3. Notification System Flow

```
Flash Sale Created
       ↓
MessagePublisher
       ↓
RabbitMQ Exchange
       ↓
Flash Sale Queue
       ↓
NotificationConsumer
       ↓
┌──────┴──────┐
▼             ▼
Get Store   Redis Geo
Coordinates → Find nearby users
              ↓
       Save notifications to DB
              ↓
       Users get notified
```

---

## 🔄 Scheduled Tasks Flow

```
Every 5 minutes:
    ↓
StatusUpdateScheduler.markExpiredFoodItems()
    ↓
Find items where saleEndTime < now
    ↓
Set status = EXPIRED
    ↓
Update database


Every 10 minutes:
    ↓
StatusUpdateScheduler.expireUnclaimedOrders()
    ↓
Find orders (READY) where pickupTime < now - 2 hours
    ↓
Set status = EXPIRED
    ↓
Update database
```

---

## � Enum Conversion Pattern

Flash-Food implements a three-layer conversion system for enum types to achieve optimal performance, type safety, and API flexibility.

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                       CLIENT LAYER                               │
│  JSON: {"status": "active", "role": "customer"}                  │
│  Type: String (human-readable, version-independent)              │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTP Request
                         ▼
                    @JsonCreator
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND/ENTITY LAYER                          │
│  Enum: UserStatus.ACTIVE, UserRole.CUSTOMER                      │
│  Type: Java Enum (type-safe, compile-time checked)              │
└────────────────────────┬────────────────────────────────────────┘
                         │ JPA Converter
                         ▼
                 @Converter(autoApply=true)
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER                              │
│  Column: status = 1, role = 2                                    │
│  Type: Integer (compact, efficient indexing)                     │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation Details

#### 1. PersistableEnum Interface

All enums implement this interface for standardized conversion:

```java
public interface PersistableEnum<T> {
    T getValue();              // Returns Integer for DB storage
    String getDisplayName();   // Returns String for client API

    // Static methods in each enum for conversion
    static EnumType fromValue(Integer value) { ... }
    static EnumType fromDisplayName(String name) { ... }
}
```

#### 2. JPA AttributeConverter

Automatic conversion between Enum and Integer for database operations:

```java
@Converter(autoApply = true)  // Applied automatically to all fields of this enum type
public class UserStatusConverter extends AbstractEnumConverter<UserStatus> {
    public UserStatusConverter() {
        super(UserStatus.class);
    }
}
```

#### 3. Jackson Serialization

JSON serialization/deserialization using display names:

```java
public enum UserStatus implements PersistableEnum<Integer> {
    ACTIVE(1, "active"),
    INACTIVE(2, "inactive"),
    SUSPENDED(3, "suspended");

    @JsonValue  // Serialize to "active", "inactive", etc.
    public String getDisplayName() { ... }

    @JsonCreator  // Deserialize from "active" → ACTIVE
    public static UserStatus fromDisplayName(String displayName) { ... }
}
```

### Benefits

| Layer          | Type    | Benefits                                                                                                          |
| -------------- | ------- | ----------------------------------------------------------------------------------------------------------------- |
| **Database**   | Integer | • Compact storage (4 bytes vs varchar)<br>• Fast indexing<br>• Efficient comparisons<br>• Easy migration          |
| **Backend**    | Enum    | • Type safety at compile-time<br>• IDE autocomplete<br>• Refactoring support<br>• Switch statement exhaustiveness |
| **Client API** | String  | • Human-readable<br>• Version-independent<br>• No magic numbers<br>• Easy debugging                               |

### Example Flow

#### Create Store Request

```
1. Client sends:
   POST /api/v1/stores
   { "name": "Fresh Market", "type": "supermarket" }

2. Jackson deserializes:
   "supermarket" → StoreType.SUPERMARKET (enum)

3. JPA persists:
   StoreType.SUPERMARKET → 2 (integer in DB)

4. Database stores:
   Column 'type' = 2
```

#### Get Store Response

```
1. Database returns:
   Column 'type' = 2

2. JPA converts:
   2 → StoreType.SUPERMARKET (enum)

3. Jackson serializes:
   StoreType.SUPERMARKET → "supermarket"

4. Client receives:
   { "id": 1, "name": "Fresh Market", "type": "supermarket" }
```

### Enum Types in System

| Enum               | Values                                                                     | Usage                       |
| ------------------ | -------------------------------------------------------------------------- | --------------------------- |
| `UserRole`         | customer(1), store_owner(2), admin(3)                                      | User authorization          |
| `UserStatus`       | active(1), inactive(2), suspended(3), deleted(4)                           | Account status              |
| `StoreType`        | restaurant(1), supermarket(2), bakery(3), cafe(4)                          | Store classification        |
| `StoreStatus`      | active(1), inactive(2), pending(3), suspended(4)                           | Store availability          |
| `FoodItemStatus`   | pending(1), available(2), sold_out(3), expired(4)                          | Item lifecycle              |
| `OrderStatus`      | pending(1), confirmed(2), ready(3), completed(4), cancelled(5), expired(6) | Order tracking              |
| `PaymentMethod`    | cash(1), card(2), e_wallet(3)                                              | Payment options             |
| `PaymentStatus`    | pending(1), completed(2), failed(3), refunded(4)                           | Payment tracking            |
| `NotificationType` | flash_sale(1), order_update(2), promotion(3), system(4)                    | Notification categorization |

### Migration from Old Design

**Before (String-based enums):**

```sql
-- Storage: 10-20 bytes per enum
status VARCHAR(20) = 'ACTIVE'

-- Indexing: Slower string comparison
CREATE INDEX idx_status ON users(status);
```

**After (Integer-based enums):**

```sql
-- Storage: 4 bytes per enum
status INTEGER = 1

-- Indexing: Fast integer comparison
CREATE INDEX idx_status ON users(status);
```

**Performance Impact:**

- 60-75% storage reduction for enum columns
- 2-3x faster index lookups
- Better query optimizer statistics

---

## �🗃️ Database Schema (Main Tables)

### Users Table

```sql
users
├── id (PK)
├── email (UNIQUE)
├── password
├── full_name
├── phone_number
├── latitude
├── longitude
├── notification_enabled
├── notification_radius
├── role (INTEGER → UserRole enum)
├── status (INTEGER → UserStatus enum)
└── created_at
```

### Stores Table

```sql
stores
├── id (PK)
├── name
├── address
├── phone_number
├── latitude (indexed for geo)
├── longitude (indexed for geo)
├── type (INTEGER → StoreType enum)
├── flash_sale_time
├── status (INTEGER → StoreStatus enum)
├── rating
└── created_at
```

### Food Items Table

```sql
food_items
├── id (PK)
├── store_id (FK → stores)
├── category_id (FK → categories)
├── name
├── original_price
├── flash_price
├── total_quantity
├── available_quantity (with version for optimistic lock)
├── sale_start_time
├── sale_end_time
├── status (INTEGER → FoodItemStatus enum)
├── is_expired
└── version (for optimistic locking)
```

### Categories Table

```sql
categories
├── id (PK)
├── parent_id (FK → categories, nullable)
├── name
├── slug (UNIQUE, indexed)
├── description
├── image_url
├── display_order
├── is_active (indexed)
└── created_at
```

### Orders Table

```sql
orders
├── id (PK)
├── order_number (UNIQUE)
├── user_id (FK → users)
├── store_id (FK → stores)
├── total_amount
├── status (INTEGER → OrderStatus enum)
├── payment_method (INTEGER → PaymentMethod enum)
├── payment_status (INTEGER → PaymentStatus enum)
├── pickup_time
└── created_at
```

### Notifications Table

```sql
notifications
├── id (PK)
├── user_id (FK → users)
├── title
├── message
├── type (INTEGER → NotificationType enum)
├── reference_id
├── is_read
├── read_at
└── created_at
```

---

## 🔑 Redis Data Structures

### 1. Geo-spatial Index

```
Key: "geo:stores"
Type: GEOSPATIAL
Data: [(longitude, latitude, store_id), ...]

Commands:
- GEOADD geo:stores lon lat store_id
- GEORADIUS geo:stores lon lat 1 km
```

### 2. Distributed Locks

```
Key: "lock:food_item:123"
Type: STRING
TTL: 10 seconds
Value: uuid (lock owner)

Commands:
- SET lock:food_item:123 uuid NX EX 10
- DEL lock:food_item:123 (if owner)
```

### 3. Cache

```
Key: "food_item:123"
Type: JSON (via Jackson)
TTL: 5 minutes

Key: "store:456"
Type: JSON
TTL: 10 minutes
```

---

## 📨 RabbitMQ Queues & Exchanges

### Exchange & Queue Configuration

```
┌─────────────────────────────────────────────┐
│  flash-food.notification.exchange (Topic)   │
└────────────┬────────────────────────────────┘
             │ routing: notification.#
             ▼
┌─────────────────────────────────────────────┐
│  flash-food.notification.queue              │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  flash-food.flash-sale.exchange (Topic)     │
└────────────┬────────────────────────────────┘
             │ routing: flash-sale.#
             ▼
┌─────────────────────────────────────────────┐
│  flash-food.flash-sale.queue                │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  flash-food.order.exchange (Topic)          │
└────────────┬────────────────────────────────┘
             │ routing: order.#
             ▼
┌─────────────────────────────────────────────┐
│  flash-food.order.queue                     │
└─────────────────────────────────────────────┘
```

---

## 🛡️ Security Architecture

```
Client Request
     ↓
HTTPS (TLS 1.3)
     ↓
Spring Security Filter Chain
     ↓
┌────────────────────────┐
│  JWT Authentication    │
│  Filter                │
└────────────────────────┘
     ↓
┌────────────────────────┐
│  Authorization         │
│  (Role-based)          │
└────────────────────────┘
     ↓
Controller Method
(@PreAuthorize)
```

### Security Features

- ✅ JWT token-based authentication
- ✅ BCrypt password hashing
- ✅ CSRF protection disabled (stateless)
- ✅ CORS configuration
- ✅ Input validation (@Valid)
- ✅ SQL injection prevention (JPA)
- ✅ XSS prevention (DTOs)

---

## 📈 Scalability Strategy

### Horizontal Scaling

```
Load Balancer (NGINX)
         ↓
    ┌────┴────┐
    ▼         ▼
 App-1     App-2    ... App-N
    │         │
    └────┬────┘
         ↓
  Shared Resources:
  - PostgreSQL (Master-Slave)
  - Redis Cluster
  - RabbitMQ Cluster
```

### Caching Strategy

```
Request → Check Redis Cache
              ↓
         ┌────┴────┐
         │ Hit?    │
         └────┬────┘
    ┌─────Yes│No──────┐
    ▼                 ▼
Return Cache    Query Database
                      ↓
                 Update Cache
                      ↓
                Return Result
```

---

## 🔍 Monitoring & Logging

### Application Metrics

- Request/Response times
- Database query performance
- Redis hit/miss ratio
- RabbitMQ queue depth
- JVM memory usage

### Logging Levels

```
INFO  → Important business events
DEBUG → Detailed flow information
ERROR → Exception and errors
TRACE → Very detailed (SQL bindings)
```

---

## 🚀 Performance Optimizations

1. **Database Level**
   - Connection pooling
   - Prepared statements
   - Batch operations
   - Database indexing

2. **Application Level**
   - DTOs instead of entities
   - Lazy loading configurations
   - Async processing
   - Pagination

3. **Caching Level**
   - Redis for hot data
   - Geo-spatial indexing
   - Session management

4. **Messaging Level**
   - RabbitMQ for async tasks
   - Bulk notifications
   - Dead letter queues

---

**Architecture designed for:**

- ✅ High availability
- ✅ Horizontal scalability
- ✅ High concurrency
- ✅ Real-time notifications
- ✅ Fault tolerance
