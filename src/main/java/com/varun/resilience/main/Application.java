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

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slowCallDurationThreshold(Duration.ofSeconds(5)) //max wait time is 5 seconds
                //  .failureRateThreshold(10)
                .slowCallRateThreshold(30) //if 30% or more calls are slower than  trigger Ckt Breaker
                .minimumNumberOfCalls(1) //start ckt breaker to monitor after 1st call.
                .slidingWindowSize(5)  //use the stats from 5 recent calls to calc stats to see threshold breached or not
                .permittedNumberOfCallsInHalfOpenState(3) //when circuit is half open. how many calls to allow to gauge success
                //        .automaticTransitionFromOpenToHalfOpenEnabled(true) //should open turn into half open after set time.
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        return registry;
    }


    @Bean(name="failedCallCktBreaker")
    CircuitBreakerRegistry getCircuitBreakerRegistryForFailure(){
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(30)
                .minimumNumberOfCalls(2)
                .slidingWindowSize(50)
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
             //   .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
        .waitDurationInOpenState(Duration.ofMinutes(2)) //how long the open state remains before moving to half open
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        return registry;
    }
}
