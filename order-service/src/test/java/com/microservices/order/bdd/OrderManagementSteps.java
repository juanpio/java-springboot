package com.microservices.order.bdd;

import com.microservices.order.client.ProductClient;
import com.microservices.order.client.UserClient;
import com.microservices.order.dto.ProductDTO;
import com.microservices.order.dto.UserDTO;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.microservices.order.dto.OrderRequest;
import com.microservices.order.dto.OrderItemRequest;
import com.microservices.order.entity.Order;
import com.microservices.order.entity.OrderStatus;
import com.microservices.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class OrderManagementSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private UserClient userClient;

    private Response response;
    private Long currentOrderId;
    private Long currentUserId;
    private OrderRequest orderRequest;
    private Exception lastException;
    private Map<Long, ProductDTO> mockProducts = new HashMap<>();
    private Map<Long, UserDTO> mockUsers = new HashMap<>();

    @Given("a user exists with id {long} and username {string}")
    public void aUserExistsWithIdAndUsername(Long userId, String username) {
        this.currentUserId = userId;
        
        // Mock user service response
        UserDTO user = new UserDTO();
        user.setId(userId);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        mockUsers.put(userId, user);
        
        when(userClient.getUserById(userId)).thenReturn(user);
    }

    @Given("the following products exist:")
    public void theFollowingProductsExist(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        
        // Mock product service responses
        for (Map<String, String> product : products) {
            Long id = Long.parseLong(product.get("id"));
            String name = product.get("name");
            BigDecimal price = new BigDecimal(product.get("price"));
            Integer quantity = Integer.parseInt(product.get("quantity"));
            
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(id);
            productDTO.setName(name);
            productDTO.setPrice(price);
            productDTO.setQuantity(quantity);
            mockProducts.put(id, productDTO);
            
            when(productClient.getProductById(id)).thenReturn(productDTO);
        }
    }

    @Given("I am authenticated as user with id {long}")
    public void iAmAuthenticatedAsUserWithId(Long userId) {
        this.currentUserId = userId;
    }

    @When("I create an order with the following items:")
    public void iCreateAnOrderWithTheFollowingItems(DataTable dataTable) {
        List<Map<String, String>> items = dataTable.asMaps(String.class, String.class);
        
        orderRequest = new OrderRequest();
        orderRequest.setUserId(currentUserId);
        
        List<OrderItemRequest> orderItems = new ArrayList<>();
        for (Map<String, String> item : items) {
            OrderItemRequest itemRequest = new OrderItemRequest();
            itemRequest.setProductId(Long.parseLong(item.get("productId")));
            itemRequest.setQuantity(Integer.parseInt(item.get("quantity")));
            orderItems.add(itemRequest);
        }
        orderRequest.setItems(orderItems);

        try {
            response = given()
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .when()
                .post()
                .then()
                .extract()
                .response();
                
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                currentOrderId = response.jsonPath().getLong("id");
            }
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the order should be created successfully")
    public void theOrderShouldBeCreatedSuccessfully() {
        assertNotNull(response);
        assertTrue(response.statusCode() == 200 || response.statusCode() == 201,
            "Expected status 200 or 201 but got " + response.statusCode());
        assertNotNull(currentOrderId);
    }

    @Then("the order status should be {string}")
    public void theOrderStatusShouldBe(String expectedStatus) {
        String actualStatus = response.jsonPath().getString("status");
        assertEquals(expectedStatus, actualStatus);
    }

    @Then("the order total amount should be {double}")
    public void theOrderTotalAmountShouldBe(double expectedAmount) {
        double actualAmount = response.jsonPath().getDouble("totalAmount");
        assertEquals(expectedAmount, actualAmount, 0.01);
    }

    @Then("the order should contain {int} item(s)")
    public void theOrderShouldContainItems(int expectedItemCount) {
        List<Map<String, Object>> items = response.jsonPath().getList("items");
        assertEquals(expectedItemCount, items.size());
    }

    @Then("the order creation should fail")
    public void theOrderCreationShouldFail() {
        assertNotNull(response);
        assertTrue(response.statusCode() >= 400 && response.statusCode() < 600,
            "Expected error status but got " + response.statusCode());
    }

    @Then("the error message should contain {string}")
    public void theErrorMessageShouldContain(String expectedMessage) {
        String errorMessage = response.jsonPath().getString("message");
        if (errorMessage == null) {
            errorMessage = response.asString();
        }
        assertTrue(errorMessage.contains(expectedMessage),
            "Expected error message to contain '" + expectedMessage + "' but was '" + errorMessage + "'");
    }

    @Given("the following orders exist:")
    public void theFollowingOrdersExist(DataTable dataTable) {
        List<Map<String, String>> orders = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> orderData : orders) {
            Order order = new Order();
            order.setUserId(Long.parseLong(orderData.get("userId")));
            order.setUsername("user_" + orderData.get("userId"));
            order.setTotalAmount(new BigDecimal(orderData.get("totalAmount")));
            order.setStatus(OrderStatus.valueOf(orderData.get("status")));
            orderRepository.save(order);
        }
    }

    @When("I request all orders")
    public void iRequestAllOrders() {
        response = given()
            .when()
            .get()
            .then()
            .extract()
            .response();
    }

    @Then("I should receive {int} order(s)")
    public void iShouldReceiveOrders(int expectedCount) {
        List<Map<String, Object>> orders = response.jsonPath().getList("$");
        assertEquals(expectedCount, orders.size());
    }

    @When("I request orders for user with id {long}")
    public void iRequestOrdersForUserWithId(Long userId) {
        response = given()
            .queryParam("userId", userId)
            .when()
            .get("/user/" + userId)
            .then()
            .extract()
            .response();
    }

    @Then("all orders should belong to user with id {long}")
    public void allOrdersShouldBelongToUserWithId(Long expectedUserId) {
        List<Map<String, Object>> orders = response.jsonPath().getList("$");
        for (Map<String, Object> order : orders) {
            Long userId = ((Number) order.get("userId")).longValue();
            assertEquals(expectedUserId, userId);
        }
    }

    @Given("an order exists with id {long} and status {string}")
    public void anOrderExistsWithIdAndStatus(Long orderId, String status) {
        Order order = new Order();
        order.setUserId(1L);
        order.setUsername("test_user");
        order.setTotalAmount(new BigDecimal("999.99"));
        order.setStatus(OrderStatus.valueOf(status));
        Order savedOrder = orderRepository.save(order);
        this.currentOrderId = savedOrder.getId();
    }

    @When("I update the order status to {string}")
    public void iUpdateTheOrderStatusTo(String newStatus) {
        response = given()
            .contentType(ContentType.JSON)
            .body("{\"status\": \"" + newStatus + "\"}")
            .when()
            .patch("/" + currentOrderId + "/status?status=" + newStatus)
            .then()
            .extract()
            .response();
    }

    @When("I cancel the order")
    public void iCancelTheOrder() {
        try {
            response = given()
                .when()
                .delete("/" + currentOrderId)
                .then()
                .extract()
                .response();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the cancellation should fail")
    public void theCancellationShouldFail() {
        assertTrue(response.statusCode() >= 400 && response.statusCode() < 600);
    }
}
