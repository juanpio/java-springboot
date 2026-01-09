package com.microservices.product.controller;

import com.microservices.product.dto.ProductRequest;
import com.microservices.product.dto.ProductResponse;
import com.microservices.product.service.ProductService;
import com.microservices.productservice.dto.ApiResponse;
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
 * REST controller for managing products
 * All endpoints follow REST conventions and return standardized responses
 */
@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Product Management", description = "APIs for managing product catalog")
public class ProductController {

    private final ProductService productService;

    /**
     * Constructor injection for better testability and immutability
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(
        summary = "Create a new product",
        description = "Adds a new product to the catalog with specified details"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request or validation error"
        )
    })
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody 
            @Parameter(description = "Product creation request") ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(
            ApiResponse.success(response, "Product created successfully"),
            HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(
        summary = "Get all products",
        description = "Retrieves a list of all products in the catalog"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(
            ApiResponse.success(products, "Products retrieved successfully")
        );
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieves detailed information about a specific product"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable 
            @Parameter(description = "Product ID", example = "1") Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(
            ApiResponse.success(product, "Product retrieved successfully")
        );
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get products by category",
        description = "Retrieves all products in a specific category"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable 
            @Parameter(description = "Product category", example = "Electronics") String category) {
        List<ProductResponse> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(
            ApiResponse.success(products, "Products retrieved successfully")
        );
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search products by name",
        description = "Searches for products matching the provided name"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam 
            @Parameter(description = "Product name search term", example = "laptop") String name) {
        List<ProductResponse> products = productService.searchProducts(name);
        return ResponseEntity.ok(
            ApiResponse.success(products, "Search completed successfully")
        );
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update a product",
        description = "Updates an existing product with new details"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable 
            @Parameter(description = "Product ID", example = "1") Long id,
            @Valid @RequestBody 
            @Parameter(description = "Product update request") ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(
            ApiResponse.success(product, "Product updated successfully")
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a product",
        description = "Removes a product from the catalog"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable 
            @Parameter(description = "Product ID", example = "1") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
            ApiResponse.success("Product deleted successfully")
        );
    }

    @PatchMapping("/{id}/stock")
    @Operation(
        summary = "Update product stock",
        description = "Updates the available stock quantity for a product"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable 
            @Parameter(description = "Product ID", example = "1") Long id,
            @RequestParam 
            @Parameter(description = "New stock quantity", example = "100") Integer quantity) {
        productService.updateStock(id, quantity);
        return ResponseEntity.ok(
            ApiResponse.success("Stock updated successfully")
        );
    }
}
