# Common Library (common-lib)

A reusable Java library for standardized error handling, structured logging, DTOs, and common utilities across microservices.

## 📦 What's Included

### 🔥 Exception Handling
- **BusinessException** - Base class for all business/domain exceptions
- **ResourceNotFoundException** - For 404 NOT FOUND scenarios
- **InsufficientStockException** - For inventory/stock conflicts (409 CONFLICT)
- **ServiceUnavailableException** - For external service failures (503 SERVICE UNAVAILABLE)

### 📊 DTOs
- **ErrorResponse** - Standardized error response with field-level validation details
- **ApiResponse<T>** - Standardized success response wrapper with factory methods

### 🎯 Exception Handler
- **BaseExceptionHandler** - Pre-configured `@RestControllerAdvice` that handles:
  - Validation errors (`@Valid`)
  - Custom business exceptions
  - Feign client exceptions
  - Generic uncaught exceptions

### 📝 Structured Logging (NEW!)
- **Correlation ID Tracking** - Auto-generate and propagate correlation IDs across services
- **MDC Integration** - Automatic context population (correlationId, traceId, spanId, userId)
- **Request/Response Logging** - HTTP method, path, status, timing with sensitive data filtering
- **Sensitive Data Filtering** - Auto-redact passwords, tokens, and other sensitive fields
- **Zero Configuration** - Auto-enabled via Spring Boot, just add dependency!

## 🚀 Usage

### 1. Add Dependency

Add to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.microservices</groupId>
    <artifactId>common-lib</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. Use Common Exceptions

```java
import com.microservices.common.exception.ResourceNotFoundException;

public class ProductService {
    public Product getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));
    }
}
```

### 3. Use ApiResponse Wrapper

```java
import com.microservices.common.dto.ApiResponse;

@RestController
public class ProductController {
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable Long id) {
        ProductDTO product = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved"));
    }
}
```

### 4. Extend Base Exception Handler (Optional)

For service-specific exceptions:

```java
import com.microservices.common.handler.BaseExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProductExceptionHandler extends BaseExceptionHandler {
    
    // BaseExceptionHandler handles all common exceptions
    // Add service-specific handlers here
    
    @ExceptionHandler(ProductSpecificException.class)
    public ResponseEntity<ErrorResponse> handleProductException(...) {
        // Custom handling
    }
}
```

### 5. Or Use Base Handler Directly

No need to create a handler class - just having `common-lib` as a dependency will activate `BaseExceptionHandler`:

```java
// No handler class needed! BaseExceptionHandler is auto-configured
```

## 📋 Response Examples

### Success Response
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "Gaming Laptop",
    "price": 1499.99
  },
  "timestamp": "2026-01-30T23:00:00"
}
```

### Error Response (Resource Not Found)
```json
{
  "timestamp": "2026-01-30T23:00:00",
  "status": 404,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Product not found with identifier: 123",
  "path": "/api/v1/products/123",
  "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### Error Response (Validation)
```json
{
  "timestamp": "2026-01-30T23:00:00",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/products",
  "requestId": "uuid-here",
  "fieldErrors": [
    {
      "field": "price",
      "rejectedValue": -10,
      "message": "must be greater than 0"
    }
  ]
}
```

## 🎯 Benefits

### For New Services
✅ **Zero Configuration** - Just add dependency, exceptions handled automatically  
✅ **Consistent Responses** - All services return same error format  
✅ **Less Boilerplate** - No need to write exception handlers  

### For Existing Services
✅ **Drop-in Replacement** - Replace local exceptions with common ones  
✅ **Gradual Migration** - Can extend BaseExceptionHandler while migrating  
✅ **No Breaking Changes** - Response format remains the same  

### For Frontend Developers
✅ **Predictable Errors** - Same structure across all services  
✅ **Error Codes** - Machine-readable codes for client-side logic  
✅ **Field Validation** - Detailed field-level error information  

## 🔧 Customization

### Create Custom Exceptions

```java
package com.yourcompany.yourservice.exception;

import com.microservices.common.exception.BusinessException;

public class PaymentFailedException extends BusinessException {
    public PaymentFailedException(String message) {
        super(message, "PAYMENT_FAILED");
    }
}
```

### Add Custom Exception Handler

```java
@RestControllerAdvice
public class PaymentExceptionHandler extends BaseExceptionHandler {
    
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailed(
            PaymentFailedException ex, 
            HttpServletRequest request) {
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.PAYMENT_REQUIRED,
            ex.getErrorCode(),
            ex.getMessage(),
            null,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.PAYMENT_REQUIRED);
    }
}
```

## 📝 Structured Logging

### Overview

Common-lib provides automatic structured logging with correlation ID tracking, request/response logging, and sensitive data filtering.

### Features

✅ **Auto-Configuration** - Zero configuration required, works out of the box  
✅ **Correlation ID Tracking** - Auto-generated UUIDs propagated across services  
✅ **MDC Integration** - Every log includes: correlationId, traceId, spanId, userId, serviceName  
✅ **Request/Response Logging** - HTTP method, path, status, timing  
✅ **Sensitive Data Filtering** - Auto-redacts passwords, tokens, credit cards, etc.  
✅ **Performance Optimized** - Async logging, configurable body logging  

### Log Format

```log
2026-01-30 23:45:12.345 INFO  [product-service,a1b2c3d4-uuid,trace123,span456] ProductService - Getting product 123
                               ↑service        ↑correlationId ↑traceId ↑spanId
```

### Configuration (Optional)

All settings are optional with sensible defaults:

```yaml
# application.yml
common-lib:
  logging:
    enabled: true                    # Enable/disable structured logging (default: true)
    request-logging: true            # Log incoming requests (default: true)
    response-logging: true           # Log responses (default: true)
    log-request-body: false          # Log request body - DISABLED by default for performance
    log-response-body: false         # Log response body - DISABLED by default
    max-body-size: 1024             # Max body size to log in bytes (default: 1KB)
    sensitive-fields:                # Additional fields to redact (beyond defaults)
      - customSecret
      - internalToken
    excluded-paths:                  # Paths to skip logging (health checks, etc.)
      - /actuator/health
      - /actuator/prometheus
      - /health
      - /favicon.ico
```

### Default Sensitive Fields

Automatically redacted from logs (case-insensitive):
- `password`, `passwd`, `pwd`
- `token`, `authorization`, `auth`
- `secret`, `api-key`, `apikey`
- `credit-card`, `creditcard`, `cvv`
- `ssn`, `social-security`

### Example Log Output

**Request:**
```log
2026-01-30 23:45:12.123 INFO  [product-service,abc123,trace456,span789] RequestResponseLoggingFilter - HTTP Request: POST /api/products
```

**Response:**
```log
2026-01-30 23:45:12.234 INFO  [product-service,abc123,trace456,span789] RequestResponseLoggingFilter - HTTP Response: POST /api/products | Status: 201 | Duration: 111ms
```

**With Body Logging Enabled:**
```log
2026-01-30 23:45:12.123 INFO  [product-service,abc123,trace456,span789] RequestResponseLoggingFilter - HTTP Request: POST /api/auth/login | Body: {"username":"john","password":"***REDACTED***"}
```

### Correlation ID Headers

The correlation ID is automatically:
1. **Extracted** from incoming `X-Correlation-ID` or `X-Request-ID` headers
2. **Generated** as a UUID if not present
3. **Propagated** to response headers as `X-Correlation-ID`
4. **Added to MDC** for automatic inclusion in all logs
5. **Integrated** with distributed tracing (Micrometer/Zipkin)

### Custom Logback Configuration

Override the default log pattern by creating `logback-spring.xml` in your service:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%X{serviceName},%X{correlationId},%X{traceId},%X{spanId}] %logger{36} - %msg%n
            </pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### Performance Considerations

- **Body logging disabled by default** - Enable only for debugging
- **Async logging** - Non-blocking with 512-message queue
- **Path exclusions** - Health checks and metrics endpoints skipped
- **Max body size** - Truncates large bodies (default 1KB)
- **Overhead** - <5ms per request with body logging disabled

### Integration with Distributed Tracing

Correlation IDs automatically integrate with Micrometer/Zipkin:
- MDC `traceId` and `spanId` extracted from current span
- Correlation ID persists across service calls
- View complete request flow in Zipkin UI with matching correlation IDs

## 📦 Maven Commands

Build the library:
```bash
mvn clean install
```

Use in other modules:
```bash
# Just add the dependency - Maven will resolve it from the parent reactor
```

## 🧪 Testing

Unit tests for your service exceptions:

```java
@Test
void testResourceNotFound() {
    ResourceNotFoundException ex = 
        new ResourceNotFoundException("Product", "123");
    
    assertEquals("RESOURCE_NOT_FOUND", ex.getErrorCode());
    assertEquals("Product not found with identifier: 123", ex.getMessage());
}
```

## 🔄 Migration Guide

### Step 1: Add Dependency
```xml
<dependency>
    <groupId>com.microservices</groupId>
    <artifactId>common-lib</artifactId>
    <version>${project.version}</version>
</dependency>
```

### Step 2: Update Imports
Replace:
```java
import com.yourservice.exception.ResourceNotFoundException;
```

With:
```java
import com.microservices.common.exception.ResourceNotFoundException;
```

### Step 3: Remove Local Classes (Optional)
Delete local copies of:
- `BusinessException.java`
- `ResourceNotFoundException.java`
- `ErrorResponse.java`
- `ApiResponse.java`
- `GlobalExceptionHandler.java` (if using BaseExceptionHandler)

### Step 4: Build & Test
```bash
mvn clean package
```

## 📚 Dependencies

This library requires:
- Spring Boot 3.2.1+
- Spring Web
- Spring Validation
- Lombok 1.18.36+
- SpringDoc OpenAPI (optional, for API docs)
- Spring Cloud OpenFeign (optional, for Feign error handling)

## 🤝 Contributing

To add new common exceptions or utilities:

1. Add to `common-lib/src/main/java/com/microservices/common/`
2. Update `BaseExceptionHandler` if needed
3. Document in this README
4. Build: `mvn clean install`
5. All services automatically get the update!

## 📄 License

Same as parent project (MIT License)
