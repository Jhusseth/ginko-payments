package com.ginko.payments.domain.port.output;

import com.ginko.payments.domain.model.PaymentOrder;
import com.ginko.payments.domain.model.enums.OrderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentOrderRepositoryPort {
    Mono<PaymentOrder> save(PaymentOrder order);

    Mono<PaymentOrder> findById(Long id);

    Mono<PaymentOrder> findByIdempotencyKey(String key);

    Flux<PaymentOrder> findAll(int page, int size);

    Flux<PaymentOrder> findByStatus(OrderStatus status, int page, int size);

    Flux<PaymentOrder> findByProviderId(Long providerId, int page, int size);

    Flux<PaymentOrder> findByStatusAndProviderId(OrderStatus status, Long providerId, int page, int size);

    Mono<Long> countAll();

    Mono<Long> countByStatus(OrderStatus status);

    Mono<Long> countByProviderId(Long providerId);

    Mono<Long> countByStatusAndProviderId(OrderStatus status, Long providerId);

    Mono<BigDecimal> totalPaidByProviderInRange(Long providerId, LocalDateTime start, LocalDateTime end);

    Flux<PaymentOrder> findOrdersAboutToExpire(int days, int page, int size);

    Mono<Long> countOrdersAboutToExpire(int days);

    Mono<Boolean> updateStatusWithVersion(Long id, OrderStatus newStatus, Long version);
}
