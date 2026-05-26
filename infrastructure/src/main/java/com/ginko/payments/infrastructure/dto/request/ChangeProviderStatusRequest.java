package com.ginko.payments.infrastructure.dto.request;

import com.ginko.payments.domain.model.enums.ProviderStatus;
import jakarta.validation.constraints.NotNull;

public class ChangeProviderStatusRequest {
    @NotNull(message = "Status is required")
    private ProviderStatus status;

    public ProviderStatus getStatus() {
        return status;
    }

    public void setStatus(ProviderStatus status) {
        this.status = status;
    }
}
