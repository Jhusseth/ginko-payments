package com.ginko.payments.domain.exception;

import com.ginko.payments.domain.model.ErrorCode;

public class DuplicateResourceException extends DomainException {

    private final ErrorCode errorCode;

    public DuplicateResourceException(ErrorCode errorCode, String resource, String field, Object value) {
        super(errorCode.getBusinessCode(),
                String.format("%s already exists with %s: %s", resource, field, value));
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
