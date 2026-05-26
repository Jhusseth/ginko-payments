package com.ginko.payments.domain.model;

import com.ginko.payments.domain.model.enums.ProviderStatus;

public class Provider {

    private final Long id;
    private final String name;
    private final String taxIdentificationNumber;
    private final String email;
    private final ProviderStatus status;

    public Provider(String name, String taxIdentificationNumber, String email) {
        this(null, name, taxIdentificationNumber, email, ProviderStatus.ACTIVE);
    }

    public Provider(Long id, String name, String taxIdentificationNumber,
                    String email, ProviderStatus status) {
        this.id = id;
        this.name = name;
        this.taxIdentificationNumber = taxIdentificationNumber;
        this.email = email;
        this.status = status;
    }

    public Provider withId(Long id) {
        return new Provider(id, this.name, this.taxIdentificationNumber, this.email, this.status);
    }

    public Provider withStatus(ProviderStatus newStatus) {
        return new Provider(this.id, this.name, this.taxIdentificationNumber, this.email, newStatus);
    }

    public Provider withUpdatedData(String name, String nit, String email) {
        return new Provider(this.id, name, nit, email, this.status);
    }

    public boolean isActive() {
        return ProviderStatus.ACTIVE.equals(this.status);
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
