package com.ginko.payments.infrastructure.adapter.input.rest;

import com.ginko.payments.domain.model.BusinessException;
import com.ginko.payments.domain.model.ErrorCode;
import com.ginko.payments.domain.model.enums.OrderStatus;
import com.ginko.payments.domain.port.input.PaymentOrderUseCase;
import com.ginko.payments.domain.port.input.ProviderUseCase;
import com.ginko.payments.infrastructure.dto.request.ChangeOrderStatusRequest;
import com.ginko.payments.infrastructure.dto.request.CreatePaymentOrderRequest;
import com.ginko.payments.infrastructure.dto.response.PaymentOrderResponse;
import com.ginko.payments.infrastructure.dto.response.ReportResponse;
import com.ginko.payments.infrastructure.dto.response.StandardResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-orders")
@Tag(name = "Payment Orders", description = "Payment Order Management")
public class PaymentOrderController {

    private final PaymentOrderUseCase useCase;
    private final ProviderUseCase providerUseCase;

    public PaymentOrderController(PaymentOrderUseCase useCase, ProviderUseCase providerUseCase) {
        this.useCase = useCase;
        this.providerUseCase = providerUseCase;
    }

    @PostMapping
    @Operation(summary = "Create payment order",
            description = "Supports idempotency via Idempotency-Key header")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid data or inactive provider"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    public Mono<ResponseEntity<StandardResponse<PaymentOrderResponse>>> create(
            @Valid @RequestBody Mono<CreatePaymentOrderRequest> requestMono,
            @Parameter(description = "Idempotency key")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return requestMono
                .flatMap(req -> useCase.create(req.getProviderId(), req.getAmount(),
                        req.getDescription(), idempotencyKey))
                .map(o -> ResponseEntity.status(HttpStatus.CREATED).body(StandardResponse.success(PaymentOrderResponse.fromDomain(o))));
    }

    @GetMapping
    @Operation(summary = "List payment orders")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    public Mono<ResponseEntity<StandardResponse<List<PaymentOrderResponse>>>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var content = useCase.list(status, providerId, page, size)
                .map(PaymentOrderResponse::fromDomain)
                .collectList();
        Mono<Long> total = useCase.count(status, providerId);
        return Mono.zip(content, total)
                .map(tuple -> ResponseEntity.ok(StandardResponse.paged(tuple.getT1(), tuple.getT2(), page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment order by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    public Mono<ResponseEntity<StandardResponse<PaymentOrderResponse>>> getById(@PathVariable Long id) {
        return useCase.getById(id)
                .map(o -> ResponseEntity.ok(StandardResponse.success(PaymentOrderResponse.fromDomain(o))));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition status",
            description = "DRAFT->APPROVED, DRAFT->REJECTED, APPROVED->PAID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid transition"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Concurrency conflict")
    })
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    public Mono<ResponseEntity<StandardResponse<PaymentOrderResponse>>> transitionStatus(
            @PathVariable Long id,
            @Valid @RequestBody Mono<ChangeOrderStatusRequest> requestMono) {
        return requestMono
                .flatMap(req -> useCase.transitionStatus(id, req.getStatus()))
                .map(o -> ResponseEntity.ok(StandardResponse.success(PaymentOrderResponse.fromDomain(o))));
    }

    @GetMapping("/report")
    @Operation(summary = "Total paid by provider in range")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    public Mono<ResponseEntity<StandardResponse<ReportResponse>>> paidReport(
            @RequestParam Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return useCase.reportTotalPaid(providerId, startDate, endDate)
                .zipWith(providerUseCase.getById(providerId).map(com.ginko.payments.domain.model.Provider::getName))
                .map(tuple -> ResponseEntity.ok(StandardResponse.success(new ReportResponse(
                        providerId, tuple.getT2(), tuple.getT1(), startDate.toString(), endDate.toString()))));
    }

    @GetMapping("/about-to-expire")
    @Operation(summary = "Orders about to expire",
            description = "APPROVED orders more than 30 days without payment")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    public Mono<ResponseEntity<StandardResponse<List<PaymentOrderResponse>>>> ordersAboutToExpire(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var content = useCase.ordersAboutToExpire(page, size)
                .map(PaymentOrderResponse::fromDomain)
                .collectList();
        Mono<Long> total = useCase.countAboutToExpire();
        return Mono.zip(content, total)
                .map(tuple -> ResponseEntity.ok(StandardResponse.paged(tuple.getT1(), tuple.getT2(), page, size)));
    }

    private <T> Mono<T> fallback(Throwable t) {
        return Mono.error(new BusinessException(
                ErrorCode.SERVICE_UNAVAILABLE, t.getMessage()));
    }
}
