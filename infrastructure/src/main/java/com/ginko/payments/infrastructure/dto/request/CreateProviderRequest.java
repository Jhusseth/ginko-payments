package com.ginko.payments.infrastructure.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateProviderRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "NIT is required")
    private String taxIdentificationNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

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
}
