package com.microservices.common.exception;

/**
 * Exception thrown when a requested resource is not found
 * HTTP Status: 404 NOT FOUND
 */
public class ResourceNotFoundException extends BusinessException {
    
    /**
     * Create exception with resource type and identifier
     * @param resourceName Type of resource (e.g., "User", "Product", "Order")
     * @param identifier The identifier used to search for the resource
     */
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier), 
              "RESOURCE_NOT_FOUND");
    }
    
    /**
     * Create exception with custom message
     * @param message Custom error message
     */
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}
