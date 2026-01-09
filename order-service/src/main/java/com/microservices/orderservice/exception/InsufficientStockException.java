package com.microservices.orderservice.exception;

/**
 * Exception thrown when there is insufficient stock for a product
 */
public class InsufficientStockException extends BusinessException {
    
    public InsufficientStockException(Long productId, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for product %d. Requested: %d, Available: %d", 
                productId, requestedQuantity, availableStock),
              "INSUFFICIENT_STOCK");
    }
    
    public InsufficientStockException(String message) {
        super(message, "INSUFFICIENT_STOCK");
    }
}
