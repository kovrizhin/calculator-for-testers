package io.paradym.testercalculator.api.dto;

import io.paradym.testercalculator.priority.Priority;
import jakarta.validation.constraints.NotBlank;

public record SubmitOperationRequest(
        @NotBlank(message = "equation must not be blank") String equation,
        Priority priority
) {

    public Priority priorityOrDefault() {
        return priority == null ? Priority.LOW : priority;
    }
}
