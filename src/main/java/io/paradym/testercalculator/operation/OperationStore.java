package io.paradym.testercalculator.operation;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OperationStore {

    private final Map<UUID, Operation> operations = new ConcurrentHashMap<>();

    public void save(Operation operation) {
        operations.put(operation.getId(), operation);
    }

    public Optional<Operation> find(UUID id) {
        return Optional.ofNullable(operations.get(id));
    }

    public List<Operation> findByOwner(String owner) {
        return operations.values().stream()
                .filter(operation -> operation.getOwner().equals(owner))
                .sorted(Comparator.comparing(Operation::getCreatedAt).reversed())
                .toList();
    }

    public boolean delete(UUID id) {
        return operations.remove(id) != null;
    }
}
