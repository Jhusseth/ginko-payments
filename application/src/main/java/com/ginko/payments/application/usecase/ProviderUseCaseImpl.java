package com.ginko.payments.application.usecase;

import com.ginko.payments.domain.exception.DuplicateResourceException;
import com.ginko.payments.domain.exception.ResourceNotFoundException;
import com.ginko.payments.domain.model.ErrorCode;
import com.ginko.payments.domain.model.Provider;
import com.ginko.payments.domain.model.enums.ProviderStatus;
import com.ginko.payments.domain.port.input.ProviderUseCase;
import com.ginko.payments.domain.port.output.ProviderRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProviderUseCaseImpl implements ProviderUseCase {

    private final ProviderRepositoryPort repository;

    public ProviderUseCaseImpl(ProviderRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Provider> create(String name, String nit, String email) {
        return repository.existsByTaxIdentificationNumber(nit)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                                ErrorCode.NIT_DUPLICATE, "Provider", "nit", nit));
                    }
                    return repository.save(new Provider(name, nit, email));
                });
    }

    @Override
    public Flux<Provider> list(ProviderStatus status, int page, int size) {
        if (status != null) {
            return repository.findByStatus(status, page, size);
        }
        return repository.findAll(page, size);
    }

    @Override
    public Mono<Long> count(ProviderStatus status) {
        if (status != null) {
            return repository.countByStatus(status);
        }
        return repository.countAll();
    }

    @Override
    public Mono<Provider> getById(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ErrorCode.PROVIDER_NOT_FOUND, "Provider", "id", id)));
    }

    @Override
    public Mono<Provider> update(Long id, String name, String nit, String email) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ErrorCode.PROVIDER_NOT_FOUND, "Provider", "id", id)))
                .flatMap(existing -> repository.findByTaxIdentificationNumber(nit)
                        .flatMap(dup -> {
                            if (!dup.getId().equals(id)) {
                                return Mono.error(new DuplicateResourceException(
                                        ErrorCode.NIT_DUPLICATE, "Provider", "nit", nit));
                            }
                            return Mono.just(existing);
                        })
                        .switchIfEmpty(Mono.just(existing)))
                .flatMap(p -> repository.save(p.withUpdatedData(name, nit, email)));
    }

    @Override
    public Mono<Provider> changeStatus(Long id, ProviderStatus newStatus) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ErrorCode.PROVIDER_NOT_FOUND, "Provider", "id", id)))
                .flatMap(p -> repository.save(p.withStatus(newStatus)));
    }

}
