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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderUseCaseImplTest {

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
        when(repository.save(any())).thenReturn(Mono.just(new Provider(1L, "Test", "NIT-001", "e@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.create("Test", "NIT-001", "e@e.com"))
                .expectNextMatches(p -> p.getId() == 1L && p.getName().equals("Test"))
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
                new Provider(1L, "P1", "N1", "e1@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.list(null, 0, 20))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void list_WithStatus_ShouldFilter() {
        when(repository.findByStatus(ProviderStatus.ACTIVE, 0, 20)).thenReturn(Flux.just(
                new Provider(1L, "P1", "N1", "e1@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.list(ProviderStatus.ACTIVE, 0, 20))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getById_WhenExists_ShouldReturn() {
        when(repository.findById(1L)).thenReturn(Mono.just(
                new Provider(1L, "Test", "NIT", "e@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.getById(1L))
                .expectNextMatches(p -> p.getId() == 1L)
                .verifyComplete();
    }

    @Test
    void getById_WhenNotExists_ShouldThrowNotFound() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getById(99L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void update_WhenExistsAndNitUnique_ShouldUpdate() {
        Provider existing = new Provider(1L, "Old", "NIT", "old@e.com", ProviderStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.findByTaxIdentificationNumber("NIT-NEW")).thenReturn(Mono.empty());
        when(repository.save(any())).thenReturn(Mono.just(
                new Provider(1L, "New", "NIT-NEW", "new@e.com", ProviderStatus.ACTIVE)));

        StepVerifier.create(useCase.update(1L, "New", "NIT-NEW", "new@e.com"))
                .expectNextMatches(p -> p.getName().equals("New"))
                .verifyComplete();
    }

    @Test
    void update_WhenNotExists_ShouldThrowNotFound() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.update(99L, "New", "NIT", "e@e.com"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void changeStatus_ShouldUpdate() {
        Provider existing = new Provider(1L, "Test", "NIT", "e@e.com", ProviderStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.save(any())).thenReturn(Mono.just(
                new Provider(1L, "Test", "NIT", "e@e.com", ProviderStatus.INACTIVE)));

        StepVerifier.create(useCase.changeStatus(1L, ProviderStatus.INACTIVE))
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
