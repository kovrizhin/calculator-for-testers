package io.paradym.testercalculator.api;

import io.paradym.testercalculator.api.dto.OperationAcceptedResponse;
import io.paradym.testercalculator.api.dto.OperationPendingResponse;
import io.paradym.testercalculator.api.dto.OperationResponse;
import io.paradym.testercalculator.api.dto.PageResponse;
import io.paradym.testercalculator.api.dto.SubmitOperationRequest;
import io.paradym.testercalculator.operation.Operation;
import io.paradym.testercalculator.operation.OperationService;
import io.paradym.testercalculator.operation.OperationStatus;
import io.paradym.testercalculator.operation.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationController {

    private final OperationService operationService;

    @PostMapping
    public OperationAcceptedResponse submit(@AuthenticationPrincipal UserDetails user,
                                            @Valid @RequestBody SubmitOperationRequest request) {
        Operation operation = operationService.submit(user.getUsername(), request.equation(), request.priorityOrDefault());
        return new OperationAcceptedResponse(
                operation.getId(),
                operation.getStatus(),
                operation.getPriority(),
                operation.getEstimatedCompletionAt()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@AuthenticationPrincipal UserDetails user, @PathVariable UUID id) {
        Operation operation = operationService.get(user.getUsername(), id);
        if (operation.getStatus() == OperationStatus.DONE || operation.getStatus() == OperationStatus.FAILED) {
            return ResponseEntity.ok(OperationResponse.from(operation));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new OperationPendingResponse(operation.getId(), operation.getStatus(),
                        operationService.secondsUntilCompletion(operation)));
    }

    @GetMapping
    public PageResponse<OperationResponse> list(@AuthenticationPrincipal UserDetails user,
                                                @RequestParam(defaultValue = "0") @Min(0) int page,
                                                @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Page<Operation> result = operationService.list(user.getUsername(), page, size);
        return new PageResponse<>(
                result.items().stream().map(OperationResponse::from).toList(),
                result.page(),
                result.size(),
                result.total()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        operationService.delete(id);
    }
}
