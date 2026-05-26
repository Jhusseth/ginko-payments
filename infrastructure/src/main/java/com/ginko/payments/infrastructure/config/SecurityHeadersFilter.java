package com.ginko.payments.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
public class SecurityHeadersFilter implements WebFilter {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOriginsRaw;

    private List<String> allowedOrigins;
    private boolean allowAll;

    @PostConstruct
    void init() {
        String trimmed = allowedOriginsRaw.trim();
        if ("*".equals(trimmed)) {
            allowAll = true;
        } else {
            allowedOrigins = Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();

        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        headers.set("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        headers.set("Pragma", "no-cache");

        resolveCorsOrigin(exchange, headers);

        if (isPreflightRequest(exchange)) {
            headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Trace-Id, Idempotency-Key");
            headers.set("Access-Control-Max-Age", "3600");
            response.setStatusCode(HttpStatus.OK);
            return Mono.empty();
        }

        return chain.filter(exchange);
    }

    private void resolveCorsOrigin(ServerWebExchange exchange, HttpHeaders headers) {
        if (allowAll) {
            headers.set("Access-Control-Allow-Origin", "*");
            return;
        }

        String requestOrigin = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ORIGIN);
        if (requestOrigin != null && allowedOrigins.contains(requestOrigin)) {
            headers.set("Access-Control-Allow-Origin", requestOrigin);
            headers.set("Vary", "Origin");
        }
    }

    private boolean isPreflightRequest(ServerWebExchange exchange) {
        return exchange.getRequest().getMethod() == HttpMethod.OPTIONS;
    }
}
