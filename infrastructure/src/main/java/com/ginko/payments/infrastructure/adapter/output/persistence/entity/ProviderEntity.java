package com.ginko.payments.infrastructure.adapter.output.persistence.entity;

import com.ginko.payments.domain.model.enums.ProviderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("providers")
public class ProviderEntity {
    @Id
    private Long id;
    private String name;
    private String taxIdentificationNumber;
    private String email;
    private ProviderStatus status;

    public ProviderEntity() {
    }

    public ProviderEntity(Long id, String name, String taxIdentificationNumber,
                          String email, ProviderStatus status) {
        this.id = id;
        this.name = name;
        this.taxIdentificationNumber = taxIdentificationNumber;
        this.email = email;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    public void setTaxIdentificationNumber(String v) {
        this.taxIdentificationNumber = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ProviderStatus getStatus() {
        return status;
    }

    public void setStatus(ProviderStatus status) {
        this.status = status;
    }
}
