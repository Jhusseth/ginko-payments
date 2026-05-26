package com.ginko.payments.infrastructure.adapter.input.rest;

import com.ginko.payments.domain.exception.DuplicateResourceException;
import com.ginko.payments.domain.exception.ResourceNotFoundException;
import com.ginko.payments.domain.model.BusinessException;
import com.ginko.payments.domain.model.ErrorCode;
import com.ginko.payments.infrastructure.config.EcsLogger;
import com.ginko.payments.infrastructure.config.TraceIdFilter;
import com.ginko.payments.infrastructure.dto.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalErrorWebExceptionHandler {

    private final EcsLogger ecsLogger;

    public GlobalErrorWebExceptionHandler(EcsLogger ecsLogger) {
        this.ecsLogger = ecsLogger;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<StandardResponse<StandardResponse.ErrorData>>> handleNotFound(
            ResourceNotFoundException ex, ServerWebExchange exchange) {
        ErrorCode ec = ex.getErrorCode() != null ? ex.getErrorCode() : ErrorCode.PROVIDER_NOT_FOUND;
        String traceId = TraceIdFilter.extractTraceId(exchange);
        ecsLogger.logError(ec, ex.getMessage(), ex, traceId);
        return Mono.just(ResponseEntity.status(ec.getHttpStatus())
                .body(StandardResponse.error(ec.getHttpStatus(), ec.getBusinessCode(), ex.getMessage())));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public Mono<ResponseEntity<StandardResponse<StandardResponse.ErrorData>>> handleConflict(
            DuplicateResourceException ex, ServerWebExchange exchange) {
        ErrorCode ec = ex.getErrorCode() != null ? ex.getErrorCode() : ErrorCode.NIT_DUPLICATE;
        String traceId = TraceIdFilter.extractTraceId(exchange);
        ecsLogger.logError(ec, ex.getMessage(), ex, traceId);
        return Mono.just(ResponseEntity.status(ec.getHttpStatus())
                .body(StandardResponse.error(ec.getHttpStatus(), ec.getBusinessCode(), ex.getMessage())));
    }

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<StandardResponse<StandardResponse.ErrorData>>> handleBusiness(
            BusinessException ex, ServerWebExchange exchange) {
        ErrorCode ec = ex.getErrorCode() != null ? ex.getErrorCode() : ErrorCode.INTERNAL_ERROR;
        String traceId = TraceIdFilter.extractTraceId(exchange);
        ecsLogger.logError(ec, ex.getMessage(), ex, traceId);
        return Mono.just(ResponseEntity.status(ec.getHttpStatus())
                .body(StandardResponse.error(ec.getHttpStatus(), ec.getBusinessCode(), ex.getMessage())));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<StandardResponse<StandardResponse.ErrorData>>> handleValidation(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        ErrorCode ec = ErrorCode.FIELD_VALIDATION;
        String traceId = TraceIdFilter.extractTraceId(exchange);
        String details = ex.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(" | "));
        ecsLogger.logError(ec, "Validation error: " + details, ex, traceId);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.error(400, ec.getBusinessCode(),
                        "Validation error: " + details)));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<StandardResponse<StandardResponse.ErrorData>>> handleResource(
            NoResourceFoundException ex, ServerWebExchange exchange) {
        ErrorCode ec = ErrorCode.RESOURCE_NOT_FOUND;
        String traceId = TraceIdFilter.extractTraceId(exchange);
        ecsLogger.logError(ec, ex.getMessage(), ex, traceId);
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.error(404, ec.getBusinessCode(), ex.getMessage())));
    }

    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<StandardResponse<StandardResponse.ErrorData>>> handleGeneral(
            Throwable ex, ServerWebExchange exchange) {
        ErrorCode ec = ErrorCode.INTERNAL_ERROR;
        String traceId = TraceIdFilter.extractTraceId(exchange);
        ecsLogger.logUnexpected(ex, traceId);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error(500, ec.getBusinessCode(), ec.getMessage())));
    }
}
