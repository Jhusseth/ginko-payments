package com.ginko.payments.infrastructure.adapter.input.rest;

import com.ginko.payments.domain.exception.DuplicateResourceException;
import com.ginko.payments.domain.exception.ResourceNotFoundException;
import com.ginko.payments.domain.model.BusinessException;
import com.ginko.payments.domain.model.ErrorCode;
import com.ginko.payments.infrastructure.config.EcsLogger;
import com.ginko.payments.infrastructure.dto.response.StandardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GlobalErrorWebExceptionHandlerTest {

    @Mock
    private EcsLogger ecsLogger;

    private GlobalErrorWebExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalErrorWebExceptionHandler(ecsLogger);
    }

    private ServerWebExchange createExchange() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put("trace.id", "test-trace-123");
        return exchange;
    }

    @Test
    void handleNotFound_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException(
                ErrorCode.PROVIDER_NOT_FOUND, "Provider", "id", 99L);
        ResponseEntity<StandardResponse<StandardResponse.ErrorData>> response = handler.handleNotFound(ex, createExchange()).block();
        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getMeta().getStatusCode());
        assertEquals("PROVIDER_NOT_FOUND", response.getBody().getData().code());
    }

    @Test
    void handleConflict_Returns409() {
        DuplicateResourceException ex = new DuplicateResourceException(
                ErrorCode.NIT_DUPLICATE, "Provider", "nit", "NIT-001");
        ResponseEntity<StandardResponse<StandardResponse.ErrorData>> response = handler.handleConflict(ex, createExchange()).block();
        assertNotNull(response);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("NIT_DUPLICATE", response.getBody().getData().code());
    }

    @Test
    void handleBusiness_ReturnsCorrespondingStatus() {
        BusinessException ex = new BusinessException(ErrorCode.INACTIVE_PROVIDER);
        ResponseEntity<StandardResponse<StandardResponse.ErrorData>> response = handler.handleBusiness(ex, createExchange()).block();
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("INACTIVE_PROVIDER", response.getBody().getData().code());
    }

    @Test
    void handleGeneral_Returns500() {
        ResponseEntity<StandardResponse<StandardResponse.ErrorData>> response = handler.handleGeneral(
                new RuntimeException("Unexpected"), createExchange()).block();
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("INTERNAL_ERROR", response.getBody().getData().code());
    }
}
