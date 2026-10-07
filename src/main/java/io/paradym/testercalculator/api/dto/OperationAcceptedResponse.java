package io.paradym.testercalculator.api.dto;

import io.paradym.testercalculator.operation.OperationStatus;
import io.paradym.testercalculator.priority.Priority;

import java.time.Instant;
import java.util.UUID;

public record OperationAcceptedResponse(
        UUID id,
        OperationStatus status,
        Priority priority,
        Instant estimatedCompletionAt
) {
}
