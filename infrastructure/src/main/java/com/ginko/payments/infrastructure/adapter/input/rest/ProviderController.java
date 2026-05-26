package com.ginko.payments.infrastructure.adapter.input.rest;

import com.ginko.payments.domain.model.BusinessException;
import com.ginko.payments.domain.model.ErrorCode;
import com.ginko.payments.domain.model.enums.ProviderStatus;
import com.ginko.payments.domain.port.input.ProviderUseCase;
import com.ginko.payments.infrastructure.dto.request.ChangeProviderStatusRequest;
import com.ginko.payments.infrastructure.dto.request.CreateProviderRequest;
import com.ginko.payments.infrastructure.dto.request.UpdateProviderRequest;
import com.ginko.payments.infrastructure.dto.response.ProviderResponse;
import com.ginko.payments.infrastructure.dto.response.StandardResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers")
@Tag(name = "Providers", description = "Provider Management")
public class ProviderController {

    private final ProviderUseCase useCase;

    public ProviderController(ProviderUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(summary = "Create provider")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "409", description = "Duplicate NIT")
    })
    @CircuitBreaker(name = "providerService", fallbackMethod = "fallback")
    @Retry(name = "providerService")
    public Mono<ResponseEntity<StandardResponse<ProviderResponse>>> create(@Valid @RequestBody Mono<CreateProviderRequest> requestMono) {
        return requestMono
                .flatMap(req -> useCase.create(req.getName(), req.getTaxIdentificationNumber(), req.getEmail()))
                .map(p -> ResponseEntity.status(HttpStatus.CREATED).body(StandardResponse.success(ProviderResponse.fromDomain(p))));
    }

    @GetMapping
    @Operation(summary = "List providers")
    @CircuitBreaker(name = "providerService", fallbackMethod = "fallback")
    @Retry(name = "providerService")
    public Mono<ResponseEntity<StandardResponse<List<ProviderResponse>>>> list(
            @RequestParam(required = false) ProviderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Mono<Long> total = useCase.count(status);
        var content = useCase.list(status, page, size)
                .map(ProviderResponse::fromDomain)
                .collectList();
        return Mono.zip(content, total)
                .map(tuple -> ResponseEntity.ok(StandardResponse.paged(tuple.getT1(), tuple.getT2(), page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get provider by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @CircuitBreaker(name = "providerService", fallbackMethod = "fallback")
    @Retry(name = "providerService")
    public Mono<ResponseEntity<StandardResponse<ProviderResponse>>> getById(@PathVariable Long id) {
        return useCase.getById(id)
                .map(p -> ResponseEntity.ok(StandardResponse.success(ProviderResponse.fromDomain(p))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update provider")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate NIT")
    })
    @CircuitBreaker(name = "providerService", fallbackMethod = "fallback")
    @Retry(name = "providerService")
    public Mono<ResponseEntity<StandardResponse<ProviderResponse>>> update(
            @PathVariable Long id,
            @Valid @RequestBody Mono<UpdateProviderRequest> requestMono) {
        return requestMono
                .flatMap(req -> useCase.update(id, req.getName(),
                        req.getTaxIdentificationNumber(), req.getEmail()))
                .map(p -> ResponseEntity.ok(StandardResponse.success(ProviderResponse.fromDomain(p))));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change provider status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @CircuitBreaker(name = "providerService", fallbackMethod = "fallback")
    @Retry(name = "providerService")
    public Mono<ResponseEntity<StandardResponse<ProviderResponse>>> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody Mono<ChangeProviderStatusRequest> requestMono) {
        return requestMono
                .flatMap(req -> useCase.changeStatus(id, req.getStatus()))
                .map(p -> ResponseEntity.ok(StandardResponse.success(ProviderResponse.fromDomain(p))));
    }

    private <T> Mono<T> fallback(Throwable t) {
        return Mono.error(new BusinessException(
                ErrorCode.SERVICE_UNAVAILABLE, t.getMessage()));
    }
}
