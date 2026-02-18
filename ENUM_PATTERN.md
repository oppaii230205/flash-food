# Enum Conversion Pattern

## 📋 Overview

Flash-Food sử dụng một pattern đơn giản và rõ ràng để convert enum giữa 3 layers:

- **Database**: Lưu trữ dưới dạng `Integer` (compact, fast indexing)
- **Backend**: Sử dụng Java `Enum` (type-safe, compile-time checking)
- **Client API**: Gửi/nhận dưới dạng `String` (human-readable, version-independent)

## 🔄 Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│  CLIENT: {"status": "active", "role": "customer"}       │
│  Type: String (JSON)                                    │
└────────────────────┬────────────────────────────────────┘
                     │ @JsonCreator / @JsonValue
                     │ fromDisplayName() / getDisplayName()
                     ▼
┌─────────────────────────────────────────────────────────┐
│  BACKEND: UserStatus.ACTIVE, UserRole.CUSTOMER          │
│  Type: Java Enum                                        │
└────────────────────┬────────────────────────────────────┘
                     │ JPA AttributeConverter
                     │ fromCode() / getCode()
                     ▼
┌─────────────────────────────────────────────────────────┐
│  DATABASE: status = 1, role = 1                         │
│  Type: INTEGER                                          │
└─────────────────────────────────────────────────────────┘
```

## 🏗️ Pattern Structure

### 1. Enum Class

Mỗi enum class bao gồm:

```java
public enum OrderStatus {
    PENDING(1, "pending"),
    CONFIRMED(2, "confirmed"),
    COMPLETED(3, "completed");

    private final int code;              // For database storage
    private final String displayName;    // For client API

    // Static LOOKUP maps for fast O(1) conversion
    private static final Map<Integer, OrderStatus> CODE_LOOKUP =
            Arrays.stream(values())
                  .collect(Collectors.toMap(OrderStatus::getCode, e -> e));

    private static final Map<String, OrderStatus> NAME_LOOKUP =
            Arrays.stream(values())
                  .collect(Collectors.toMap(e -> e.displayName.toLowerCase(), e -> e));

    // Constructor
    OrderStatus(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    // Getters
    public int getCode() {
        return code;
    }

    @JsonValue  // Jackson serializes enum to string for client
    public String getDisplayName() {
        return displayName;
    }

    // Conversion methods
    public static OrderStatus fromCode(int code) {
        OrderStatus status = CODE_LOOKUP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Invalid OrderStatus code: " + code);
        }
        return status;
    }

    @JsonCreator  // Jackson deserializes string from client to enum
    public static OrderStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("OrderStatus displayName cannot be null or empty");
        }
        OrderStatus status = NAME_LOOKUP.get(displayName.toLowerCase());
        if (status == null) {
            throw new IllegalArgumentException("Invalid OrderStatus name: " + displayName);
        }
        return status;
    }
}
```

### 2. JPA Converter

Mỗi enum có một converter riêng để tự động convert giữa enum và integer:

```java
@Converter(autoApply = true)  // Tự động apply cho tất cả fields của enum này
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(OrderStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public OrderStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : OrderStatus.fromCode(dbData);
    }
}
```

### 3. Entity Usage

Trong entity, không cần thêm annotation nào, converter tự động apply:

```java
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private Long id;

    // Converter tự động convert OrderStatus <-> Integer
    private OrderStatus status = OrderStatus.PENDING;

    // Không cần @Enumerated annotation nữa!
}
```

### 4. DTO Mapping

DTOs trả về cho client sử dụng String:

```java
@Data
public class OrderResponse {
    private Long id;
    private String status;  // String, not Enum
}

// In EntityMapper
public OrderResponse toOrderResponse(Order order) {
    return OrderResponse.builder()
            .id(order.getId())
            .status(order.getStatus().getDisplayName())  // Enum -> String
            .build();
}
```

## 📊 Complete Example

### API Request Flow

```java
// 1. Client sends JSON
POST /api/orders
{
  "storeId": 1,
  "paymentMethod": "cash"  // String
}

// 2. Jackson deserializes using @JsonCreator
CreateOrderRequest request = objectMapper.readValue(json);
request.getPaymentMethod() // "cash" String

// 3. Service converts string to enum
PaymentMethod method = PaymentMethod.fromDisplayName(request.getPaymentMethod());
// method = PaymentMethod.CASH (enum)

// 4. Save to database - converter automatically converts enum to integer
order.setPaymentMethod(PaymentMethod.CASH);
orderRepository.save(order);
// Database stores: payment_method = 1

// 5. Load from database - converter automatically converts integer to enum
Order savedOrder = orderRepository.findById(orderId);
savedOrder.getPaymentMethod() // PaymentMethod.CASH (enum)

// 6. Map to response DTO
OrderResponse response = entityMapper.toOrderResponse(savedOrder);
response.getPaymentMethod() // "cash" (string)

// 7. Jackson serializes using @JsonValue
String responseJson = objectMapper.writeValueAsString(response);
// {"id": 1, "paymentMethod": "cash"}
```

## 🎯 Benefits of This Pattern

| Aspect          | Benefit                                                 |
| --------------- | ------------------------------------------------------- |
| **Simplicity**  | No generic classes, no complex inheritance              |
| **Performance** | O(1) lookup with static maps, minimal overhead          |
| **Type Safety** | Compile-time checking with Java enums                   |
| **Storage**     | Compact integer storage (4 bytes vs 10-20 bytes string) |
| **Indexing**    | Fast integer indexing in database                       |
| **API**         | Human-readable strings for clients                      |
| **Versioning**  | Client doesn't break with new enum values               |
| **Debugging**   | Easy to trace: string → enum → integer                  |

## 📝 All Enums in System

| Enum               | DB Codes   | Display Names                                                       | Usage                       |
| ------------------ | ---------- | ------------------------------------------------------------------- | --------------------------- |
| `UserRole`         | 1, 2, 3    | customer, store_owner, admin                                        | User authorization          |
| `UserStatus`       | 1, 2, 3, 4 | active, inactive, suspended, deleted                                | Account status              |
| `StoreType`        | 1-7        | bakery, restaurant, cafe, etc.                                      | Store categorization        |
| `StoreStatus`      | 1, 2, 3, 4 | active, inactive, suspended, pending_approval                       | Store lifecycle             |
| `FoodItemStatus`   | 1-5        | pending, available, sold_out, expired, cancelled                    | Item lifecycle              |
| `OrderStatus`      | 1-7        | pending, confirmed, preparing, ready, completed, cancelled, expired | Order tracking              |
| `PaymentMethod`    | 1-5        | cash, momo, zalopay, banking, credit_card                           | Payment options             |
| `PaymentStatus`    | 1-4        | pending, paid, failed, refunded                                     | Payment tracking            |
| `NotificationType` | 1-6        | new_flash_sale, order_confirmed, etc.                               | Notification categorization |

## 🔧 Adding New Enum Value

```java
// 1. Add to enum class
public enum OrderStatus {
    PENDING(1, "pending"),
    CONFIRMED(2, "confirmed"),
    // Add new value
    PICKED_UP(8, "picked_up"),  // New value with next code
}

// 2. No changes needed in:
// - Converter (works automatically)
// - Entity (works automatically)
// - DTO mapping (works automatically)

// 3. Database migration
ALTER TABLE orders MODIFY COLUMN status INTEGER;
-- No schema change needed, just update data if necessary
```

## ⚠️ Important Notes

1. **Code Values**: Integer codes must be unique and never reused
2. **Display Names**: String values must be lowercase and URL-safe
3. **Null Safety**: All conversion methods handle null properly
4. **Case Insensitive**: fromDisplayName() accepts any case ("Active", "ACTIVE", "active")
5. **No Generic Base Class**: Each enum is independent and simple
6. **Static Maps**: Initialized once at class loading, thread-safe

## 🚀 Performance

- **Lookup Time**: O(1) with HashMap
- **Memory**: Static maps shared across all instances
- **Storage**: 60-75% reduction vs VARCHAR storage
- **Index Speed**: 2-3x faster than string comparison

---

**Pattern Inspiration**: Based on simple, explicit pattern without over-engineering through generics.
