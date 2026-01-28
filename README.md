Microservice with spring boot that has an orchestration layer to manage multiple services.
# Microservice with Spring Boot
This project is a microservice built using Spring Boot that includes an orchestration layer to manage multiple services. The orchestration layer is responsible for coordinating the interactions between different microservices, ensuring that they work together seamlessly.

## Features
- Built with Spring Boot for rapid development and easy deployment.
- Orchestration layer to manage and coordinate multiple services.
- RESTful APIs for communication between services.
- **OpenAPI/Swagger Documentation** - Interactive API documentation for all services.
- **API Versioning** - Versioned endpoints (`/api/v1/`) for backward compatibility.
- **Standardized API Responses** - Consistent response wrappers with success/error metadata.
- **Custom Domain Exceptions** - Type-safe exception handling with proper HTTP status codes.
- **MapStruct DTO Mapping** - Compile-time type-safe object mapping.
- **Constructor-based Dependency Injection** - Immutable dependencies for better testability.
- Easy to extend and integrate with additional services.
- Configurable via application properties with externalized sensitive configuration.
- Built-in error handling and logging with request tracing.
- Security features to protect service endpoints, with JWT authentication and role-based access control.
- Resilience features such as circuit breakers and retries using Resilience4j (modern replacement for deprecated Hystrix).

## Technologies Used
- Spring Boot 3.2.1
- Spring Cloud 2023.0.0
- Spring Cloud Gateway (API Gateway)
- Spring Cloud Netflix Eureka (Service Discovery)
- Spring Security with JWT
- Spring Data JPA
- Resilience4j (Circuit Breaker, Retry)
- OpenFeign (Inter-service communication)
- **SpringDoc OpenAPI 2.3.0** - API Documentation & Swagger UI
- **MapStruct 1.5.5.Final** - Object Mapping
- **Jakarta Bean Validation** - Request/Response validation
- PostgreSQL (Production)
- H2 Database (Development/Testing)
- Maven
- Docker & Docker Compose

## Architecture

This project implements a complete microservices architecture with:
- **Eureka Server** (Port 8761): Service discovery and registration
- **API Gateway** (Port 8080): Single entry point with routing, load balancing, and circuit breakers
- **User Service** (Port 8081): User management with JWT authentication
- **Product Service** (Port 8082): Product catalog management
- **Order Service** (Port 8083): Order processing with inter-service communication

### Architecture Diagram
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────────┐
│          API Gateway (Port 8080)            │
│  - Request Routing                          │
│  - Load Balancing                           │
│  - Circuit Breakers                         │
│  - CORS Configuration                       │
└────┬────────────┬────────────┬──────────────┘
     │            │            │
     ▼            ▼            ▼
┌─────────┐  ┌─────────┐  ┌─────────┐
│  User   │  │ Product │  │  Order  │
│ Service │  │ Service │  │ Service │
│  :8081  │  │  :8082  │  │  :8083  │
└────┬────┘  └────┬────┘  └────┬────┘
     │            │            │
     └────────────┴────────────┘
                  │
                  ▼
         ┌────────────────┐
         │ Eureka Server  │
         │  (Discovery)   │
         │     :8761      │
         └────────────────┘
```

## Architectural Decisions

### Why Service Discovery (Eureka)?

**Problem**: In a microservices architecture, services need to communicate with each other. Hardcoding service URLs creates several issues:
- Services may scale up/down dynamically (multiple instances)
- Service locations may change (different ports/hosts)
- Manual configuration becomes error-prone and unmanageable

**Solution**: Spring Cloud Netflix Eureka provides service discovery:
- **Dynamic Service Registration**: Services automatically register themselves on startup
- **Health Monitoring**: Eureka tracks which service instances are healthy
- **Load Distribution**: Client-side load balancing across multiple instances
- **Fault Tolerance**: Automatically removes unhealthy instances from the registry
- **No Hardcoded URLs**: Services discover each other by logical name (e.g., `user-service`)

**Benefits**:
- **Zero Configuration**: Services find each other automatically
- **Auto-Scaling Support**: New instances are discovered immediately
- **Development Simplicity**: Same code works in dev, staging, and production
- **Resilience**: System adapts to service failures and recoveries

### Why API Gateway?

**Problem**: Without a gateway, clients must:
- Know the address of every microservice
- Handle cross-cutting concerns (auth, logging) in each service
- Deal with network complexity and protocol differences
- Manage versioning across multiple services

**Solution**: Spring Cloud Gateway provides a single entry point:
- **Single Entry Point**: Clients connect to one URL (`:8080`)
- **Request Routing**: Routes `/api/v1/users/**` → User Service, `/api/v1/products/**` → Product Service
- **Load Balancing**: Distributes requests across service instances
- **Circuit Breakers**: Prevents cascading failures with fallback responses
- **Security**: Centralized authentication and authorization
- **CORS Management**: Unified cross-origin configuration
- **Rate Limiting**: Protects services from overload
- **Protocol Translation**: Can handle REST → gRPC, HTTP → WebSocket

**Benefits**:
- **Simplified Client Code**: Frontend only needs one endpoint
- **Security**: Single point for authentication/authorization
- **Monitoring**: Centralized logging and metrics collection
- **Flexibility**: Change backend services without affecting clients
- **Performance**: Request/response caching and compression

### Why Circuit Breakers (Resilience4j)?

**Problem**: Service failures can cascade:
- If Order Service calls Product Service and it's down, requests pile up
- Threads block waiting for timeouts
- Eventually, Order Service becomes unresponsive
- The entire system can collapse from a single service failure

**Solution**: Resilience4j Circuit Breakers prevent cascading failures:
- **Fail Fast**: Immediately return fallback when service is down
- **Auto-Recovery**: Periodically tests if failed service is healthy again
- **Thread Protection**: Prevents resource exhaustion from waiting on dead services
- **Graceful Degradation**: System continues functioning with reduced features

**States**:
1. **CLOSED**: Normal operation, requests pass through
2. **OPEN**: Service is failing, requests fail immediately with fallback
3. **HALF_OPEN**: Testing if service recovered, allows limited requests

**Benefits**:
- **System Stability**: One failing service doesn't crash the entire system
- **Better UX**: Users get quick fallback responses instead of timeouts
- **Resource Protection**: Prevents thread exhaustion and memory leaks
- **Automatic Recovery**: System self-heals when services come back online

### Why OpenFeign for Inter-Service Communication?

**Problem**: Making HTTP calls between services requires boilerplate:
```java
// Without Feign - lots of boilerplate
RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.set("Content-Type", "application/json");
HttpEntity<String> entity = new HttpEntity<>(headers);
ResponseEntity<User> response = restTemplate.exchange(
    "http://user-service/api/v1/users/" + userId,
    HttpMethod.GET,
    entity,
    User.class
);
```

**Solution**: OpenFeign provides declarative REST clients:
```java
// With Feign - clean and simple
@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/v1/users/{id}")
    UserDTO getUserById(@PathVariable Long id);
}
```

**Benefits**:
- **Declarative**: Define interface, Feign generates implementation
- **Integration**: Works seamlessly with Eureka for service discovery
- **Load Balancing**: Built-in client-side load balancing
- **Retry Logic**: Automatic retry on failures
- **Less Code**: Reduces boilerplate by 80%+
- **Type Safety**: Compile-time checking of API contracts

### Why JWT Authentication?

**Problem**: Traditional session-based auth doesn't scale in microservices:
- Sessions require shared state (Redis, database)
- Each service needs access to session storage
- Horizontal scaling becomes complex

**Solution**: JWT (JSON Web Tokens) provide stateless authentication:
- **Self-Contained**: Token contains all user information (username, roles)
- **Stateless**: No need for session storage or database lookups
- **Distributed**: Any service can validate tokens independently
- **Secure**: Digitally signed to prevent tampering
- **Scalable**: Services can scale horizontally without session replication

**Flow**:
1. User logs in → User Service validates credentials
2. User Service generates JWT with user info + expiration
3. Client includes JWT in `Authorization: Bearer <token>` header
4. Each service validates JWT signature independently
5. No database lookup needed for authentication

**Benefits**:
- **Scalability**: No shared session state required
- **Performance**: No database calls for auth checks
- **Decentralized**: Services are independent
- **Security**: Tokens expire and can be revoked

### Why MapStruct for DTO Mapping?

**Problem**: Manual DTO mapping is tedious and error-prone:
```java
// Manual mapping - lots of boilerplate, easy to miss fields
UserResponse response = new UserResponse();
response.setId(user.getId());
response.setUsername(user.getUsername());
response.setEmail(user.getEmail());
// ... 15 more fields
```

**Solution**: MapStruct generates mapping code at compile-time:
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
}
```

**Benefits**:
- **Type-Safe**: Compile-time checking catches missing fields
- **Performance**: No reflection, plain Java method calls
- **Maintainable**: Change entity → mapper updates automatically
- **Less Code**: 10 lines instead of 100+
- **Spring Integration**: Works as Spring beans with dependency injection

### Why Standardized API Responses?

**Problem**: Inconsistent response formats across services:
- User Service returns `{ "id": 1, "name": "John" }`
- Product Service returns `{ "data": {...}, "status": "success" }`
- Error formats differ between services

**Solution**: `ApiResponse<T>` wrapper provides consistency:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* actual payload */ },
  "timestamp": "2026-01-09T10:30:00"
}
```

**Benefits**:
- **Consistency**: All responses follow same structure
- **Client Simplicity**: Frontend can parse all responses the same way
- **Metadata**: Timestamp, request IDs for debugging
- **Error Handling**: Unified error format with field-level validation
- **Versioning**: Easy to add new metadata fields without breaking clients

### Why API Versioning?

**Problem**: APIs evolve, but clients expect stability:
- Breaking changes force all clients to update simultaneously
- Can't A/B test new API versions
- Difficult to maintain backward compatibility

**Solution**: URL-based versioning (`/api/v1/`, `/api/v2/`):
- **Backward Compatibility**: Old clients continue using `/api/v1/`
- **Gradual Migration**: New clients adopt `/api/v2/` at their pace
- **Clear Documentation**: Version is visible in the URL
- **Easy Routing**: Gateway can route to different service versions

**Benefits**:
- **No Breaking Changes**: Clients update when ready
- **Parallel Development**: Can develop v2 while v1 is stable
- **Testing**: A/B test new versions with subset of users
- **Deprecation**: Clearly communicate when versions will be retired

## Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Docker & Docker Compose (optional, for containerization)
- PostgreSQL (optional, for production database)

**Note**: MapStruct annotation processors are configured in the POMs and will run automatically during compilation.

## Getting Started

### Option 1: Run Locally with Maven

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd SpringBoot
   ```

2. Build all services:
   ```bash
   mvn clean package -DskipTests
   ```

3. Start services in order:
   ```bash
   # Terminal 1 - Start Eureka Server
   cd eureka-server
   mvn spring-boot:run

   # Terminal 2 - Start API Gateway (wait for Eureka to start)
   cd api-gateway
   mvn spring-boot:run

   # Terminal 3 - Start User Service
   cd user-service
   mvn spring-boot:run

   # Terminal 4 - Start Product Service
   cd product-service
   mvn spring-boot:run

   # Terminal 5 - Start Order Service
   cd order-service
   mvn spring-boot:run
   ```

### Option 2: Run with Docker Compose

1. Build all services:
   ```bash
   mvn clean package -DskipTests
   ```

2. Start all services with Docker Compose:
   ```bash
   docker-compose up --build
   ```

3. Stop all services:
   ```bash
   docker-compose down
   ```

## Accessing the Services

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081 (or via Gateway)
- **Product Service**: http://localhost:8082 (or via Gateway)
- **Order Service**: http://localhost:8083 (or via Gateway)

### API Documentation (Swagger UI)
- **User Service API Docs**: http://localhost:8081/swagger-ui.html
- **Product Service API Docs**: http://localhost:8082/swagger-ui.html
- **Order Service API Docs**: http://localhost:8083/swagger-ui.html
- **OpenAPI JSON**: Available at `/v3/api-docs` on each service

## API Endpoints

> **Note**: All endpoints use versioned paths (`/api/v1/`) for backward compatibility.

### Authentication (User Service)
- `POST /api/v1/auth/signup` - Register new user
- `POST /api/v1/auth/login` - Login and get JWT token

### Users (Requires Authentication)
- `GET /api/v1/users` - Get all users (ADMIN only)
- `GET /api/v1/users/{id}` - Get user by ID
- `GET /api/v1/users/username/{username}` - Get user by username

### Products
- `POST /api/v1/products` - Create product
- `GET /api/v1/products` - Get all products
- `GET /api/v1/products/{id}` - Get product by ID
- `GET /api/v1/products/category/{category}` - Get products by category
- `GET /api/v1/products/search?name={name}` - Search products
- `PUT /api/v1/products/{id}` - Update product
- `DELETE /api/v1/products/{id}` - Delete product
- `PATCH /api/v1/products/{id}/stock?quantity={quantity}` - Update stock

### Orders
- `POST /api/v1/orders` - Create order
- `GET /api/v1/orders` - Get all orders
- `GET /api/v1/orders/{id}` - Get order by ID
- `GET /api/v1/orders/user/{userId}` - Get orders by user ID
- `PATCH /api/v1/orders/{id}/status?status={status}` - Update order status
- `DELETE /api/v1/orders/{id}` - Cancel order

## Example Usage

### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response** (with standardized wrapper):
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "message": "User registered successfully!"
  },
  "timestamp": "2026-01-09T10:30:00"
}
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

### 3. Create a Product
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "quantity": 50,
    "category": "Electronics"
  }'
```

### 4. Create an Order
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

## API Response Standards

All API endpoints return standardized response structures for consistency:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* actual response data */ },
  "timestamp": "2026-01-09T10:30:00",
  "metadata": { /* optional metadata */ }
}
```

### Error Response
```json
{
  "timestamp": "2026-01-09T10:30:00",
  "status": 404,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Order not found with identifier: 123",
  "path": "/api/v1/orders/123",
  "requestId": "uuid-here",
  "fieldErrors": [ /* for validation errors */ ]
}
```

### Exception Handling

Custom domain exceptions provide clear error messages:
- `ResourceNotFoundException` - HTTP 404
- `InsufficientStockException` - HTTP 409
- `OrderCancellationException` - HTTP 400
- `AuthenticationException` - HTTP 401
- `UserAlreadyExistsException` - HTTP 409
- Validation errors return field-level details

## Features Implemented

### API Best Practices
- **OpenAPI/Swagger** - Interactive documentation at `/swagger-ui.html`
- **API Versioning** - All endpoints versioned as `/api/v1/*`
- **Standardized Responses** - Consistent `ApiResponse<T>` wrapper
- **Custom Exceptions** - Domain-specific exception hierarchy
- **DTO Mapping** - MapStruct for type-safe object mapping
- **Constructor Injection** - Immutable dependencies
- **Request Validation** - Jakarta Bean Validation annotations
- **Security** - Passwords never exposed in responses (`@JsonIgnore`)

### Security
- JWT-based authentication
- Role-based access control (USER, ADMIN)
- Password encryption with BCrypt
- Secured endpoints

### Resilience
- Circuit breakers with Resilience4j
- Automatic retry mechanisms
- Fallback responses
- Health monitoring

### Service Discovery
- Eureka server for service registration
- Load balancing across service instances
- Dynamic service discovery

### Orchestration
- API Gateway for centralized routing
- Request/response filtering
- Circuit breakers at gateway level
- CORS configuration

### Data Management
- JPA/Hibernate for persistence
- H2 in-memory database (easily replaceable with PostgreSQL/MySQL)
- Transaction management
- Repository pattern

## Monitoring

All services expose actuator endpoints for monitoring:
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`
- Circuit Breakers: `/actuator/circuitbreakers`

## Project Structure
```
SpringBoot/
├── eureka-server/          # Service Discovery
├── api-gateway/            # Orchestration Layer
├── user-service/           # User Management & Authentication
├── product-service/        # Product Catalog
├── order-service/          # Order Processing
├── docker-compose.yml      # Docker orchestration
└── pom.xml                 # Parent POM
```

## Environment Configuration

Sensitive configuration values are externalized using environment variables:

### User Service
```bash
export DB_URL=jdbc:postgresql://localhost:5432/userdb
export DB_USERNAME=userservice
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret_key_here
export JWT_EXPIRATION=86400000
```

### Docker Environment
Set environment variables in `.env` file for Docker Compose:
```properties
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_key
```

## Testing

The project includes:
- **BDD Tests** - Cucumber/Gherkin scenarios in order-service
- **Unit Tests** - JUnit 5 with Mockito
- **Integration Tests** - REST Assured for API testing

Run tests:
```bash
mvn test
```

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is licensed under the MIT License. 