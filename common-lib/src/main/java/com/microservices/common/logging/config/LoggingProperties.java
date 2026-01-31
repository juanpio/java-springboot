package com.microservices.common.logging.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for structured logging.
 */
@Data
@ConfigurationProperties(prefix = "common-lib.logging")
public class LoggingProperties {
    
    /**
     * Enable structured logging features.
     */
    private boolean enabled = true;
    
    /**
     * Enable request logging.
     */
    private boolean requestLogging = true;
    
    /**
     * Enable response logging.
     */
    private boolean responseLogging = true;
    
    /**
     * Log request body (disabled by default for performance).
     */
    private boolean logRequestBody = false;
    
    /**
     * Log response body (disabled by default for performance).
     */
    private boolean logResponseBody = false;
    
    /**
     * Maximum body size to log in bytes (default 1KB).
     */
    private int maxBodySize = 1024;
    
    /**
     * Custom sensitive field names to redact from logs.
     */
    private List<String> sensitiveFields = new ArrayList<>();
    
    /**
     * URLs to exclude from request/response logging (e.g., health checks).
     */
    private List<String> excludedPaths = List.of(
        "/actuator/health",
        "/actuator/prometheus",
        "/health",
        "/favicon.ico"
    );
}
