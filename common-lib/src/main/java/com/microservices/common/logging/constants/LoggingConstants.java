package com.microservices.common.logging.constants;

/**
 * Constants for logging and correlation ID management.
 */
public final class LoggingConstants {
    
    // Header names
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    // MDC keys
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_SERVICE_NAME = "serviceName";
    public static final String MDC_REQUEST_METHOD = "requestMethod";
    public static final String MDC_REQUEST_URI = "requestUri";
    
    // Sensitive field names (case-insensitive)
    public static final String[] DEFAULT_SENSITIVE_FIELDS = {
        "password", "passwd", "pwd",
        "token", "authorization", "auth",
        "secret", "api-key", "apikey",
        "credit-card", "creditcard", "cvv",
        "ssn", "social-security"
    };
    
    private LoggingConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
