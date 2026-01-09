package com.microservices.productservice.mapper;

import com.microservices.product.dto.ProductRequest;
import com.microservices.product.dto.ProductResponse;
import com.microservices.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Product entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {
    
    /**
     * Convert Product entity to ProductResponse DTO
     */
    ProductResponse toProductResponse(Product product);
    
    /**
     * Convert list of Product entities to list of ProductResponse DTOs
     */
    List<ProductResponse> toProductResponseList(List<Product> products);
    
    /**
     * Convert ProductRequest DTO to Product entity
     */
    Product toProduct(ProductRequest request);
    
    /**
     * Update existing Product entity from ProductRequest DTO
     */
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product product);
}
