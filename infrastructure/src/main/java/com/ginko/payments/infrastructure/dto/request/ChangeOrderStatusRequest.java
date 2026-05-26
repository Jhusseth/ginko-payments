package com.ginko.payments.infrastructure.dto.request;

import com.ginko.payments.domain.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class ChangeOrderStatusRequest {
    @NotNull(message = "Status is required")
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
