package com.ginko.payments.infrastructure.adapter.output.persistence.entity;

import com.ginko.payments.domain.model.enums.OrderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("payment_orders")
public class PaymentOrderEntity {
    @Id
    private Long id;
    private Long providerId;
    private String providerName;
    private BigDecimal amount;
    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
    private OrderStatus status;
    @Version
    private Long version;
    private String idempotencyKey;

    public PaymentOrderEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long v) {
        this.providerId = v;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String v) {
        this.providerName = v;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime v) {
        this.creationDate = v;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime v) {
        this.updateDate = v;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String v) {
        this.idempotencyKey = v;
    }
}
