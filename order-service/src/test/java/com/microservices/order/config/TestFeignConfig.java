package com.microservices.order.config;

import com.microservices.order.client.ProductClient;
import com.microservices.order.client.UserClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class TestFeignConfig {

    @MockBean
    private ProductClient productClient;

    @MockBean
    private UserClient userClient;
}
