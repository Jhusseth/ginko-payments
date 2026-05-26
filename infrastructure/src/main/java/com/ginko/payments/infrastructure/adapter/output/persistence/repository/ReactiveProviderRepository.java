package com.ginko.payments.infrastructure.adapter.output.persistence.repository;

import com.ginko.payments.infrastructure.adapter.output.persistence.entity.ProviderEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveProviderRepository extends ReactiveCrudRepository<ProviderEntity, Long> {
    Mono<Boolean> existsByTaxIdentificationNumber(String nit);

    Mono<ProviderEntity> findByTaxIdentificationNumber(String nit);

    @Query("SELECT * FROM providers ORDER BY id LIMIT :size OFFSET :offset")
    Flux<ProviderEntity> findAllPaged(int size, int offset);

    @Query("SELECT * FROM providers WHERE status = :status ORDER BY id LIMIT :size OFFSET :offset")
    Flux<ProviderEntity> findByStatusPaged(String status, int size, int offset);

    @Query("SELECT COUNT(*) FROM providers WHERE status = :status")
    Mono<Long> countByStatus(String status);
}
