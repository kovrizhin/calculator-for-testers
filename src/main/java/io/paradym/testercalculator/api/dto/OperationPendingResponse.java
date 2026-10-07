package io.paradym.testercalculator.api.dto;

import io.paradym.testercalculator.operation.OperationStatus;

import java.util.UUID;

public record OperationPendingResponse(
        UUID id,
        OperationStatus status,
        long retryAfterSeconds
) {
}
