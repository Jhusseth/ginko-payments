package com.ginko.payments.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ginko.payments.domain.model.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class EcsLogger {

    private static final Logger log = LoggerFactory.getLogger(EcsLogger.class);
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void logError(ErrorCode errorCode, String message, Throwable cause, String traceId) {
        try {
            Map<String, Object> json = buildBaseJson(traceId);

            Map<String, Object> error = new LinkedHashMap<>();
            error.put("code", errorCode.getTechnicalCode());
            error.put("business_code", errorCode.getBusinessCode());
            error.put("description", message);
            error.put("http_status", errorCode.getHttpStatus());
            if (cause != null) {
                error.put("exception", cause.getClass().getName());
                error.put("stack_trace", stackTraceToString(cause));
            }
            json.put("error", error);

            String jsonString = mapper.writeValueAsString(json);

            if (errorCode.getHttpStatus() >= 500) {
                log.error(jsonString);
            } else {
                log.warn(jsonString);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error log", e);
        }
    }

    public void logUnexpected(Throwable cause, String traceId) {
        logError(ErrorCode.INTERNAL_ERROR, cause.getMessage(), cause, traceId);
    }

    private Map<String, Object> buildBaseJson(String traceId) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("@timestamp", Instant.now().toString());
        json.put("trace.id", traceId != null ? traceId : "N/A");
        json.put("service.name", "ginko-payments");
        return json;
    }

    private String stackTraceToString(Throwable cause) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        return sw.toString();
    }
}
