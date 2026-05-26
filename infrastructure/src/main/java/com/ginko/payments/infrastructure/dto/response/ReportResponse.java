package com.ginko.payments.infrastructure.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class ReportResponse {
    private UUID providerId;
    private String providerName;
    private BigDecimal totalPaid;
    private String startDate;
    private String endDate;

    public ReportResponse() {
    }

    public ReportResponse(UUID providerId, String providerName, BigDecimal totalPaid,
                          String startDate, String endDate) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.totalPaid = totalPaid;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
