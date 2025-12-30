package com.microservices.order.bdd;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import com.microservices.order.repository.OrderRepository;

public class TestHooks {

    @Autowired
    private OrderRepository orderRepository;

    @Before
    public void setUp() {
        // Clean up database before each scenario
        if (orderRepository != null) {
            orderRepository.deleteAll();
        }
    }
}
