package com.microservices.user.controller;

import com.microservices.user.dto.JwtResponse;
import com.microservices.user.dto.LoginRequest;
import com.microservices.user.dto.MessageResponse;
import com.microservices.user.dto.SignupRequest;
import com.microservices.user.service.AuthService;
import com.microservices.userservice.dto.ApiResponse;
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

/**
 * REST controller for authentication and registration
 * Provides endpoints for user login and signup
 */
@RestController
@RequestMapping(\"/api/v1/auth\")
@CrossOrigin(origins = \"*\", maxAge = 3600)
@Tag(name = \"Authentication\", description = \"APIs for user authentication and registration\")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor injection for better testability and immutability
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(\"/login\")
    @Operation(
        summary = \"User login\",
        description = \"Authenticates a user and returns a JWT token for accessing protected endpoints\"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = \"200\",
            description = \"Authentication successful\",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = \"401\",
            description = \"Invalid credentials\"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = \"400\",
            description = \"Validation error\"\n        )\n    })
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(\n            @Valid @RequestBody \n            @Parameter(description = \"Login credentials\") LoginRequest loginRequest) {\n        JwtResponse response = authService.authenticateUser(loginRequest);\n        return ResponseEntity.ok(\n            ApiResponse.success(response, \"Login successful\")\n        );\n    }\n\n    @PostMapping(\"/signup\")
    @Operation(
        summary = \"User registration\",
        description = \"Registers a new user in the system with the provided credentials\"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = \"201\",
            description = \"User registered successfully\"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = \"409\",
            description = \"User already exists\"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = \"400\",
            description = \"Validation error\"
        )
    })
    public ResponseEntity<ApiResponse<MessageResponse>> registerUser(\n            @Valid @RequestBody \n            @Parameter(description = \"User registration details\") SignupRequest signupRequest) {\n        MessageResponse response = authService.registerUser(signupRequest);\n        if (response.getMessage().startsWith(\"Error\")) {\n            return new ResponseEntity<>(\n                ApiResponse.error(response.getMessage()),\n                HttpStatus.BAD_REQUEST\n            );\n        }\n        return new ResponseEntity<>(\n            ApiResponse.success(response, \"Registration successful\"),\n            HttpStatus.CREATED\n        );\n    }\n}
