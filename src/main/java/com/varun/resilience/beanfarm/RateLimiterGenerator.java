package com.varun.resilience.beanfarm;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiterGenerator {
    @Bean
    public RateLimiterRegistry getRateLimiterReg(){
        RateLimiterConfig config  = RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(90)) //how long a thread should wait before timing out
                .limitForPeriod(20) //how many requests allowed in a given time period  limitRefreshPeriod
                .limitRefreshPeriod(Duration.ofSeconds(5)).build();
        return RateLimiterRegistry.of(config);
    }

}
