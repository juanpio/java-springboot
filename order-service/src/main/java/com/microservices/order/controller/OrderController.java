package com.microservices.order.controller;

import com.microservices.order.dto.OrderRequest;
import com.microservices.order.dto.OrderResponse;
import com.microservices.order.entity.OrderStatus;
import com.microservices.order.service.OrderService;
import com.microservices.orderservice.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing orders
 * All endpoints follow REST conventions and return standardized responses
 */
@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Order Management", description = "APIs for managing orders and order lifecycle")
public class OrderController {

    private final OrderService orderService;

    /**
     * Constructor injection for better testability and immutability
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order with the specified items for a user. Validates product availability and calculates total amount."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request or validation error"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Insufficient stock for requested products"
        )
    })
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody 
            @Parameter(description = "Order creation request with items") OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(
            ApiResponse.success(response, "Order created successfully"),
            HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(
        summary = "Get all orders",
        description = "Retrieves a list of all orders in the system"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Orders retrieved successfully"
        )
    })
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(
            ApiResponse.success(orders, "Orders retrieved successfully")
        );
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves detailed information about a specific order"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Order found and returned"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Order not found"
        )
    })
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable 
            @Parameter(description = "Order ID", example = "1") Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(
            ApiResponse.success(order, "Order retrieved successfully")
        );
    }

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get orders by user ID",
        description = "Retrieves all orders for a specific user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User orders retrieved successfully"
        )
    })
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserId(
            @PathVariable 
            @Parameter(description = "User ID", example = "1") Long userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(
            ApiResponse.success(orders, "User orders retrieved successfully")
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update order status",
        description = "Updates the status of an existing order (e.g., PENDING, PROCESSING, SHIPPED, DELIVERED)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Order status updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Order not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid status transition"
        )
    })
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable 
            @Parameter(description = "Order ID", example = "1") Long id,
            @RequestParam 
            @Parameter(description = "New order status", example = "SHIPPED") OrderStatus status) {
        OrderResponse order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(
            ApiResponse.success(order, "Order status updated successfully")
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancel an order",
        description = "Cancels an order if it hasn't been shipped or delivered yet"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Order cancelled successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Order not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Order cannot be cancelled in current status"
        )
    })
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable 
            @Parameter(description = "Order ID", example = "1") Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(
            ApiResponse.success("Order cancelled successfully")
        );
    }
}

