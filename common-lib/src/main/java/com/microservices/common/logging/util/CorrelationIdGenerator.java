package com.microservices.common.logging.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static com.microservices.common.logging.constants.LoggingConstants.CORRELATION_ID_HEADER;
import static com.microservices.common.logging.constants.LoggingConstants.REQUEST_ID_HEADER;

/**
 * Utility class for generating and extracting correlation IDs.
 */
public class CorrelationIdGenerator {
    
    /**
     * Get or generate a correlation ID from the request.
     * Checks X-Correlation-ID header first, then X-Request-ID, then generates a new UUID.
     */
    public static String getOrGenerate(HttpServletRequest request) {
        // Try X-Correlation-ID first
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (StringUtils.hasText(correlationId)) {
            return correlationId;
        }
        
        // Try X-Request-ID as fallback
        correlationId = request.getHeader(REQUEST_ID_HEADER);
        if (StringUtils.hasText(correlationId)) {
            return correlationId;
        }
        
        // Generate new UUID
        return generate();
    }
    
    /**
     * Generate a new correlation ID.
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Validate if a string is a valid correlation ID (non-empty).
     */
    public static boolean isValid(String correlationId) {
        return StringUtils.hasText(correlationId);
    }
}
