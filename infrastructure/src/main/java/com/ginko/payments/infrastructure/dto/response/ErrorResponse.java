package com.ginko.payments.infrastructure.dto.response;

import com.ginko.payments.domain.model.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {

    private String technicalCode;
    private String businessCode;
    private String message;
    private int httpStatus;
    private LocalDateTime timestamp;
    private String traceId;
    private List<ErrorDetail> details;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse fromErrorCode(ErrorCode errorCode, String traceId) {
        ErrorResponse r = new ErrorResponse();
        r.technicalCode = errorCode.getTechnicalCode();
        r.businessCode = errorCode.getBusinessCode();
        r.message = errorCode.getMessage();
        r.httpStatus = errorCode.getHttpStatus();
        r.traceId = traceId;
        return r;
    }

    public ErrorResponse withDetails(List<ErrorDetail> details) {
        this.details = details;
        return this;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public List<ErrorDetail> getDetails() {
        return details;
    }

    public static class ErrorDetail {
        private String field;
        private String message;

        public ErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
