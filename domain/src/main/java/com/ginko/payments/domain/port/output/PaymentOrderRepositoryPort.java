package com.ginko.payments.domain.port.output;

import com.ginko.payments.domain.model.PaymentOrder;
import com.ginko.payments.domain.model.enums.OrderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentOrderRepositoryPort {
    Mono<PaymentOrder> save(PaymentOrder order);

    Mono<PaymentOrder> findById(UUID id);

    Mono<PaymentOrder> findByIdempotencyKey(String key);

    Flux<PaymentOrder> findAll(int page, int size);

    Flux<PaymentOrder> findByStatus(OrderStatus status, int page, int size);

    Flux<PaymentOrder> findByProviderId(UUID providerId, int page, int size);

    Flux<PaymentOrder> findByStatusAndProviderId(OrderStatus status, UUID providerId, int page, int size);

    Mono<Long> countAll();

    Mono<Long> countByStatus(OrderStatus status);

    Mono<Long> countByProviderId(UUID providerId);

    Mono<Long> countByStatusAndProviderId(OrderStatus status, UUID providerId);

    Mono<BigDecimal> totalPaidByProviderInRange(UUID providerId, LocalDateTime start, LocalDateTime end);

    Flux<PaymentOrder> findOrdersAboutToExpire(int days, int page, int size);

    Mono<Long> countOrdersAboutToExpire(int days);

    Mono<Boolean> updateStatusWithVersion(UUID id, OrderStatus newStatus, Long version);
}
