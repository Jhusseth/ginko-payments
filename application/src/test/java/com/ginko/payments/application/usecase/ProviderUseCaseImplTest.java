package com.ginko.payments.application.usecase;

import com.ginko.payments.domain.exception.DuplicateResourceException;
import com.ginko.payments.domain.exception.ResourceNotFoundException;
import com.ginko.payments.domain.model.Provider;
import com.ginko.payments.domain.model.enums.ProviderStatus;
import com.ginko.payments.domain.port.output.ProviderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderUseCaseImplTest {

    private static final UUID PROVIDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MISSING_ID = UUID.fromString("00000000-0000-0000-0000-000000000063");

    @Mock
    private ProviderRepositoryPort repository;

    private ProviderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProviderUseCaseImpl(repository);
    }

    @Test
    void create_WhenNitNotExists_ShouldSave() {
        when(repository.existsByTaxIdentificationNumber("NIT-001")).thenReturn(Mono.just(false));
        when(repository.save(any())).thenReturn(Mono.just(new Provider(PROVIDER_ID, "Test", "NIT-001", "e@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.create("Test", "NIT-001", "e@e.com"))
                .expectNextMatches(p -> p.getId().equals(PROVIDER_ID) && p.getName().equals("Test"))
                .verifyComplete();
    }

    @Test
    void create_WhenNitExists_ShouldThrowDuplicate() {
        when(repository.existsByTaxIdentificationNumber("NIT-001")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.create("Test", "NIT-001", "e@e.com"))
                .expectError(DuplicateResourceException.class)
                .verify();
    }

    @Test
    void list_WithoutStatus_ShouldReturnAll() {
        when(repository.findAll(0, 20)).thenReturn(Flux.just(
                new Provider(PROVIDER_ID, "P1", "N1", "e1@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.list(null, 0, 20))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void list_WithStatus_ShouldFilter() {
        when(repository.findByStatus(ProviderStatus.ACTIVE, 0, 20)).thenReturn(Flux.just(
                new Provider(PROVIDER_ID, "P1", "N1", "e1@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.list(ProviderStatus.ACTIVE, 0, 20))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getById_WhenExists_ShouldReturn() {
        when(repository.findById(PROVIDER_ID)).thenReturn(Mono.just(
                new Provider(PROVIDER_ID, "Test", "NIT", "e@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.getById(PROVIDER_ID))
                .expectNextMatches(p -> p.getId().equals(PROVIDER_ID))
                .verifyComplete();
    }

    @Test
    void getById_WhenNotExists_ShouldThrowNotFound() {
        when(repository.findById(MISSING_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getById(MISSING_ID))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void update_WhenExistsAndNitUnique_ShouldUpdate() {
        Provider existing = new Provider(PROVIDER_ID, "Old", "NIT", "old@e.com", ProviderStatus.ACTIVE);
        when(repository.findById(PROVIDER_ID)).thenReturn(Mono.just(existing));
        when(repository.findByTaxIdentificationNumber("NIT-NEW")).thenReturn(Mono.empty());
        when(repository.save(any())).thenReturn(Mono.just(
                new Provider(PROVIDER_ID, "New", "NIT-NEW", "new@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.update(PROVIDER_ID, "New", "NIT-NEW", "new@e.com"))
                .expectNextMatches(p -> p.getName().equals("New"))
                .verifyComplete();
    }

    @Test
    void update_WhenNotExists_ShouldThrowNotFound() {
        when(repository.findById(MISSING_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.update(MISSING_ID, "New", "NIT", "e@e.com"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void changeStatus_ShouldUpdate() {
        Provider existing = new Provider(PROVIDER_ID, "Test", "NIT", "e@e.com", ProviderStatus.ACTIVE);
        when(repository.findById(PROVIDER_ID)).thenReturn(Mono.just(existing));
        when(repository.save(any())).thenReturn(Mono.just(
                new Provider(PROVIDER_ID, "Test", "NIT", "e@e.com", ProviderStatus.INACTIVE)));

        StepVerifier.create(useCase.changeStatus(PROVIDER_ID, ProviderStatus.INACTIVE))
                .expectNextMatches(p -> p.getStatus() == ProviderStatus.INACTIVE)
                .verifyComplete();
    }

    @Test
    void count_WithoutStatus_ShouldReturnAllCount() {
        when(repository.countAll()).thenReturn(Mono.just(5L));

        StepVerifier.create(useCase.count(null))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void count_WithStatus_ShouldReturnFilteredCount() {
        when(repository.countByStatus(ProviderStatus.ACTIVE)).thenReturn(Mono.just(3L));

        StepVerifier.create(useCase.count(ProviderStatus.ACTIVE))
                .expectNext(3L)
                .verifyComplete();
    }
}
