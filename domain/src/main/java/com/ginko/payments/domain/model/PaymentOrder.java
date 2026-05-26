package com.ginko.payments.domain.model;

import com.ginko.payments.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class PaymentOrder {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.DRAFT, Set.of(OrderStatus.APPROVED, OrderStatus.REJECTED),
            OrderStatus.APPROVED, Set.of(OrderStatus.PAID)
    );

    private final Long id;
    private final Long providerId;
    private final String providerName;
    private final BigDecimal amount;
    private final String description;
    private final LocalDateTime creationDate;
    private final LocalDateTime updateDate;
    private final OrderStatus status;
    private final Long version;
    private final String idempotencyKey;

    public PaymentOrder(Long providerId, String providerName, BigDecimal amount, String description) {
        this(null, providerId, providerName, amount, description,
                LocalDateTime.now(), null, OrderStatus.DRAFT, null, null);
    }

    public PaymentOrder(Long id, Long providerId, String providerName, BigDecimal amount,
                        String description, LocalDateTime creationDate, LocalDateTime updateDate,
                        OrderStatus status, Long version, String idempotencyKey) {
        this.id = id;
        this.providerId = providerId;
        this.providerName = providerName;
        this.amount = amount;
        this.description = description;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.status = status;
        this.version = version;
        this.idempotencyKey = idempotencyKey;
    }

    public PaymentOrder transitionTo(OrderStatus newStatus) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(this.status);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_TRANSITION, String.format(
                    "Invalid transition from %s to %s. " +
                            "Valid transitions: DRAFT->APPROVED, DRAFT->REJECTED, APPROVED->PAID",
                    this.status, newStatus));
        }
        return new PaymentOrder(this.id, this.providerId, this.providerName, this.amount,
                this.description, this.creationDate, LocalDateTime.now(), newStatus, this.version, this.idempotencyKey);
    }

    public PaymentOrder withId(Long id) {
        return new PaymentOrder(id, this.providerId, this.providerName, this.amount,
                this.description, this.creationDate, this.updateDate, this.status, this.version, this.idempotencyKey);
    }

    public PaymentOrder withVersion(Long version) {
        return new PaymentOrder(this.id, this.providerId, this.providerName, this.amount,
                this.description, this.creationDate, this.updateDate, this.status, version, this.idempotencyKey);
    }

    public PaymentOrder withIdempotencyKey(String idempotencyKey) {
        return new PaymentOrder(this.id, this.providerId, this.providerName, this.amount,
                this.description, this.creationDate, this.updateDate, this.status, this.version, idempotencyKey);
    }

    public boolean isAboutToExpire(int limitDays) {
        return OrderStatus.APPROVED.equals(this.status)
                && this.creationDate.isBefore(LocalDateTime.now().minusDays(limitDays));
    }

    public Long getId() {
        return id;
    }

    public Long getProviderId() {
        return providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
