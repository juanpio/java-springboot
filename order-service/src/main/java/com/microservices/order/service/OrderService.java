package com.microservices.order.service;

import com.microservices.order.client.ProductClient;
import com.microservices.order.client.UserClient;
import com.microservices.order.dto.*;
import com.microservices.order.entity.Order;
import com.microservices.order.entity.OrderItem;
import com.microservices.order.entity.OrderStatus;
import com.microservices.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private UserClient userClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "createOrderFallback")
    @Retry(name = "productService")
    public OrderResponse createOrder(OrderRequest request) {
        logger.info("Creating order for user: {}", request.getUserId());

        // Fetch user details with circuit breaker
        UserDTO user = getUserWithCircuitBreaker(request.getUserId());

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setUsername(user.getUsername());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Process each order item
        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductDTO product = productClient.getProductById(itemRequest.getProductId());

            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            order.addItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        logger.info("Order created successfully with ID: {}", savedOrder.getId());
        return mapToResponse(savedOrder);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    public UserDTO getUserWithCircuitBreaker(Long userId) {
        return userClient.getUserById(userId);
    }

    public OrderResponse createOrderFallback(OrderRequest request, Exception ex) {
        logger.error("Circuit breaker activated for createOrder. Error: {}", ex.getMessage());
        throw new RuntimeException("Product service is currently unavailable. Please try again later.");
    }

    public UserDTO getUserFallback(Long userId, Exception ex) {
        logger.error("Circuit breaker activated for getUserById. Error: {}", ex.getMessage());
        UserDTO fallbackUser = new UserDTO();
        fallbackUser.setId(userId);
        fallbackUser.setUsername("Unknown User");
        fallbackUser.setEmail("unknown@example.com");
        return fallbackUser;
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new RuntimeException("Cannot cancel order in " + order.getStatus() + " status");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setUsername(order.getUsername());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}
