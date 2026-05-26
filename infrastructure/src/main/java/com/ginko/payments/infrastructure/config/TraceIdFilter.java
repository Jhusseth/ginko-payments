package com.ginko.payments.infrastructure.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceIdFilter implements WebFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "trace.id";

    public static String extractTraceId(ServerWebExchange exchange) {
        Object attr = exchange.getAttribute(TRACE_ID_KEY);
        return attr != null ? attr.toString() : "N/A";
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String headerId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        String traceId = (headerId == null || headerId.isBlank())
                ? UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                : headerId;

        exchange.getResponse().getHeaders().add(TRACE_ID_HEADER, traceId);
        exchange.getAttributes().put(TRACE_ID_KEY, traceId);
        MDC.put(TRACE_ID_KEY, traceId);

        return chain.filter(exchange)
                .doFinally(signalType -> MDC.remove(TRACE_ID_KEY));
    }
}
