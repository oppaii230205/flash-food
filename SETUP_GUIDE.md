# Flash-Food - Setup & Development Guide

## 📋 Mục Lục

1. [Prerequisites](#prerequisites)
2. [Cài Đặt Dependencies](#cài-đặt-dependencies)
3. [Cấu Hình Database](#cấu-hình-database)
4. [Chạy Ứng Dụng](#chạy-ứng-dụng)
5. [Testing API](#testing-api)
6. [Development Guidelines](#development-guidelines)

---

## 🔧 Prerequisites

Đảm bảo bạn đã cài đặt các công cụ sau:

- **Java 21** - [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 15+** - [Download](https://www.postgresql.org/download/)
- **Redis 7+** - [Download](https://redis.io/download/)
- **RabbitMQ 3.12+** - [Download](https://www.rabbitmq.com/download.html)
- **Git** - [Download](https://git-scm.com/downloads)
- **IDE** - IntelliJ IDEA hoặc VS Code

---

## 📦 Cài Đặt Dependencies

### 1. Clone Repository

```bash
git clone <your-repo-url>
cd flash-food
```

### 2. Build Project

```bash
mvn clean install
```

### 3. Verify Dependencies

```bash
mvn dependency:tree
```

---

## 🗄️ Cấu Hình Database

### PostgreSQL Setup

1. **Tạo Database**

```sql
CREATE DATABASE flashfood_db;
```

2. **Tạo User (Optional)**

```sql
CREATE USER flashfood_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE flashfood_db TO flashfood_user;
```

3. **Cập nhật `application.properties`**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flashfood_db
spring.datasource.username=flashfood_user
spring.datasource.password=your_password
```

> **Note:** JPA sẽ tự động tạo tables khi chạy app lần đầu (ddl-auto=update)

---

## 🚀 Chạy Ứng Dụng

### Method 1: Maven

```bash
mvn spring-boot:run
```

### Method 2: IDE

- IntelliJ: Right-click `FlashFoodApplication.java` → Run
- VS Code: F5 hoặc Debug icon

### Method 3: JAR File

```bash
mvn package
java -jar target/flash-food-0.0.1-SNAPSHOT.jar
```

### Verify Application Started

Kiểm tra log:

```
Started FlashFoodApplication in X.XXX seconds
```

Truy cập:

- API Health Check: http://localhost:8080/api/v1/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

---

## 🧪 Testing API

### 1. Health Check

```bash
curl http://localhost:8080/api/v1/health
```

Expected Response:

```json
{
  "status": "UP",
  "timestamp": "2026-02-13T...",
  "service": "Flash-Food API",
  "version": "1.0.0"
}
```

### 2. Swagger UI

1. Mở browser: http://localhost:8080/swagger-ui.html
2. Explore các API endpoints
3. Try out các API trực tiếp

---

## 🛠️ Development Guidelines

### Package Structure Best Practices

#### 1. **Entities** (Domain Models)

```java
@Entity
@Table(name = "users")
@Data
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ... fields
}
```

#### 2. **Repositories** (Data Access)

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

#### 3. **Services** (Business Logic)

```java
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserResponse createUser(RegisterRequest request) {
        // Business logic
    }
}
```

#### 4. **Controllers** (REST API)

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
```

---

## 🔑 Các Tính Năng Cần Implement

### 1. **User Authentication (Priority: HIGH)**

- [ ] JWT Token generation
- [ ] Login endpoint
- [ ] Register endpoint
- [ ] Password encryption
- [ ] Token validation filter

**Files to create:**

- `JwtTokenProvider.java`
- `JwtAuthenticationFilter.java`
- `UserDetailsServiceImpl.java`
- `AuthService.java`
- `AuthController.java`

### 2. **Store Management (Priority: HIGH)**

- [ ] CRUD operations for stores
- [ ] Store search by location
- [ ] Store verification
- [ ] Store ratings

**Files to create:**

- `StoreService.java`
- `StoreController.java`

### 3. **Food Item Management (Priority: HIGH)**

- [ ] Create flash sale items
- [ ] Update quantity
- [ ] Search available items
- [ ] Handle high concurrency

**Files to create:**

- `FoodItemService.java`
- `FoodItemController.java`

### 4. **Order Processing (Priority: CRITICAL)**

- [ ] Create order với distributed lock
- [ ] Update order status
- [ ] Handle payment
- [ ] Auto-expire orders

**Files to create:**

- `OrderService.java`
- `OrderController.java`

### 5. **Notification System (Priority: MEDIUM)**

- [ ] Send flash sale notifications
- [ ] Mark as read
- [ ] Get user notifications
- [ ] Clean old notifications

**Files to create:**

- `NotificationService.java`
- `NotificationController.java`

---

## 🐛 Debugging Tips

### 1. Database Issues

```bash
# Check PostgreSQL status
sudo service postgresql status

# Check logs
tail -f /var/log/postgresql/postgresql-15-main.log
```

### 2. Redis Issues

```bash
# Check Redis status
redis-cli ping

# Monitor Redis
redis-cli monitor
```

### 3. RabbitMQ Issues

```bash
# Check RabbitMQ status
rabbitmqctl status

# Access Management UI
http://localhost:15672
# Default: guest/guest
```

### 4. Application Logs

```bash
# Real-time logs
tail -f logs/spring-boot-logger.log

# Search errors
grep -i error logs/spring-boot-logger.log
```

---

## 📊 Performance Optimization

### 1. **Database Indexing**

Create indexes for frequently queried fields:

```sql
CREATE INDEX idx_food_items_status ON food_items(status);
CREATE INDEX idx_stores_location ON stores USING GIST(
    ll_to_earth(latitude, longitude)
);
```

### 2. **Redis Caching Strategy**

```java
@Cacheable(value = "stores", key = "#id")
public StoreResponse getStore(Long id) {
    // Will cache result
}
```

### 3. **Connection Pooling**

Already configured in `application.properties`:

```properties
spring.data.redis.lettuce.pool.max-active=8
```

---

## 🔐 Security Checklist

- [ ] HTTPS enabled (production)
- [ ] JWT secret key in environment variable
- [ ] Database password encrypted
- [ ] Input validation on all endpoints
- [ ] Rate limiting implemented
- [ ] CORS configured properly
- [ ] SQL injection prevention (using JPA)
- [ ] XSS prevention (using DTOs)

---

## 📝 Environment Variables (Production)

Create `.env` file:

```bash
# Database
DB_URL=jdbc:postgresql://your-host:5432/flashfood_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Redis
REDIS_HOST=your-redis-host
REDIS_PASSWORD=your-redis-password

# RabbitMQ
RABBITMQ_HOST=your-rabbitmq-host
RABBITMQ_USERNAME=your_username
RABBITMQ_PASSWORD=your_password

# JWT
JWT_SECRET=your-very-long-secret-key-min-256-bits
JWT_EXPIRATION=86400000

# Application
SERVER_PORT=8080
```

---

## 🚀 Deployment

### Docker Deployment (Recommended)

1. **Create Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/flash-food-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

2. **Build Image**

```bash
docker build -t flash-food:latest .
```

3. **Run Container**

```bash
docker run -p 8080:8080 --env-file .env flash-food:latest
```

---

## 📚 Learning Resources

### Backend Development

- [Spring Boot Official Docs](https://spring.io/projects/spring-boot)
- [Baeldung Spring Tutorials](https://www.baeldung.com/spring-tutorial)
- [Redis University](https://university.redis.com/)

### High Concurrency

- [Java Concurrency in Practice](https://jcip.net/)
- [Distributed Systems Patterns](https://martinfowler.com/articles/patterns-of-distributed-systems/)

### System Design

- [System Design Primer](https://github.com/donnemartin/system-design-primer)
- [Designing Data-Intensive Applications](https://dataintensive.net/)

---

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -m 'Add some feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Open Pull Request

---

## 📞 Support

- Email: support@flashfood.com
- Issues: GitHub Issues
- Documentation: `/docs` folder

---

**Happy Coding! 🚀**
