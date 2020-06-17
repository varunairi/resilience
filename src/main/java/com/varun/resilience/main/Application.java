package com.varun.resilience.main;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.Duration;

@SpringBootApplication
@ComponentScan(basePackages = "com.varun.resilience")
@EnableAutoConfiguration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean(name="slowCallCktBreakder")
    public CircuitBreakerRegistry getCktBreakerRegistry(){

        CircuitBreakerConfig config = CircuitBreakerConfig.custom().slowCallDurationThreshold(Duration.ofSeconds(5))
                //  .failureRateThreshold(10)
                .slowCallRateThreshold(30)
                .minimumNumberOfCalls(1)
                .slidingWindowSize(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                //        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        return registry;
    }


    @Bean(name="slowCallCktBreakderFailed")
    CircuitBreaker getCircuitBreakerFailure(){
        CircuitBreakerConfig config = CircuitBreakerConfig.custom().slowCallDurationThreshold(Duration.ofSeconds(5))
                .failureRateThreshold(10)
                .minimumNumberOfCalls(1)
                .slidingWindowSize(50)
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("name1");
        return circuitBreaker;
    }
}
