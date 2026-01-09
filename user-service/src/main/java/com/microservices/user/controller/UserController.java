package com.microservices.user.controller;

import com.microservices.user.entity.User;
import com.microservices.user.repository.UserRepository;
import com.microservices.userservice.dto.ApiResponse;
import com.microservices.userservice.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing users
 * Requires authentication for all endpoints
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "User Management", description = "APIs for managing users (requires authentication)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

    /**
     * Constructor injection for better testability and immutability
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all registered users. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - requires ADMIN role"
        )
    })
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(
            ApiResponse.success(users, "Users retrieved successfully")
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves detailed information about a specific user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<ApiResponse<User>> getUserById(
            @PathVariable 
            @Parameter(description = "User ID", example = "1") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        return ResponseEntity.ok(
            ApiResponse.success(user, "User retrieved successfully")
        );
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get user by username",
        description = "Retrieves user information by their username"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<ApiResponse<User>> getUserByUsername(
            @PathVariable 
            @Parameter(description = "Username", example = "john.doe") String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        return ResponseEntity.ok(
            ApiResponse.success(user, "User retrieved successfully")
        );
    }
}
