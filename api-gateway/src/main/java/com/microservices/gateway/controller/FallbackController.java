package com.microservices.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, String>> userServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("error", "User Service is currently unavailable");
        response.put("message", "Please try again later");
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping("/product-service")
    public ResponseEntity<Map<String, String>> productServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Product Service is currently unavailable");
        response.put("message", "Please try again later");
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping("/order-service")
    public ResponseEntity<Map<String, String>> orderServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Order Service is currently unavailable");
        response.put("message", "Please try again later");
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
