package com.ginko.payments.infrastructure.dto.response;

import com.ginko.payments.domain.model.PaymentOrder;
import com.ginko.payments.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentOrderResponse {
    private Long id;
    private Long providerId;
    private String providerName;
    private BigDecimal amount;
    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
    private OrderStatus status;

    public PaymentOrderResponse() {
    }

    public static PaymentOrderResponse fromDomain(PaymentOrder o) {
        PaymentOrderResponse r = new PaymentOrderResponse();
        r.id = o.getId();
        r.providerId = o.getProviderId();
        r.providerName = o.getProviderName();
        r.amount = o.getAmount();
        r.description = o.getDescription();
        r.creationDate = o.getCreationDate();
        r.updateDate = o.getUpdateDate();
        r.status = o.getStatus();
        return r;
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
}
