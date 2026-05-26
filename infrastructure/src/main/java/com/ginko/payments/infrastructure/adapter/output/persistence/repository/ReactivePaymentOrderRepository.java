package com.ginko.payments.infrastructure.adapter.output.persistence.repository;

import com.ginko.payments.infrastructure.adapter.output.persistence.entity.PaymentOrderEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ReactivePaymentOrderRepository extends ReactiveCrudRepository<PaymentOrderEntity, UUID> {

    Mono<PaymentOrderEntity> findByIdempotencyKey(String key);

    @Query("SELECT * FROM payment_orders ORDER BY id LIMIT :size OFFSET :offset")
    Flux<PaymentOrderEntity> findAllPaged(int size, int offset);

    @Query("SELECT * FROM payment_orders WHERE status = :status ORDER BY id LIMIT :size OFFSET :offset")
    Flux<PaymentOrderEntity> findByStatusPaged(String status, int size, int offset);

    @Query("SELECT * FROM payment_orders WHERE provider_id = :providerId ORDER BY id LIMIT :size OFFSET :offset")
    Flux<PaymentOrderEntity> findByProviderIdPaged(UUID providerId, int size, int offset);

    @Query("SELECT * FROM payment_orders WHERE status = :status AND provider_id = :providerId ORDER BY id LIMIT :size OFFSET :offset")
    Flux<PaymentOrderEntity> findByStatusAndProviderIdPaged(String status, UUID providerId, int size, int offset);

    @Query("SELECT COUNT(*) FROM payment_orders WHERE status = :status")
    Mono<Long> countByStatus(String status);

    @Query("SELECT COUNT(*) FROM payment_orders WHERE provider_id = :providerId")
    Mono<Long> countByProviderId(UUID providerId);

    @Query("SELECT COUNT(*) FROM payment_orders WHERE status = :status AND provider_id = :providerId")
    Mono<Long> countByStatusAndProviderId(String status, UUID providerId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payment_orders WHERE provider_id = :providerId " +
            "AND status = 'PAID' AND creation_date BETWEEN :start AND :end")
    Mono<BigDecimal> totalPaidByProviderInRange(UUID providerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT * FROM payment_orders WHERE status = 'APPROVED' " +
            "AND creation_date <= :limitDate ORDER BY id LIMIT :size OFFSET :offset")
    Flux<PaymentOrderEntity> findOrdersAboutToExpire(LocalDateTime limitDate, int size, int offset);

    @Query("SELECT COUNT(*) FROM payment_orders WHERE status = 'APPROVED' " +
            "AND creation_date <= :limitDate")
    Mono<Long> countOrdersAboutToExpire(LocalDateTime limitDate);

    @Modifying
    @Query("UPDATE payment_orders SET status = :status, version = version + 1 " +
            "WHERE id = :id AND version = :version")
    Mono<Integer> updateStatusWithVersion(UUID id, String status, Long version);
}
