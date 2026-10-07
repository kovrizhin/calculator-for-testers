package io.paradym.testercalculator.operation;

import io.paradym.testercalculator.parser.Expr;
import io.paradym.testercalculator.priority.Priority;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Operation {

    private final UUID id;
    private final String equation;
    private final Expr expression;
    private final Priority priority;
    private final String owner;
    private final Instant createdAt;
    private final Instant estimatedCompletionAt;
    private volatile OperationStatus status;
    private volatile Double result;
    private volatile OperationError error;
    private volatile Instant completedAt;
}
