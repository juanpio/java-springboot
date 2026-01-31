package com.microservices.common.logging.filter;

import com.microservices.common.logging.util.CorrelationIdGenerator;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Principal;

import static com.microservices.common.logging.constants.LoggingConstants.*;

/**
 * Filter to populate MDC (Mapped Diagnostic Context) with correlation ID,
 * trace ID, span ID, user ID, and other request context.
 * This ensures all log statements include this context automatically.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCFilter implements Filter {
    
    private final Tracer tracer;
    private final String serviceName;
    
    public MDCFilter(Tracer tracer, String serviceName) {
        this.tracer = tracer;
        this.serviceName = serviceName;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Generate or extract correlation ID
            String correlationId = CorrelationIdGenerator.getOrGenerate(httpRequest);
            MDC.put(MDC_CORRELATION_ID, correlationId);
            
            // Add correlation ID to response headers for client tracking
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Extract trace and span IDs from Micrometer
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                MDC.put(MDC_TRACE_ID, currentSpan.context().traceId());
                MDC.put(MDC_SPAN_ID, currentSpan.context().spanId());
            }
            
            // Extract user ID from security context
            Principal principal = httpRequest.getUserPrincipal();
            if (principal != null) {
                MDC.put(MDC_USER_ID, principal.getName());
            }
            
            // Add service name
            if (serviceName != null) {
                MDC.put(MDC_SERVICE_NAME, serviceName);
            }
            
            // Add request method and URI
            MDC.put(MDC_REQUEST_METHOD, httpRequest.getMethod());
            MDC.put(MDC_REQUEST_URI, httpRequest.getRequestURI());
            
            chain.doFilter(request, response);
            
        } finally {
            // Clean up MDC after request completes
            MDC.clear();
        }
    }
}
