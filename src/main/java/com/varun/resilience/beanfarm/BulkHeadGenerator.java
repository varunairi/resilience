package com.varun.resilience.beanfarm;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
@Component
public class BulkHeadGenerator {
    @Bean
    public BulkheadRegistry generateSemaphorBasedBHRegistry(){
        BulkheadConfig config  = BulkheadConfig.custom()
                .maxConcurrentCalls(10) //how many threads should be running the API/Method at a time
                .maxWaitDuration(Duration.ofSeconds(10)) // how long to wait before taking a different action
                .build();
        return BulkheadRegistry.of(config);
    }

    @Bean
    public ThreadPoolBulkheadRegistry generateTPBasedBHRegistry(){
        ThreadPoolBulkheadConfig config  = ThreadPoolBulkheadConfig.custom()
                .coreThreadPoolSize(5)
                .maxThreadPoolSize(7)
                .queueCapacity(10)
      //          .keepAliveDuration(Duration.ofSeconds(10))
                .build();
        return ThreadPoolBulkheadRegistry.of(config);
    }
}
