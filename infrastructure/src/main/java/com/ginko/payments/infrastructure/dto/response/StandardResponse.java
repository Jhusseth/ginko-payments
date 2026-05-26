package com.ginko.payments.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {

    private Meta meta;
    private T data;

    public StandardResponse() {
    }

    public static <T> StandardResponse<T> success(T data) {
        StandardResponse<T> response = new StandardResponse<>();
        response.meta = new Meta();
        response.data = data;
        return response;
    }

    public static <T> StandardResponse<List<T>> paged(List<T> data, long totalElements, int page, int size) {
        StandardResponse<List<T>> response = new StandardResponse<>();
        Meta m = new Meta();
        m.totalElements = totalElements;
        m.page = page;
        m.size = size;
        m.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        response.meta = m;
        response.data = data;
        return response;
    }

    public static StandardResponse<ErrorData> error(int httpStatus, String code, String message) {
        StandardResponse<ErrorData> response = new StandardResponse<>();
        Meta m = new Meta();
        m.status = HttpStatus.valueOf(httpStatus).getReasonPhrase();
        m.statusCode = httpStatus;
        response.meta = m;
        response.data = new ErrorData(code, message);
        return response;
    }

    public Meta getMeta() {
        return meta;
    }

    public T getData() {
        return data;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {
        private String status;
        private Integer statusCode;
        private Long totalElements;
        private Integer page;
        private Integer size;
        private Integer totalPages;

        public String getStatus() {
            return status;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public Long getTotalElements() {
            return totalElements;
        }

        public Integer getPage() {
            return page;
        }

        public Integer getSize() {
            return size;
        }

        public Integer getTotalPages() {
            return totalPages;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorData {
        private String code;
        private String message;

        public ErrorData(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
