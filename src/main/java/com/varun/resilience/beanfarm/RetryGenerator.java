package com.varun.resilience.beanfarm;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class RetryGenerator {

    @Bean (name="SimpleReg")
    public RetryRegistry getRetryReg(){
        return RetryRegistry.of(RetryConfig.custom().maxAttempts(3)
                .waitDuration(Duration.ofSeconds(5))
                .retryExceptions(IOException.class, NumberFormatException.class)
                .build());
    }


    @Bean(name="WithExpBackoff")
    public RetryRegistry getRetryRegWithExpBackoff(){
        IntervalFunction f = IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(4));
        return RetryRegistry.of(RetryConfig.custom().maxAttempts(3)
                .waitDuration(Duration.ofSeconds(10))
                .retryOnResult(o -> ((String)o).equalsIgnoreCase("GoForRetry"))
                .intervalFunction(f)
                .retryExceptions(IOException.class, NumberFormatException.class)
                .build());
    }
}
