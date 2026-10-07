package io.paradym.testercalculator.priority;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PriorityDelays {

    public Duration delayFor(Priority priority) {
        return switch (priority) {
            case HIGH -> Duration.ofSeconds(1);
            case MEDIUM -> Duration.ofSeconds(15);
            case LOW -> Duration.ofSeconds(5);
        };
    }
}
