package io.paradym.testercalculator.parser;

import lombok.Getter;

@Getter
public class EquationException extends RuntimeException {

    private final int position;

    public EquationException(String message, int position) {
        super(message);
        this.position = position;
    }
}
