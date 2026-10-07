package io.paradym.testercalculator.operation;

import java.util.UUID;

public class OperationNotFoundException extends RuntimeException {

    public OperationNotFoundException(UUID id) {
        super("Operation " + id + " not found");
    }
}
