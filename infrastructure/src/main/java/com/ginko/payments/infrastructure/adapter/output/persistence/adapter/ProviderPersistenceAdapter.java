package com.ginko.payments.infrastructure.adapter.output.persistence.adapter;

import com.ginko.payments.domain.model.Provider;
import com.ginko.payments.domain.model.enums.ProviderStatus;
import com.ginko.payments.domain.port.output.ProviderRepositoryPort;
import com.ginko.payments.infrastructure.adapter.output.persistence.entity.ProviderEntity;
import com.ginko.payments.infrastructure.adapter.output.persistence.repository.ReactiveProviderRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ProviderPersistenceAdapter implements ProviderRepositoryPort {

    private final ReactiveProviderRepository repository;

    public ProviderPersistenceAdapter(ReactiveProviderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Provider> save(Provider provider) {
        ProviderEntity entity = toEntity(provider);
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Provider> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<Provider> findByTaxIdentificationNumber(String nit) {
        return repository.findByTaxIdentificationNumber(nit).map(this::toDomain);
    }

    @Override
    public Flux<Provider> findAll(int page, int size) {
        return repository.findAllPaged(size, page * size).map(this::toDomain);
    }

    @Override
    public Flux<Provider> findByStatus(ProviderStatus status, int page, int size) {
        return repository.findByStatusPaged(status.name(), size, page * size).map(this::toDomain);
    }

    @Override
    public Mono<Long> countAll() {
        return repository.count();
    }

    @Override
    public Mono<Long> countByStatus(ProviderStatus status) {
        return repository.countByStatus(status.name());
    }

    @Override
    public Mono<Boolean> existsByTaxIdentificationNumber(String nit) {
        return repository.existsByTaxIdentificationNumber(nit);
    }

    private ProviderEntity toEntity(Provider domain) {
        return new ProviderEntity(domain.getId(), domain.getName(),
                domain.getTaxIdentificationNumber(), domain.getEmail(), domain.getStatus());
    }

    private Provider toDomain(ProviderEntity entity) {
        return new Provider(entity.getId(), entity.getName(),
                entity.getTaxIdentificationNumber(), entity.getEmail(), entity.getStatus());
    }
}
