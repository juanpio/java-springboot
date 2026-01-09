package com.microservices.orderservice.exception;

/**
 * Exception thrown when an order cannot be cancelled
 */
public class OrderCancellationException extends BusinessException {
    
    public OrderCancellationException(Long orderId, String reason) {
        super(String.format("Cannot cancel order %d: %s", orderId, reason),
              "ORDER_CANCELLATION_FAILED");
    }
    
    public OrderCancellationException(String message) {
        super(message, "ORDER_CANCELLATION_FAILED");
    }
}
