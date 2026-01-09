package com.microservices.orderservice.exception;

/**
 * Exception thrown when an order status transition is invalid
 */
public class InvalidOrderStatusException extends BusinessException {
    
    public InvalidOrderStatusException(String currentStatus, String targetStatus) {
        super(String.format("Cannot transition order from %s to %s", currentStatus, targetStatus),
              "INVALID_ORDER_STATUS");
    }
    
    public InvalidOrderStatusException(String message) {
        super(message, "INVALID_ORDER_STATUS");
    }
}
