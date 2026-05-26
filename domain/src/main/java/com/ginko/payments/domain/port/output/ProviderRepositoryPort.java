package com.ginko.payments.domain.port.output;

import com.ginko.payments.domain.model.Provider;
import com.ginko.payments.domain.model.enums.ProviderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProviderRepositoryPort {
    Mono<Provider> save(Provider provider);

    Mono<Provider> findById(UUID id);

    Mono<Provider> findByTaxIdentificationNumber(String nit);

    Flux<Provider> findAll(int page, int size);

    Flux<Provider> findByStatus(ProviderStatus status, int page, int size);

    Mono<Long> countAll();

    Mono<Long> countByStatus(ProviderStatus status);

    Mono<Boolean> existsByTaxIdentificationNumber(String nit);
}
