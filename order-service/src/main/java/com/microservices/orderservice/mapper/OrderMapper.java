package com.microservices.orderservice.mapper;

import com.microservices.order.dto.OrderResponse;
import com.microservices.order.dto.OrderItemResponse;
import com.microservices.order.entity.Order;
import com.microservices.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Order entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper {
    
    /**
     * Convert Order entity to OrderResponse DTO
     */
    OrderResponse toOrderResponse(Order order);
    
    /**
     * Convert list of Order entities to list of OrderResponse DTOs
     */
    List<OrderResponse> toOrderResponseList(List<Order> orders);
    
    /**
     * Convert OrderItem entity to OrderItemResponse DTO
     */
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
    
    /**
     * Convert list of OrderItem entities to list of OrderItemResponse DTOs
     */
    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);
}
