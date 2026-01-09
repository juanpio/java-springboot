package com.microservices.userservice.mapper;

import com.microservices.user.dto.UserResponse;
import com.microservices.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for User entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {
    
    /**
     * Convert User entity to UserResponse DTO
     * Password field is automatically excluded as it's not in UserResponse
     */
    UserResponse toUserResponse(User user);
    
    /**
     * Convert list of User entities to list of UserResponse DTOs
     */
    List<UserResponse> toUserResponseList(List<User> users);
}
