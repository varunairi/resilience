package com.varun.resilience.beanfarm;

import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TimeoutGenerator {

    @Bean
    public TimeLimiterRegistry getRegistry(){
        return TimeLimiterRegistry.of(TimeLimiterConfig.custom().
                cancelRunningFuture(true)
                .timeoutDuration(Duration.ofSeconds(5))
                .build());
    }
}
