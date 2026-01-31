package com.microservices.common.logging.config;

import com.microservices.common.logging.filter.MDCFilter;
import com.microservices.common.logging.filter.RequestResponseLoggingFilter;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for structured logging.
 * Activates when common-lib.logging.enabled=true (default).
 */
@Configuration
@ConditionalOnProperty(prefix = "common-lib.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {
    
    @Bean
    @ConditionalOnClass(Tracer.class)
    public MDCFilter mdcFilter(Tracer tracer, 
                              @Value("${spring.application.name:unknown-service}") String serviceName) {
        return new MDCFilter(tracer, serviceName);
    }
    
    @Bean
    public RequestResponseLoggingFilter requestResponseLoggingFilter(LoggingProperties properties) {
        return new RequestResponseLoggingFilter(properties);
    }
}
