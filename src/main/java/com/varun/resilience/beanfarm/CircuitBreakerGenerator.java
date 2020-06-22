package com.varun.resilience.beanfarm;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
@Component
public class CircuitBreakerGenerator {


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
        return CircuitBreakerRegistry.of(config);
    }


    @Bean(name="failedCallCktBreaker")
    CircuitBreakerRegistry getCircuitBreakerRegistryForFailure(){
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(30) //%age of failures before Ckt is open
                .minimumNumberOfCalls(2)  //minimum number of calls at the begining OR RESET to go let through.
                .slidingWindowSize(50)   //sliding window size to find  the stats for ckt breaker to determine the state
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                //   .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .waitDurationInOpenState(Duration.ofMinutes(2)) //how long the open state remains before moving to half open
                .build();
        return CircuitBreakerRegistry.of(config);
    }
}
