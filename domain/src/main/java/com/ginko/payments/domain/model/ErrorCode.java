package com.ginko.payments.domain.model;

public enum ErrorCode {

    PROVIDER_NOT_FOUND("ERR-PROV-001", "PROVIDER_NOT_FOUND",
            "Provider not found", 404),
    NIT_DUPLICATE("ERR-PROV-002", "NIT_DUPLICATE",
            "The entered NIT already exists in the system", 409),
    INVALID_PROVIDER_STATUS("ERR-PROV-003", "INVALID_PROVIDER_STATUS",
            "Invalid provider status", 400),

    ORDER_NOT_FOUND("ERR-ORDEN-001", "ORDER_NOT_FOUND",
            "Payment order not found", 404),
    INVALID_TRANSITION("ERR-ORDEN-002", "INVALID_TRANSITION",
            "State transition not allowed for the order", 400),
    INACTIVE_PROVIDER("ERR-ORDEN-003", "INACTIVE_PROVIDER",
            "Cannot create an order for an inactive provider", 400),
    ORDER_CONCURRENCY("ERR-ORDEN-004", "ORDER_CONCURRENCY",
            "The order was modified by another user. Please try again", 409),

    FIELD_VALIDATION("ERR-VAL-001", "FIELD_VALIDATION",
            "Validation error in input fields", 400),

    RESOURCE_NOT_FOUND("ERR-RES-001", "RESOURCE_NOT_FOUND",
            "The requested resource could not be found.", 404),

    INTERNAL_ERROR("ERR-GEN-001", "INTERNAL_ERROR",
            "An unexpected server error occurred", 500),
    SERVICE_UNAVAILABLE("ERR-GEN-002", "SERVICE_UNAVAILABLE",
            "Service temporarily unavailable", 503);

    private final String technicalCode;
    private final String businessCode;
    private final String message;
    private final int httpStatus;

    ErrorCode(String technicalCode, String businessCode, String message, int httpStatus) {
        this.technicalCode = technicalCode;
        this.businessCode = businessCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getTechnicalCode() {
        return technicalCode;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
