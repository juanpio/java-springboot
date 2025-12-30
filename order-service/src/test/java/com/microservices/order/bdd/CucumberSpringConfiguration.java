package com.microservices.order.bdd;

import com.microservices.order.config.TestFeignConfig;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.microservices.order.repository.OrderRepository;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestFeignConfig.class)
public class CucumberSpringConfiguration {
    
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

