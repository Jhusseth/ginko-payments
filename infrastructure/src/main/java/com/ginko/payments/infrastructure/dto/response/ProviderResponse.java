package com.ginko.payments.infrastructure.dto.response;

import com.ginko.payments.domain.model.Provider;
import com.ginko.payments.domain.model.enums.ProviderStatus;

public class ProviderResponse {
    private Long id;
    private String name;
    private String taxIdentificationNumber;
    private String email;
    private ProviderStatus status;

    public ProviderResponse() {
    }

    public static ProviderResponse fromDomain(Provider p) {
        ProviderResponse r = new ProviderResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.taxIdentificationNumber = p.getTaxIdentificationNumber();
        r.email = p.getEmail();
        r.status = p.getStatus();
        return r;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    public String getEmail() {
        return email;
    }

    public ProviderStatus getStatus() {
        return status;
    }
}
