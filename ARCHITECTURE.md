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
Store Owner → POST /api/food-items
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
User → POST /api/orders
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

## 🗃️ Database Schema (Main Tables)

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
├── role (ENUM)
├── status (ENUM)
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
├── type (ENUM)
├── flash_sale_time
├── status (ENUM)
├── rating
└── created_at
```

### Food Items Table

```sql
food_items
├── id (PK)
├── store_id (FK → stores)
├── name
├── original_price
├── flash_price
├── total_quantity
├── available_quantity (with version for optimistic lock)
├── sale_start_time
├── sale_end_time
├── status (ENUM)
├── is_expired
└── version (for optimistic locking)
```

### Orders Table

```sql
orders
├── id (PK)
├── order_number (UNIQUE)
├── user_id (FK → users)
├── store_id (FK → stores)
├── total_amount
├── status (ENUM)
├── payment_method
├── payment_status
├── pickup_time
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
