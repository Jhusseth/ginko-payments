package com.ginko.payments.domain.port.input;

import com.ginko.payments.domain.model.PaymentOrder;
import com.ginko.payments.domain.model.enums.OrderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentOrderUseCase {
    Mono<PaymentOrder> create(Long providerId, BigDecimal amount, String description, String idempotencyKey);

    Flux<PaymentOrder> list(OrderStatus status, Long providerId, int page, int size);

    Mono<Long> count(OrderStatus status, Long providerId);

    Mono<PaymentOrder> getById(Long id);

    Mono<PaymentOrder> transitionStatus(Long id, OrderStatus newStatus);

    Mono<BigDecimal> reportTotalPaid(Long providerId, LocalDate startDate, LocalDate endDate);

    Flux<PaymentOrder> ordersAboutToExpire(int page, int size);

    Mono<Long> countAboutToExpire();
}
