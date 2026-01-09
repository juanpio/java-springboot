package com.microservices.user.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for User Service
 * Access Swagger UI at: http://localhost:8083/swagger-ui.html
 * Access API docs at: http://localhost:8083/v3/api-docs
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "User Service API",
        version = "1.0.0",
        description = "Microservice for user management and JWT-based authentication",
        contact = @Contact(
            name = "User Service Team",
            email = "users@microservices.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8083", description = "Local Development Server"),
        @Server(url = "http://localhost:8080", description = "API Gateway")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "JWT Authorization header using the Bearer scheme. Example: 'Bearer {token}'"
)
public class OpenApiConfig {
}
