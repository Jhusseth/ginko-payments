package com.ginko.payments.domain.port.input;

import com.ginko.payments.domain.model.Provider;
import com.ginko.payments.domain.model.enums.ProviderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProviderUseCase {
    Mono<Provider> create(String name, String nit, String email);

    Flux<Provider> list(ProviderStatus status, int page, int size);

    Mono<Long> count(ProviderStatus status);

    Mono<Provider> getById(UUID id);

    Mono<Provider> update(UUID id, String name, String nit, String email);

    Mono<Provider> changeStatus(UUID id, ProviderStatus newStatus);
}
