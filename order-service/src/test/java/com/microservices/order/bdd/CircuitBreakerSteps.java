package com.microservices.order.bdd;

import com.microservices.order.client.ProductClient;
import com.microservices.order.client.UserClient;
import com.microservices.order.dto.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class CircuitBreakerSteps {

    @LocalServerPort
    private int port;

    @MockBean
    private ProductClient productClient;

    @MockBean
    private UserClient userClient;

    private Response response;
    private Exception lastException;
    private int productServiceCallCount = 0;
    private int failureCount = 0;
    private boolean circuitBreakerActivated = false;
    private List<Response> responses = new ArrayList<>();

    @Given("the product service is available")
    public void theProductServiceIsAvailable() {
        // Mock will be set up in specific scenarios
    }

    @Given("the user service is available")
    public void theUserServiceIsAvailable() {
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        
        when(userClient.getUserById(anyLong())).thenReturn(user);
    }

    @Given("a product with id {long} and name {string} and price {double} exists")
    public void aProductWithIdAndNameAndPriceExists(Long productId, String name, double price) {
        ProductDTO product = new ProductDTO();
        product.setId(productId);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setQuantity(100);
        
        when(productClient.getProductById(productId))
            .thenAnswer(invocation -> {
                productServiceCallCount++;
                return product;
            });
    }

    @Given("a user with id {long} and username {string} exists")
    public void aUserWithIdAndUsernameExists(Long userId, String username) {
        UserDTO user = new UserDTO();
        user.setId(userId);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        
        when(userClient.getUserById(userId)).thenReturn(user);
    }

    @When("I create an order for user {long} with product {long} and quantity {int}")
    public void iCreateAnOrderForUserWithProductAndQuantity(Long userId, Long productId, int quantity) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        
        List<OrderItemRequest> items = new ArrayList<>();
        items.add(item);
        orderRequest.setItems(items);

        try {
            response = given()
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .when()
                .post()
                .then()
                .extract()
                .response();
        } catch (Exception e) {
            lastException = e;
            circuitBreakerActivated = true;
        }
    }

    @Given("the product service is unavailable")
    public void theProductServiceIsUnavailable() {
        when(productClient.getProductById(anyLong()))
            .thenThrow(new RuntimeException("Product service is down"));
    }

    @Given("the user service is unavailable")
    public void theUserServiceIsUnavailable() {
        when(userClient.getUserById(anyLong()))
            .thenThrow(new RuntimeException("User service is down"));
    }

    @Then("the product service should be called")
    public void theProductServiceShouldBeCalled() {
        verify(productClient, atLeastOnce()).getProductById(anyLong());
    }

    @Then("the circuit breaker should be activated")
    public void theCircuitBreakerShouldBeActivated() {
        assertTrue(circuitBreakerActivated || (response != null && response.statusCode() >= 500));
    }

    @Then("the order username should be {string}")
    public void theOrderUsernameShouldBe(String expectedUsername) {
        String actualUsername = response.jsonPath().getString("username");
        assertEquals(expectedUsername, actualUsername);
    }

    @Then("the user service fallback should be used")
    public void theUserServiceFallbackShouldBeUsed() {
        // Verify that despite user service being down, the order was created with fallback user
        String username = response.jsonPath().getString("username");
        assertNotNull(username);
    }

    @Given("the product service fails {int} times then succeeds")
    public void theProductServiceFailsTimesThenSucceeds(int failureTimes) {
        ProductDTO product = new ProductDTO();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setQuantity(100);

        when(productClient.getProductById(anyLong()))
            .thenAnswer(invocation -> {
                productServiceCallCount++;
                if (productServiceCallCount <= failureTimes) {
                    throw new RuntimeException("Temporary failure");
                }
                return product;
            });
    }

    @Then("the product service should be called {int} times")
    public void theProductServiceShouldBeCalledTimes(int expectedCalls) {
        assertEquals(expectedCalls, productServiceCallCount,
            "Expected " + expectedCalls + " calls but got " + productServiceCallCount);
    }

    @Then("the retry mechanism should have been triggered")
    public void theRetryMechanismShouldHaveBeenTriggered() {
        assertTrue(productServiceCallCount > 1,
            "Retry mechanism should have caused multiple calls");
    }

    @Given("the product service is consistently failing")
    public void theProductServiceIsConsistentlyFailing() {
        when(productClient.getProductById(anyLong()))
            .thenAnswer(invocation -> {
                productServiceCallCount++;
                throw new RuntimeException("Service consistently down");
            });
    }

    @When("I attempt to create {int} orders sequentially")
    public void iAttemptToCreateOrdersSequentially(int orderCount) {
        for (int i = 0; i < orderCount; i++) {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setUserId(1L);
            
            OrderItemRequest item = new OrderItemRequest();
            item.setProductId(1L);
            item.setQuantity(1);
            
            List<OrderItemRequest> items = new ArrayList<>();
            items.add(item);
            orderRequest.setItems(items);

            try {
                Response resp = given()
                    .contentType(ContentType.JSON)
                    .body(orderRequest)
                    .when()
                    .post()
                    .then()
                    .extract()
                    .response();
                responses.add(resp);
            } catch (Exception e) {
                // Circuit breaker might throw exception
            }
            
            // Small delay between requests
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Then("the first few orders should fail with service errors")
    public void theFirstFewOrdersShouldFailWithServiceErrors() {
        assertTrue(responses.size() > 0, "Should have some responses");
        // First few should be actual service errors (500)
        long serviceErrors = responses.stream()
            .limit(5)
            .filter(r -> r.statusCode() >= 500)
            .count();
        assertTrue(serviceErrors > 0, "Expected some service errors");
    }

    @Then("subsequent orders should fail immediately with circuit breaker open error")
    public void subsequentOrdersShouldFailImmediatelyWithCircuitBreakerOpenError() {
        // After circuit opens, requests should fail fast
        // This is indicated by the number of actual service calls being less than total requests
        assertTrue(productServiceCallCount < responses.size(),
            "Circuit breaker should prevent all calls from reaching the service");
    }

    @Then("the circuit breaker should be in OPEN state")
    public void theCircuitBreakerShouldBeInOPENState() {
        // Verify that subsequent calls don't reach the service (circuit is open)
        int callsBeforeTest = productServiceCallCount;
        
        // Try one more call
        try {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setUserId(1L);
            
            OrderItemRequest item = new OrderItemRequest();
            item.setProductId(1L);
            item.setQuantity(1);
            
            List<OrderItemRequest> items = new ArrayList<>();
            items.add(item);
            orderRequest.setItems(items);

            given()
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .when()
                .post();
        } catch (Exception e) {
            // Expected
        }
        
        // Service shouldn't be called if circuit is open
        assertEquals(callsBeforeTest, productServiceCallCount,
            "Circuit breaker should be open, preventing service calls");
    }
}
