package com.microservices.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service-route", r -> r
                        .path("/api/auth/**", "/api/users/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("userService")
                                        .setFallbackUri("forward:/fallback/user-service")))
                        .uri("lb://user-service"))
                .route("product-service-route", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("productService")
                                        .setFallbackUri("forward:/fallback/product-service")))
                        .uri("lb://product-service"))
                .route("order-service-route", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("orderService")
                                        .setFallbackUri("forward:/fallback/order-service")))
                        .uri("lb://order-service"))
                .build();
    }
}
