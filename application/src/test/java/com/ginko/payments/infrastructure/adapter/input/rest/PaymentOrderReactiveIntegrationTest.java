package com.ginko.payments.infrastructure.adapter.input.rest;

import com.ginko.payments.domain.model.enums.OrderStatus;
import com.ginko.payments.domain.model.enums.ProviderStatus;
import com.ginko.payments.infrastructure.dto.request.ChangeOrderStatusRequest;
import com.ginko.payments.infrastructure.dto.request.ChangeProviderStatusRequest;
import com.ginko.payments.infrastructure.dto.request.CreatePaymentOrderRequest;
import com.ginko.payments.infrastructure.dto.request.CreateProviderRequest;
import com.ginko.payments.infrastructure.dto.request.UpdateProviderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class PaymentOrderReactiveIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        databaseClient.sql("DELETE FROM payment_orders").then().block();
        databaseClient.sql("DELETE FROM providers").then().block();
    }

    private Long createProvider(String name, String nit, String email) {
        CreateProviderRequest req = new CreateProviderRequest();
        req.setName(name);
        req.setTaxIdentificationNumber(nit);
        req.setEmail(email);

        var result = webTestClient.post().uri("/api/v1/providers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").isNotEmpty()
                .jsonPath("$.data.name").isEqualTo(name)
                .returnResult();

        String json = new String(result.getResponseBody() != null ? result.getResponseBody() : new byte[0]);
        return extractId(json);
    }

    private Long createOrder(Long providerId, BigDecimal amount, String description) {
        return createOrder(providerId, amount, description, null);
    }

    private Long createOrder(Long providerId, BigDecimal amount, String description, String idempotencyKey) {
        CreatePaymentOrderRequest req = new CreatePaymentOrderRequest();
        req.setProviderId(providerId);
        req.setAmount(amount);
        req.setDescription(description);

        var spec = webTestClient.post().uri("/api/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON);
        if (idempotencyKey != null) {
            spec = spec.header("Idempotency-Key", idempotencyKey);
        }

        var result = spec.bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").isNotEmpty()
                .returnResult();

        String json = new String(result.getResponseBody() != null ? result.getResponseBody() : new byte[0]);
        return extractId(json);
    }

    private Long extractId(String json) {
        int idIdx = json.indexOf("\"id\":");
        if (idIdx < 0) return -1L;
        int start = idIdx + 5;
        while (start < json.length() && !Character.isDigit(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        try {
            return Long.parseLong(json.substring(start, end));
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    @Test
    void completeCycle_ProviderAndOrder_SuccessfulFlow() {
        Long providerId = createProvider("Provider ABC SAS", "900123456-7", "contact@abc.com");

        webTestClient.get().uri("/api/v1/providers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.meta.totalElements").isNumber();

        webTestClient.get().uri("/api/v1/providers/{id}", providerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("Provider ABC SAS");

        UpdateProviderRequest updateReq = new UpdateProviderRequest();
        updateReq.setName("Provider ABC Updated");
        updateReq.setTaxIdentificationNumber("900123456-7");
        updateReq.setEmail("new@abc.com");

        webTestClient.put().uri("/api/v1/providers/{id}", providerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("Provider ABC Updated");

        ChangeProviderStatusRequest statusReq = new ChangeProviderStatusRequest();
        statusReq.setStatus(ProviderStatus.INACTIVE);

        webTestClient.patch().uri("/api/v1/providers/{id}/status", providerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("INACTIVE");

        statusReq.setStatus(ProviderStatus.ACTIVE);
        webTestClient.patch().uri("/api/v1/providers/{id}/status", providerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusReq)
                .exchange()
                .expectStatus().isOk();

        Long orderId = createOrder(providerId, new BigDecimal("2500000"), "Payment services");

        webTestClient.get().uri("/api/v1/payment-orders/{id}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("DRAFT")
                .jsonPath("$.data.amount").isEqualTo(2500000);
    }

    @Test
    void createProvider_WithDuplicateNIT_Returns409() {
        createProvider("Provider 1", "NIT-UNIQUE", "p1@test.com");

        CreateProviderRequest req = new CreateProviderRequest();
        req.setName("Provider 2");
        req.setTaxIdentificationNumber("NIT-UNIQUE");
        req.setEmail("p2@test.com");

        webTestClient.post().uri("/api/v1/providers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void createOrder_WithInactiveProvider_Returns400() {
        Long providerId = createProvider("Inactive Provider", "NIT-INACTIVE", "i@test.com");

        ChangeProviderStatusRequest statusReq = new ChangeProviderStatusRequest();
        statusReq.setStatus(ProviderStatus.INACTIVE);
        webTestClient.patch().uri("/api/v1/providers/{id}/status", providerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusReq)
                .exchange()
                .expectStatus().isOk();

        CreatePaymentOrderRequest orderReq = new CreatePaymentOrderRequest();
        orderReq.setProviderId(providerId);
        orderReq.setAmount(new BigDecimal("500000"));
        orderReq.setDescription("Test payment");

        webTestClient.post().uri("/api/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderReq)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createOrder_WithNonExistentProvider_Returns404() {
        CreatePaymentOrderRequest req = new CreatePaymentOrderRequest();
        req.setProviderId(9999L);
        req.setAmount(new BigDecimal("500000"));
        req.setDescription("Test payment");

        webTestClient.post().uri("/api/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void transitionStatus_CompleteFlow_Successful() {
        Long providerId = createProvider("Test Provider", "NIT-TRANS", "trans@test.com");

        Long orderId = createOrder(providerId, new BigDecimal("1000000"), "Test transitions");

        ChangeOrderStatusRequest statusOrderReq = new ChangeOrderStatusRequest();
        statusOrderReq.setStatus(OrderStatus.APPROVED);

        webTestClient.patch().uri("/api/v1/payment-orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusOrderReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("APPROVED");

        statusOrderReq.setStatus(OrderStatus.PAID);
        webTestClient.patch().uri("/api/v1/payment-orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusOrderReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("PAID");
    }

    @Test
    void idempotencyKey_PreventsDuplicates() {
        Long providerId = createProvider("Provider Idempotency", "NIT-IDEM", "idem@test.com");

        CreatePaymentOrderRequest req = new CreatePaymentOrderRequest();
        req.setProviderId(providerId);
        req.setAmount(new BigDecimal("1000000"));
        req.setDescription("Test idempotency");

        webTestClient.post().uri("/api/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "unique-key-001")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").isNotEmpty()
                .jsonPath("$.data.amount").isEqualTo(1000000);

        req.setAmount(new BigDecimal("9999999"));
        req.setDescription("Should not be created");

        webTestClient.post().uri("/api/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "unique-key-001")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.amount").isEqualTo(1000000);
    }
}
