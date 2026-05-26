package com.ginko.payments.application.usecase;

import com.ginko.payments.domain.exception.ResourceNotFoundException;
import com.ginko.payments.domain.model.BusinessException;
import com.ginko.payments.domain.model.ErrorCode;
import com.ginko.payments.domain.model.PaymentOrder;
import com.ginko.payments.domain.model.enums.OrderStatus;
import com.ginko.payments.domain.port.input.PaymentOrderUseCase;
import com.ginko.payments.domain.port.output.PaymentOrderRepositoryPort;
import com.ginko.payments.domain.port.output.ProviderRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
public class PaymentOrderUseCaseImpl implements PaymentOrderUseCase {

    private static final int EXPIRY_DAYS = 30;

    private final PaymentOrderRepositoryPort paymentOrderRepository;
    private final ProviderRepositoryPort providerRepository;

    public PaymentOrderUseCaseImpl(PaymentOrderRepositoryPort paymentOrderRepository,
                                   ProviderRepositoryPort providerRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.providerRepository = providerRepository;
    }

    @Override
    public Mono<PaymentOrder> create(UUID providerId, BigDecimal amount, String description, String idempotencyKey) {
        if (idempotencyKey != null) {
            return paymentOrderRepository.findByIdempotencyKey(idempotencyKey)
                    .flatMap(Mono::just)
                    .switchIfEmpty(Mono.defer(() ->
                            createNewOrder(providerId, amount, description, idempotencyKey)));
        }
        return createNewOrder(providerId, amount, description, null);
    }

    private Mono<PaymentOrder> createNewOrder(UUID providerId, BigDecimal amount,
                                              String description, String idempotencyKey) {
        return providerRepository.findById(providerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ErrorCode.PROVIDER_NOT_FOUND, "Provider", "id", providerId)))
                .flatMap(provider -> {
                    if (!provider.isActive()) {
                        return Mono.error(new BusinessException(
                                ErrorCode.INACTIVE_PROVIDER));
                    }
                    PaymentOrder order = new PaymentOrder(providerId, provider.getName(), amount, description);
                    if (idempotencyKey != null) {
                        order = order.withIdempotencyKey(idempotencyKey);
                    }
                    return paymentOrderRepository.save(order);
                });
    }

    @Override
    public Flux<PaymentOrder> list(OrderStatus status, UUID providerId, int page, int size) {
        if (status != null && providerId != null) {
            return paymentOrderRepository.findByStatusAndProviderId(status, providerId, page, size);
        } else if (status != null) {
            return paymentOrderRepository.findByStatus(status, page, size);
        } else if (providerId != null) {
            return paymentOrderRepository.findByProviderId(providerId, page, size);
        }
        return paymentOrderRepository.findAll(page, size);
    }

    @Override
    public Mono<Long> count(OrderStatus status, UUID providerId) {
        if (status != null && providerId != null) {
            return paymentOrderRepository.countByStatusAndProviderId(status, providerId);
        } else if (status != null) {
            return paymentOrderRepository.countByStatus(status);
        } else if (providerId != null) {
            return paymentOrderRepository.countByProviderId(providerId);
        }
        return paymentOrderRepository.countAll();
    }

    @Override
    public Mono<PaymentOrder> getById(UUID id) {
        return paymentOrderRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ErrorCode.ORDER_NOT_FOUND, "Payment order", "id", id)));
    }

    @Override
    public Mono<PaymentOrder> transitionStatus(UUID id, OrderStatus newStatus) {
        return paymentOrderRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ErrorCode.ORDER_NOT_FOUND, "Payment order", "id", id)))
                .flatMap(order -> {
                    PaymentOrder updated;
                    try {
                        updated = order.transitionTo(newStatus);
                    } catch (BusinessException e) {
                        return Mono.error(new BusinessException(
                                ErrorCode.INVALID_TRANSITION, e.getMessage()));
                    }
                    return paymentOrderRepository.updateStatusWithVersion(
                                    id, newStatus, order.getVersion())
                            .flatMap(success -> {
                                if (!success) {
                                    return Mono.error(new BusinessException(
                                            ErrorCode.ORDER_CONCURRENCY));
                                }
                                return Mono.just(updated.withVersion(order.getVersion() + 1));
                            });
                });
    }

    @Override
    public Mono<BigDecimal> reportTotalPaid(UUID providerId, LocalDate startDate, LocalDate endDate) {
        return providerRepository.findById(providerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        ErrorCode.PROVIDER_NOT_FOUND, "Provider", "id", providerId)))
                .flatMap(p -> paymentOrderRepository.totalPaidByProviderInRange(
                        providerId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX)));
    }

    @Override
    public Flux<PaymentOrder> ordersAboutToExpire(int page, int size) {
        return paymentOrderRepository.findOrdersAboutToExpire(EXPIRY_DAYS, page, size);
    }

    @Override
    public Mono<Long> countAboutToExpire() {
        return paymentOrderRepository.countOrdersAboutToExpire(EXPIRY_DAYS);
    }

}
