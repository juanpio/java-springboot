Feature: Order Management
  As a customer
  I want to create and manage orders
  So that I can purchase products

  Background:
    Given the order service is running
    And a user exists with id 1 and username "john_doe"
    And the following products exist:
      | id | name        | price | quantity |
      | 1  | Laptop      | 999.99| 10       |
      | 2  | Mouse       | 29.99 | 50       |
      | 3  | Keyboard    | 79.99 | 30       |

  Scenario: Successfully create an order with single item
    Given I am authenticated as user with id 1
    When I create an order with the following items:
      | productId | quantity |
      | 1         | 1        |
    Then the order should be created successfully
    And the order status should be "PENDING"
    And the order total amount should be 999.99
    And the order should contain 1 item

  Scenario: Successfully create an order with multiple items
    Given I am authenticated as user with id 1
    When I create an order with the following items:
      | productId | quantity |
      | 1         | 2        |
      | 2         | 3        |
    Then the order should be created successfully
    And the order status should be "PENDING"
    And the order total amount should be 2089.95
    And the order should contain 2 items

  Scenario: Fail to create order with insufficient stock
    Given I am authenticated as user with id 1
    When I create an order with the following items:
      | productId | quantity |
      | 1         | 100      |
    Then the order creation should fail
    And the error message should contain "Insufficient stock"

  Scenario: Get all orders
    Given the following orders exist:
      | userId | totalAmount | status  |
      | 1      | 999.99      | PENDING |
      | 1      | 500.00      | CONFIRMED |
    When I request all orders
    Then I should receive 2 orders

  Scenario: Get orders by user id
    Given the following orders exist:
      | userId | totalAmount | status  |
      | 1      | 999.99      | PENDING |
      | 1      | 500.00      | CONFIRMED |
      | 2      | 250.00      | PENDING |
    When I request orders for user with id 1
    Then I should receive 2 orders
    And all orders should belong to user with id 1

  Scenario: Update order status
    Given an order exists with id 1 and status "PENDING"
    When I update the order status to "CONFIRMED"
    Then the order status should be "CONFIRMED"

  Scenario: Cancel a pending order
    Given an order exists with id 1 and status "PENDING"
    When I cancel the order
    Then the order status should be "CANCELLED"

  Scenario: Fail to cancel a delivered order
    Given an order exists with id 1 and status "DELIVERED"
    When I cancel the order
    Then the cancellation should fail
    And the error message should contain "Cannot cancel order"

  Scenario: Fail to cancel a shipped order
    Given an order exists with id 1 and status "SHIPPED"
    When I cancel the order
    Then the cancellation should fail
    And the error message should contain "Cannot cancel order"
