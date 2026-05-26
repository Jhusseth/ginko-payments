package com.ginko.payments.domain.port.input;

import com.ginko.payments.domain.model.PaymentOrder;
import com.ginko.payments.domain.model.enums.OrderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface PaymentOrderUseCase {
    Mono<PaymentOrder> create(UUID providerId, BigDecimal amount, String description, String idempotencyKey);

    Flux<PaymentOrder> list(OrderStatus status, UUID providerId, int page, int size);

    Mono<Long> count(OrderStatus status, UUID providerId);

    Mono<PaymentOrder> getById(UUID id);

    Mono<PaymentOrder> transitionStatus(UUID id, OrderStatus newStatus);

    Mono<BigDecimal> reportTotalPaid(UUID providerId, LocalDate startDate, LocalDate endDate);

    Flux<PaymentOrder> ordersAboutToExpire(int page, int size);

    Mono<Long> countAboutToExpire();
}
