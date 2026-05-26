package com.ginko.payments.infrastructure.adapter.input.rest;

import com.ginko.payments.domain.model.enums.ProviderStatus;
import com.ginko.payments.infrastructure.dto.request.ChangeProviderStatusRequest;
import com.ginko.payments.infrastructure.dto.request.CreateProviderRequest;
import com.ginko.payments.infrastructure.dto.request.UpdateProviderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProviderControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        databaseClient.sql("DELETE FROM payment_orders").then().block();
        databaseClient.sql("DELETE FROM providers").then().block();
    }

    private UUID createProvider(String name, String nit, String email) {
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
        int idIdx = json.indexOf("\"id\":\"");
        int start = idIdx + 6;
        int end = json.indexOf("\"", start);
        return UUID.fromString(json.substring(start, end));
    }

    @Test
    void listProviders_WithoutFilter_ReturnsAll() {
        createProvider("P1", "NIT-1", "p1@test.com");
        createProvider("P2", "NIT-2", "p2@test.com");

        webTestClient.get().uri("/api/v1/providers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.meta.totalElements").isNumber()
                .jsonPath("$.data.length()").isNumber();
    }

    @Test
    void listProviders_WithStatusFilter_ReturnsFiltered() {
        UUID id = createProvider("P1", "NIT-FILTER", "p1@test.com");
        ChangeProviderStatusRequest statusReq = new ChangeProviderStatusRequest();
        statusReq.setStatus(ProviderStatus.INACTIVE);
        webTestClient.patch().uri("/api/v1/providers/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusReq)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri("/api/v1/providers?status=INACTIVE")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].status").isEqualTo("INACTIVE");
    }

    @Test
    void getProvider_NotFound_Returns404() {
        webTestClient.get().uri("/api/v1/providers/00000000-0000-0000-0000-000000000000")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.data.code").isEqualTo("PROVIDER_NOT_FOUND");
    }

    @Test
    void updateProvider_WithDuplicateNit_Returns409() {
        createProvider("P1", "NIT-UNIQUE", "p1@test.com");
        UUID p2Id = createProvider("P2", "NIT-OTHER", "p2@test.com");

        UpdateProviderRequest req = new UpdateProviderRequest();
        req.setName("P2 Updated");
        req.setTaxIdentificationNumber("NIT-UNIQUE");
        req.setEmail("p2@test.com");

        webTestClient.put().uri("/api/v1/providers/{id}", p2Id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void changeProviderStatus_NotFound_Returns404() {
        ChangeProviderStatusRequest req = new ChangeProviderStatusRequest();
        req.setStatus(ProviderStatus.INACTIVE);

        webTestClient.patch().uri("/api/v1/providers/00000000-0000-0000-0000-000000000000/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createProvider_WithMissingFields_Returns400() {
        CreateProviderRequest req = new CreateProviderRequest();
        webTestClient.post().uri("/api/v1/providers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
