package com.ginko.payments.domain.port.input;

import com.ginko.payments.domain.model.Provider;
import com.ginko.payments.domain.model.enums.ProviderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProviderUseCase {
    Mono<Provider> create(String name, String nit, String email);

    Flux<Provider> list(ProviderStatus status, int page, int size);

    Mono<Long> count(ProviderStatus status);

    Mono<Provider> getById(Long id);

    Mono<Provider> update(Long id, String name, String nit, String email);

    Mono<Provider> changeStatus(Long id, ProviderStatus newStatus);
}
