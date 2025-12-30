Microservice with spring boot that has an orchestration layer to manage multiple services.
# Microservice with Spring Boot
This project is a microservice built using Spring Boot that includes an orchestration layer to manage multiple services. The orchestration layer is responsible for coordinating the interactions between different microservices, ensuring that they work together seamlessly.

## Features
- Built with Spring Boot for rapid development and easy deployment.
- Orchestration layer to manage and coordinate multiple services.
- RESTful APIs for communication between services.
- Easy to extend and integrate with additional services.
- Configurable via application properties.
- Built-in error handling and logging.
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
- H2 Database (Development)
- Maven
- Docker & Docker Compose

## Architecture
This project implements a complete microservices architecture with:
- **Eureka Server** (Port 8761): Service discovery and registration
- **API Gateway** (Port 8080): Single entry point with routing, load balancing, and circuit breakers
- **User Service** (Port 8081): User management with JWT authentication
- **Product Service** (Port 8082): Product catalog management
- **Order Service** (Port 8083): Order processing with inter-service communication

## Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Docker & Docker Compose (optional, for containerization)

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

## API Endpoints

### Authentication (User Service)
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Users
- `GET /api/users` - Get all users (ADMIN only)
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username

### Products
- `POST /api/products` - Create product
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/category/{category}` - Get products by category
- `GET /api/products/search?name={name}` - Search products
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `PATCH /api/products/{id}/stock?quantity={quantity}` - Update stock

### Orders
- `POST /api/orders` - Create order
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/user/{userId}` - Get orders by user ID
- `PATCH /api/orders/{id}/status?status={status}` - Update order status
- `DELETE /api/orders/{id}` - Cancel order

## Example Usage

### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

### 3. Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
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
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
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

## Features Implemented

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

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is licensed under the MIT License. 