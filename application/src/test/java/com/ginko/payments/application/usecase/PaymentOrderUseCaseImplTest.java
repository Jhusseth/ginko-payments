package com.ginko.payments.application.usecase;

import com.ginko.payments.domain.exception.ResourceNotFoundException;
import com.ginko.payments.domain.model.BusinessException;
import com.ginko.payments.domain.model.PaymentOrder;
import com.ginko.payments.domain.model.Provider;
import com.ginko.payments.domain.model.enums.OrderStatus;
import com.ginko.payments.domain.model.enums.ProviderStatus;
import com.ginko.payments.domain.port.output.PaymentOrderRepositoryPort;
import com.ginko.payments.domain.port.output.ProviderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOrderUseCaseImplTest {

    private static final UUID PROVIDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MISSING_ID = UUID.fromString("00000000-0000-0000-0000-000000000063");

    @Mock
    private PaymentOrderRepositoryPort paymentOrderRepository;

    @Mock
    private ProviderRepositoryPort providerRepository;

    private PaymentOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new PaymentOrderUseCaseImpl(paymentOrderRepository, providerRepository);
    }

    @Test
    void create_WithActiveProvider_ShouldSave() {
        Provider activeProvider = new Provider(PROVIDER_ID, "Provider A", "NIT", "e@e.com", ProviderStatus.ACTIVE);
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Mono.just(activeProvider));
        when(paymentOrderRepository.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(useCase.create(PROVIDER_ID, BigDecimal.TEN, "desc", null))
                .expectNextMatches(o -> o.getStatus() == OrderStatus.DRAFT)
                .verifyComplete();
    }

    @Test
    void create_WithInactiveProvider_ShouldThrow() {
        Provider inactiveProvider = new Provider(PROVIDER_ID, "Provider A", "NIT", "e@e.com", ProviderStatus.INACTIVE);
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Mono.just(inactiveProvider));

        StepVerifier.create(useCase.create(PROVIDER_ID, BigDecimal.TEN, "desc", null))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void create_WithNonExistentProvider_ShouldThrowNotFound() {
        when(providerRepository.findById(MISSING_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(MISSING_ID, BigDecimal.TEN, "desc", null))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void create_WithIdempotencyKeyAndExisting_ShouldReturnExisting() {
        PaymentOrder existing = new PaymentOrder(ORDER_ID, PROVIDER_ID, "Provider", BigDecimal.TEN,
                "desc", LocalDateTime.now(), null, OrderStatus.DRAFT, 0L, "key-123");
        when(paymentOrderRepository.findByIdempotencyKey("key-123")).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.create(PROVIDER_ID, BigDecimal.TEN, "desc", "key-123"))
                .expectNextMatches(o -> o.getId().equals(ORDER_ID))
                .verifyComplete();
    }

    @Test
    void create_WithIdempotencyKeyAndNew_ShouldCreateNew() {
        when(paymentOrderRepository.findByIdempotencyKey("key-123")).thenReturn(Mono.empty());
        Provider activeProvider = new Provider(PROVIDER_ID, "Provider A", "NIT", "e@e.com", ProviderStatus.ACTIVE);
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Mono.just(activeProvider));
        when(paymentOrderRepository.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(useCase.create(PROVIDER_ID, BigDecimal.TEN, "desc", "key-123"))
                .expectNextMatches(o -> o.getIdempotencyKey().equals("key-123"))
                .verifyComplete();
    }

    @Test
    void getById_WhenExists_ShouldReturn() {
        PaymentOrder order = new PaymentOrder(ORDER_ID, PROVIDER_ID, "Provider", BigDecimal.TEN,
                "desc", LocalDateTime.now(), null, OrderStatus.DRAFT, 0L, null);
        when(paymentOrderRepository.findById(ORDER_ID)).thenReturn(Mono.just(order));

        StepVerifier.create(useCase.getById(ORDER_ID))
                .expectNextMatches(o -> o.getId().equals(ORDER_ID))
                .verifyComplete();
    }

    @Test
    void getById_WhenNotExists_ShouldThrow() {
        when(paymentOrderRepository.findById(MISSING_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getById(MISSING_ID))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void transitionStatus_FromDraftToApproved_ShouldSucceed() {
        PaymentOrder order = new PaymentOrder(ORDER_ID, PROVIDER_ID, "Provider", BigDecimal.TEN,
                "desc", LocalDateTime.now(), null, OrderStatus.DRAFT, 0L, null);
        when(paymentOrderRepository.findById(ORDER_ID)).thenReturn(Mono.just(order));
        when(paymentOrderRepository.updateStatusWithVersion(ORDER_ID, OrderStatus.APPROVED, 0L))
                .thenReturn(Mono.just(true));

        StepVerifier.create(useCase.transitionStatus(ORDER_ID, OrderStatus.APPROVED))
                .expectNextMatches(o -> o.getStatus() == OrderStatus.APPROVED)
                .verifyComplete();
    }

    @Test
    void transitionStatus_InvalidTransition_ShouldThrow() {
        PaymentOrder order = new PaymentOrder(ORDER_ID, PROVIDER_ID, "Provider", BigDecimal.TEN,
                "desc", LocalDateTime.now(), null, OrderStatus.DRAFT, 0L, null);
        when(paymentOrderRepository.findById(ORDER_ID)).thenReturn(Mono.just(order));

        StepVerifier.create(useCase.transitionStatus(ORDER_ID, OrderStatus.PAID))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void transitionStatus_ConcurrencyFailure_ShouldThrow() {
        PaymentOrder order = new PaymentOrder(ORDER_ID, PROVIDER_ID, "Provider", BigDecimal.TEN,
                "desc", LocalDateTime.now(), null, OrderStatus.DRAFT, 0L, null);
        when(paymentOrderRepository.findById(ORDER_ID)).thenReturn(Mono.just(order));
        when(paymentOrderRepository.updateStatusWithVersion(ORDER_ID, OrderStatus.APPROVED, 0L))
                .thenReturn(Mono.just(false));

        StepVerifier.create(useCase.transitionStatus(ORDER_ID, OrderStatus.APPROVED))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void list_WithoutFilters_ShouldReturnAll() {
        when(paymentOrderRepository.findAll(0, 20)).thenReturn(Flux.just(
                new PaymentOrder(ORDER_ID, PROVIDER_ID, "P", BigDecimal.ONE, "d", LocalDateTime.now(), null, OrderStatus.DRAFT, 0L, null)));

        StepVerifier.create(useCase.list(null, null, 0, 20))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void list_WithStatusFilter_ShouldFilter() {
        when(paymentOrderRepository.findByStatus(OrderStatus.APPROVED, 0, 20)).thenReturn(Flux.just(
                new PaymentOrder(ORDER_ID, PROVIDER_ID, "P", BigDecimal.ONE, "d", LocalDateTime.now(), null, OrderStatus.APPROVED, 0L, null)));

        StepVerifier.create(useCase.list(OrderStatus.APPROVED, null, 0, 20))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void reportTotalPaid_WithExistingProvider_ShouldReturnSum() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Mono.just(
                new Provider(PROVIDER_ID, "P", "N", "e@e.com", ProviderStatus.ACTIVE)));
        when(paymentOrderRepository.totalPaidByProviderInRange(any(), any(), any()))
                .thenReturn(Mono.just(BigDecimal.valueOf(5000)));

        StepVerifier.create(useCase.reportTotalPaid(PROVIDER_ID, LocalDate.now().minusDays(30), LocalDate.now()))
                .expectNext(BigDecimal.valueOf(5000))
                .verifyComplete();
    }

    @Test
    void reportTotalPaid_WithNonExistentProvider_ShouldThrow() {
        when(providerRepository.findById(MISSING_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.reportTotalPaid(MISSING_ID, LocalDate.now().minusDays(30), LocalDate.now()))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void ordersAboutToExpire_ShouldReturnList() {
        when(paymentOrderRepository.findOrdersAboutToExpire(30, 0, 20)).thenReturn(Flux.just(
                new PaymentOrder(ORDER_ID, PROVIDER_ID, "P", BigDecimal.ONE, "d",
                        LocalDateTime.now().minusDays(40), null, OrderStatus.APPROVED, 0L, null)));

        StepVerifier.create(useCase.ordersAboutToExpire(0, 20))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void countAboutToExpire_ShouldReturnCount() {
        when(paymentOrderRepository.countOrdersAboutToExpire(30)).thenReturn(Mono.just(3L));

        StepVerifier.create(useCase.countAboutToExpire())
                .expectNext(3L)
                .verifyComplete();
    }
}
