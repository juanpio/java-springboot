package com.microservices.common.exception;

/**
 * Exception thrown when there's insufficient stock for a product
 * HTTP Status: 409 CONFLICT
 */
public class InsufficientStockException extends BusinessException {
    
    /**
     * Create exception for insufficient stock
     * @param productId ID of the product
     * @param requested Quantity requested
     * @param available Quantity available
     */
    public InsufficientStockException(Long productId, int requested, int available) {
        super(String.format("Insufficient stock for product %d. Requested: %d, Available: %d", 
              productId, requested, available), 
              "INSUFFICIENT_STOCK");
    }
    
    /**
     * Create exception with custom message
     * @param message Custom error message
     */
    public InsufficientStockException(String message) {
        super(message, "INSUFFICIENT_STOCK");
    }
}
