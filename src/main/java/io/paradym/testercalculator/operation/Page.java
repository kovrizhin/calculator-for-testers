package io.paradym.testercalculator.operation;

import java.util.List;

public record Page<T>(List<T> items, int page, int size, long total) {
}
