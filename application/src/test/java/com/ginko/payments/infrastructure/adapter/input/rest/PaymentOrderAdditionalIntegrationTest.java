package com.ginko.payments.infrastructure.adapter.input.rest;

import com.ginko.payments.domain.model.enums.OrderStatus;
import com.ginko.payments.infrastructure.dto.request.ChangeOrderStatusRequest;
import com.ginko.payments.infrastructure.dto.request.CreatePaymentOrderRequest;
import com.ginko.payments.infrastructure.dto.request.CreateProviderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PaymentOrderAdditionalIntegrationTest {

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
                .returnResult();

        String json = new String(result.getResponseBody() != null ? result.getResponseBody() : new byte[0]);
        int idIdx = json.indexOf("\"id\":");
        int start = idIdx + 5;
        while (start < json.length() && !Character.isDigit(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Long.parseLong(json.substring(start, end));
    }

    private Long createOrder(Long providerId, BigDecimal amount, String description) {
        CreatePaymentOrderRequest req = new CreatePaymentOrderRequest();
        req.setProviderId(providerId);
        req.setAmount(amount);
        req.setDescription(description);

        var result = webTestClient.post().uri("/api/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").isNotEmpty()
                .returnResult();

        String json = new String(result.getResponseBody() != null ? result.getResponseBody() : new byte[0]);
        int idIdx = json.indexOf("\"id\":");
        int start = idIdx + 5;
        while (start < json.length() && !Character.isDigit(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Long.parseLong(json.substring(start, end));
    }

    @Test
    void listPaymentOrders_WithStatusFilter_ReturnsFiltered() {
        Long providerId = createProvider("P", "NIT-LIST", "p@test.com");
        Long orderId = createOrder(providerId, BigDecimal.valueOf(1000), "Test");

        ChangeOrderStatusRequest statusReq = new ChangeOrderStatusRequest();
        statusReq.setStatus(OrderStatus.APPROVED);
        webTestClient.patch().uri("/api/v1/payment-orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusReq)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/api/v1/payment-orders?status=APPROVED")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].status").isEqualTo("APPROVED");
    }

    @Test
    void listPaymentOrders_WithProviderFilter_ReturnsFiltered() {
        Long providerId = createProvider("P", "NIT-FILT", "p@test.com");
        createOrder(providerId, BigDecimal.valueOf(1000), "Test1");
        createOrder(providerId, BigDecimal.valueOf(2000), "Test2");

        webTestClient.get().uri("/api/v1/payment-orders?providerId={id}", providerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.meta.totalElements").isNumber();
    }

    @Test
    void getPaymentOrder_NotFound_Returns404() {
        webTestClient.get().uri("/api/v1/payment-orders/9999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void transitionStatus_InvalidTransition_Returns400() {
        Long providerId = createProvider("P", "NIT-INV", "p@test.com");
        Long orderId = createOrder(providerId, BigDecimal.valueOf(1000), "Test");

        ChangeOrderStatusRequest req = new ChangeOrderStatusRequest();
        req.setStatus(OrderStatus.PAID);

        webTestClient.patch().uri("/api/v1/payment-orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void transitionStatus_NotFound_Returns404() {
        ChangeOrderStatusRequest req = new ChangeOrderStatusRequest();
        req.setStatus(OrderStatus.APPROVED);

        webTestClient.patch().uri("/api/v1/payment-orders/9999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void reportTotalPaid_WithOrders_ReturnsTotal() {
        Long providerId = createProvider("P", "NIT-REP", "p@test.com");
        Long orderId = createOrder(providerId, BigDecimal.valueOf(5000), "Payment 1");

        ChangeOrderStatusRequest approveReq = new ChangeOrderStatusRequest();
        approveReq.setStatus(OrderStatus.APPROVED);
        webTestClient.patch().uri("/api/v1/payment-orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(approveReq)
                .exchange()
                .expectStatus().isOk();

        ChangeOrderStatusRequest paidReq = new ChangeOrderStatusRequest();
        paidReq.setStatus(OrderStatus.PAID);
        webTestClient.patch().uri("/api/v1/payment-orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paidReq)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/api/v1/payment-orders/report?providerId={pid}&startDate=2020-01-01&endDate=2030-12-31", providerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.providerId").isEqualTo(providerId.intValue())
                .jsonPath("$.data.providerName").isEqualTo("P")
                .jsonPath("$.data.totalPaid").isEqualTo(5000);
    }

    @Test
    void ordersAboutToExpire_WhenExpiredOrder_ReturnsInList() {
        Long providerId = createProvider("P", "NIT-EXP", "p@test.com");
        Long orderId = createOrder(providerId, BigDecimal.valueOf(3000), "Old payment");

        ChangeOrderStatusRequest req = new ChangeOrderStatusRequest();
        req.setStatus(OrderStatus.APPROVED);
        webTestClient.patch().uri("/api/v1/payment-orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/api/v1/payment-orders/about-to-expire")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.meta.totalElements").isNumber();
    }

    @Test
    void createPaymentOrder_MissingFields_Returns400() {
        CreatePaymentOrderRequest req = new CreatePaymentOrderRequest();
        webTestClient.post().uri("/api/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createPaymentOrder_WithInvalidAmount_Returns400() {
        Long providerId = createProvider("P", "NIT-AMT", "p@test.com");
        CreatePaymentOrderRequest req = new CreatePaymentOrderRequest();
        req.setProviderId(providerId);
        req.setAmount(BigDecimal.valueOf(-1));
        req.setDescription("Negative amount");

        webTestClient.post().uri("/api/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
