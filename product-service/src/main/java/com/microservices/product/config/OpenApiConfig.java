package com.microservices.product.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for Product Service
 * Access Swagger UI at: http://localhost:8081/swagger-ui.html
 * Access API docs at: http://localhost:8081/v3/api-docs
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Product Service API",
        version = "1.0.0",
        description = "Microservice for managing product catalog, inventory, and stock operations",
        contact = @Contact(
            name = "Product Service Team",
            email = "products@microservices.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "Local Development Server"),
        @Server(url = "http://localhost:8080", description = "API Gateway")
    }
)
public class OpenApiConfig {
}
