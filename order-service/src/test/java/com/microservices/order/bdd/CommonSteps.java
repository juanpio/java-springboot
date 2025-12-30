package com.microservices.order.bdd;

import io.cucumber.java.en.Given;
import io.restassured.RestAssured;
import org.springframework.boot.test.web.server.LocalServerPort;

public class CommonSteps {

    @LocalServerPort
    private int port;

    @Given("the order service is running")
    public void theOrderServiceIsRunning() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/orders";
    }
}
