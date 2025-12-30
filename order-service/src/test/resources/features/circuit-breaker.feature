Feature: Circuit Breaker Integration
  As a system administrator
  I want the order service to handle downstream service failures gracefully
  So that the system remains resilient

  Background:
    Given the order service is running

  Scenario: Create order when product service is available
    Given the product service is available
    And the user service is available
    And a product with id 1 and name "Laptop" and price 999.99 exists
    And a user with id 1 and username "john_doe" exists
    When I create an order for user 1 with product 1 and quantity 1
    Then the order should be created successfully
    And the product service should be called

  Scenario: Handle product service failure with circuit breaker
    Given the product service is unavailable
    And a user with id 1 and username "john_doe" exists
    When I create an order for user 1 with product 1 and quantity 1
    Then the order creation should fail
    And the error message should contain "unavailable"
    And the circuit breaker should be activated

  Scenario: Use fallback when user service fails
    Given the product service is available
    And the user service is unavailable
    And a product with id 1 and name "Laptop" and price 999.99 exists
    When I create an order for user 1 with product 1 and quantity 1
    Then the order should be created successfully
    And the order username should be "Unknown User"
    And the user service fallback should be used

  Scenario: Retry mechanism on transient failures
    Given the product service fails 2 times then succeeds
    And the user service is available
    And a product with id 1 and name "Laptop" and price 999.99 exists
    And a user with id 1 and username "john_doe" exists
    When I create an order for user 1 with product 1 and quantity 1
    Then the order should be created successfully
    And the product service should be called 3 times
    And the retry mechanism should have been triggered

  Scenario: Circuit breaker opens after multiple failures
    Given the product service is consistently failing
    And the user service is available
    And a user with id 1 and username "john_doe" exists
    When I attempt to create 10 orders sequentially
    Then the first few orders should fail with service errors
    And subsequent orders should fail immediately with circuit breaker open error
    And the circuit breaker should be in OPEN state
