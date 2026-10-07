package io.paradym.testercalculator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class AsyncConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService operationScheduler() {
        return Executors.newScheduledThreadPool(4, Thread.ofVirtual().factory());
    }
}
