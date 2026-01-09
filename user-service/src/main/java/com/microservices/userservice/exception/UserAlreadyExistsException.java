package com.microservices.userservice.exception;

/**
 * Exception thrown when a user already exists
 */
public class UserAlreadyExistsException extends BusinessException {
    
    public UserAlreadyExistsException(String email) {
        super(String.format("User with email %s already exists", email),
              "USER_ALREADY_EXISTS");
    }
    
    public UserAlreadyExistsException(String message, String errorCode) {
        super(message, errorCode);
    }
}
