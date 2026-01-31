package com.microservices.common.logging.filter;

import com.microservices.common.logging.config.LoggingProperties;
import com.microservices.common.logging.util.SensitiveDataFilter;
import com.microservices.common.logging.wrapper.CachedBodyHttpServletRequest;
import com.microservices.common.logging.wrapper.CachedBodyHttpServletResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Filter to log HTTP requests and responses with timing information.
 */
@Slf4j
@Order(2)
public class RequestResponseLoggingFilter implements Filter {
    
    private final LoggingProperties properties;
    private final SensitiveDataFilter sensitiveDataFilter;
    
    public RequestResponseLoggingFilter(LoggingProperties properties) {
        this.properties = properties;
        this.sensitiveDataFilter = new SensitiveDataFilter(properties.getSensitiveFields());
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (!properties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Skip logging for excluded paths
        if (shouldSkipLogging(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Wrap request and response to cache bodies
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpRequest);
        CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse(httpResponse);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log request
            if (properties.isRequestLogging()) {
                logRequest(wrappedRequest);
            }
            
            chain.doFilter(wrappedRequest, wrappedResponse);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log response
            if (properties.isResponseLogging()) {
                logResponse(wrappedRequest, wrappedResponse, duration);
            }
        }
    }
    
    private void logRequest(CachedBodyHttpServletRequest request) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("HTTP Request: ")
                  .append(request.getMethod())
                  .append(" ")
                  .append(request.getRequestURI());
        
        if (request.getQueryString() != null) {
            logMessage.append("?").append(request.getQueryString());
        }
        
        // Log headers at DEBUG level
        if (log.isDebugEnabled()) {
            String headers = Collections.list(request.getHeaderNames()).stream()
                .map(headerName -> headerName + ": " + request.getHeader(headerName))
                .collect(Collectors.joining(", "));
            logMessage.append(" | Headers: [").append(headers).append("]");
        }
        
        // Log body if enabled
        if (properties.isLogRequestBody()) {
            String body = getRequestBody(request);
            if (body != null && !body.isBlank()) {
                body = sensitiveDataFilter.filterJson(body);
                logMessage.append(" | Body: ").append(body);
            }
        }
        
        log.info(logMessage.toString());
    }
    
    private void logResponse(CachedBodyHttpServletRequest request, 
                            CachedBodyHttpServletResponse response, 
                            long duration) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("HTTP Response: ")
                  .append(request.getMethod())
                  .append(" ")
                  .append(request.getRequestURI())
                  .append(" | Status: ")
                  .append(response.getStatus())
                  .append(" | Duration: ")
                  .append(duration)
                  .append("ms");
        
        // Log body if enabled
        if (properties.isLogResponseBody()) {
            String body = getResponseBody(response);
            if (body != null && !body.isBlank()) {
                body = sensitiveDataFilter.filterJson(body);
                logMessage.append(" | Body: ").append(body);
            }
        }
        
        // Use appropriate log level based on status code
        if (response.getStatus() >= 500) {
            log.error(logMessage.toString());
        } else if (response.getStatus() >= 400) {
            log.warn(logMessage.toString());
        } else {
            log.info(logMessage.toString());
        }
    }
    
    private String getRequestBody(CachedBodyHttpServletRequest request) {
        byte[] body = request.getCachedBody();
        if (body.length == 0) {
            return null;
        }
        
        int length = Math.min(body.length, properties.getMaxBodySize());
        String bodyString = new String(body, 0, length, StandardCharsets.UTF_8);
        
        if (body.length > properties.getMaxBodySize()) {
            bodyString += "... (truncated)";
        }
        
        return bodyString;
    }
    
    private String getResponseBody(CachedBodyHttpServletResponse response) {
        byte[] body = response.getCachedBody();
        if (body.length == 0) {
            return null;
        }
        
        int length = Math.min(body.length, properties.getMaxBodySize());
        String bodyString = new String(body, 0, length, StandardCharsets.UTF_8);
        
        if (body.length > properties.getMaxBodySize()) {
            bodyString += "... (truncated)";
        }
        
        return bodyString;
    }
    
    private boolean shouldSkipLogging(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return properties.getExcludedPaths().stream()
            .anyMatch(uri::contains);
    }
}
