package com.ginko.payments.infrastructure.adapter.output.persistence.adapter;

import com.ginko.payments.domain.model.PaymentOrder;
import com.ginko.payments.domain.model.enums.OrderStatus;
import com.ginko.payments.domain.port.output.PaymentOrderRepositoryPort;
import com.ginko.payments.infrastructure.adapter.output.persistence.entity.PaymentOrderEntity;
import com.ginko.payments.infrastructure.adapter.output.persistence.repository.ReactivePaymentOrderRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class PaymentOrderPersistenceAdapter implements PaymentOrderRepositoryPort {

    private final ReactivePaymentOrderRepository repository;

    public PaymentOrderPersistenceAdapter(ReactivePaymentOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PaymentOrder> save(PaymentOrder order) {
        return repository.save(toEntity(order)).map(this::toDomain);
    }

    @Override
    public Mono<PaymentOrder> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<PaymentOrder> findByIdempotencyKey(String key) {
        return repository.findByIdempotencyKey(key).map(this::toDomain);
    }

    @Override
    public Flux<PaymentOrder> findAll(int page, int size) {
        return repository.findAllPaged(size, page * size).map(this::toDomain);
    }

    @Override
    public Flux<PaymentOrder> findByStatus(OrderStatus status, int page, int size) {
        return repository.findByStatusPaged(status.name(), size, page * size).map(this::toDomain);
    }

    @Override
    public Flux<PaymentOrder> findByProviderId(Long providerId, int page, int size) {
        return repository.findByProviderIdPaged(providerId, size, page * size).map(this::toDomain);
    }

    @Override
    public Flux<PaymentOrder> findByStatusAndProviderId(OrderStatus status, Long providerId, int page, int size) {
        return repository.findByStatusAndProviderIdPaged(status.name(), providerId, size, page * size)
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> countAll() {
        return repository.count();
    }

    @Override
    public Mono<Long> countByStatus(OrderStatus status) {
        return repository.countByStatus(status.name());
    }

    @Override
    public Mono<Long> countByProviderId(Long providerId) {
        return repository.countByProviderId(providerId);
    }

    @Override
    public Mono<Long> countByStatusAndProviderId(OrderStatus status, Long providerId) {
        return repository.countByStatusAndProviderId(status.name(), providerId);
    }

    @Override
    public Mono<BigDecimal> totalPaidByProviderInRange(Long providerId, LocalDateTime start, LocalDateTime end) {
        return repository.totalPaidByProviderInRange(providerId, start, end);
    }

    @Override
    public Flux<PaymentOrder> findOrdersAboutToExpire(int days, int page, int size) {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(days);
        return repository.findOrdersAboutToExpire(limitDate, size, page * size).map(this::toDomain);
    }

    @Override
    public Mono<Long> countOrdersAboutToExpire(int days) {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(days);
        return repository.countOrdersAboutToExpire(limitDate);
    }

    @Override
    public Mono<Boolean> updateStatusWithVersion(Long id, OrderStatus newStatus, Long version) {
        return repository.updateStatusWithVersion(id, newStatus.name(), version)
                .map(updated -> updated > 0);
    }

    private PaymentOrderEntity toEntity(PaymentOrder domain) {
        PaymentOrderEntity e = new PaymentOrderEntity();
        e.setId(domain.getId());
        e.setProviderId(domain.getProviderId());
        e.setProviderName(domain.getProviderName());
        e.setAmount(domain.getAmount());
        e.setDescription(domain.getDescription());
        e.setCreationDate(domain.getCreationDate());
        e.setUpdateDate(domain.getUpdateDate());
        e.setStatus(domain.getStatus());
        e.setVersion(domain.getVersion());
        e.setIdempotencyKey(domain.getIdempotencyKey());
        return e;
    }

    private PaymentOrder toDomain(PaymentOrderEntity entity) {
        return new PaymentOrder(entity.getId(), entity.getProviderId(), entity.getProviderName(),
                entity.getAmount(), entity.getDescription(), entity.getCreationDate(), entity.getUpdateDate(),
                entity.getStatus(), entity.getVersion(), entity.getIdempotencyKey());
    }
}
