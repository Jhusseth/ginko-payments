package com.ginko.payments.infrastructure.config;

import com.ginko.payments.infrastructure.adapter.output.persistence.repository.ReactivePaymentOrderRepository;
import com.ginko.payments.infrastructure.adapter.output.persistence.repository.ReactiveProviderRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterBinder providerCountMetric(ReactiveProviderRepository providerRepository) {
        return registry -> Gauge.builder("providers.count", providerRepository,
                        repo -> safeBlock(repo.count()))
                .description("Total number of providers")
                .register(registry);
    }

    @Bean
    public MeterBinder orderCountMetric(ReactivePaymentOrderRepository orderRepository) {
        return registry -> Gauge.builder("payment.orders.count", orderRepository,
                        repo -> safeBlock(repo.count()))
                .description("Total number of payment orders")
                .register(registry);
    }

    private static double safeBlock(Mono<Long> mono) {
        try {
            Long result = mono.block();
            return result != null ? result.doubleValue() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
