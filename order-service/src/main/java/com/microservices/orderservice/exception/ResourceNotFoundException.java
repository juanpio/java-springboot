package com.microservices.orderservice.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier), 
              "RESOURCE_NOT_FOUND");
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}
