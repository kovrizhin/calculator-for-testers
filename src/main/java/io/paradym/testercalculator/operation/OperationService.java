package io.paradym.testercalculator.operation;

import io.paradym.testercalculator.parser.EquationException;
import io.paradym.testercalculator.parser.Expr;
import io.paradym.testercalculator.parser.Parser;
import io.paradym.testercalculator.priority.Priority;
import io.paradym.testercalculator.priority.PriorityDelays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationService {

    public static final int MAX_EQUATION_LENGTH = 200;

    private final OperationStore store;
    private final PriorityDelays priorityDelays;
    private final ScheduledExecutorService scheduler;

    public Operation submit(String owner, String equation, Priority priority) {
        if (equation.length() > MAX_EQUATION_LENGTH + 1) {
            throw new EquationException("Equation exceeds " + MAX_EQUATION_LENGTH + " characters", MAX_EQUATION_LENGTH);
        }
        Expr expression = new Parser().parse(equation);
        Duration delay = priorityDelays.delayFor(priority);
        Instant now = Instant.now();
        Operation operation = Operation.builder()
                .id(UUID.randomUUID())
                .equation(equation)
                .expression(expression)
                .priority(priority)
                .owner(owner)
                .createdAt(now)
                .estimatedCompletionAt(now.plus(delay))
                .status(OperationStatus.PENDING)
                .build();
        store.save(operation);
        scheduler.schedule(() -> markInProgress(operation), delay.toMillis() / 2, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> complete(operation), delay.toMillis(), TimeUnit.MILLISECONDS);
        log.info("Submitted operation {} for {} with priority {}", operation.getId(), owner, priority);
        return operation;
    }

    public Operation get(String owner, UUID id) {
        return store.find(id).orElseThrow(() -> new OperationNotFoundException(id));
    }

    public Page<Operation> list(String owner, int page, int size) {
        List<Operation> all = store.findByOwner(owner);
        int from = Math.min(Math.max(page - 1, 0) * size, all.size());
        int to = Math.min(from + size, all.size());
        return new Page<>(all.subList(from, to), page, size, all.size());
    }

    public void delete(UUID id) {
        if (!store.delete(id)) {
            throw new OperationNotFoundException(id);
        }
    }

    public long secondsUntilCompletion(Operation operation) {
        long seconds = Duration.between(Instant.now(), operation.getEstimatedCompletionAt()).toSeconds();
        return Math.max(seconds, 1);
    }

    private void markInProgress(Operation operation) {
        if (operation.getStatus() == OperationStatus.PENDING) {
            operation.setStatus(OperationStatus.IN_PROGRESS);
        }
    }

    private void complete(Operation operation) {
        try {
            double value = operation.getExpression().evaluate();
            operation.setResult(value);
            operation.setStatus(OperationStatus.DONE);
        } catch (RuntimeException e) {
            operation.setError(new OperationError("EVALUATION_ERROR", e.getMessage()));
            operation.setStatus(OperationStatus.FAILED);
        }
        operation.setCompletedAt(Instant.now());
    }
}
