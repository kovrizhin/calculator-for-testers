package io.paradym.testercalculator.api.dto;

import io.paradym.testercalculator.operation.Operation;
import io.paradym.testercalculator.operation.OperationError;
import io.paradym.testercalculator.operation.OperationStatus;
import io.paradym.testercalculator.priority.Priority;

import java.time.Instant;
import java.util.UUID;

public record OperationResponse(
        UUID id,
        String equation,
        Priority priority,
        OperationStatus status,
        Double result,
        OperationError error,
        Instant createdAt,
        Instant completedAt
) {

    public static OperationResponse from(Operation operation) {
        return new OperationResponse(
                operation.getId(),
                operation.getEquation(),
                operation.getPriority(),
                operation.getStatus(),
                operation.getResult(),
                operation.getError(),
                operation.getCreatedAt(),
                operation.getCompletedAt()
        );
    }
}
