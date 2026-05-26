package com.ginko.payments.domain.model;

import com.ginko.payments.domain.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentOrderTest {

    @Test
    void create_ShouldBeDraft() {
        PaymentOrder order = new PaymentOrder(1L, "Provider", BigDecimal.TEN, "Test");
        assertEquals(OrderStatus.DRAFT, order.getStatus());
    }

    @Test
    void transition_FromDraftToApproved_ShouldSucceed() {
        PaymentOrder order = new PaymentOrder(1L, "Provider", BigDecimal.TEN, "Test");
        PaymentOrder updated = order.transitionTo(OrderStatus.APPROVED);
        assertEquals(OrderStatus.APPROVED, updated.getStatus());
    }

    @Test
    void transition_FromDraftToRejected_ShouldSucceed() {
        PaymentOrder order = new PaymentOrder(1L, "Provider", BigDecimal.TEN, "Test");
        PaymentOrder updated = order.transitionTo(OrderStatus.REJECTED);
        assertEquals(OrderStatus.REJECTED, updated.getStatus());
    }

    @Test
    void transition_FromApprovedToPaid_ShouldSucceed() {
        PaymentOrder order = new PaymentOrder(1L, null, "Provider", BigDecimal.TEN,
                "Test", java.time.LocalDateTime.now(), null, OrderStatus.APPROVED, 0L, null);
        PaymentOrder updated = order.transitionTo(OrderStatus.PAID);
        assertEquals(OrderStatus.PAID, updated.getStatus());
    }

    @Test
    void transition_FromDraftToPaid_ShouldThrow() {
        PaymentOrder order = new PaymentOrder(1L, "Provider", BigDecimal.TEN, "Test");
        assertThrows(BusinessException.class, () -> order.transitionTo(OrderStatus.PAID));
    }

    @Test
    void transition_FromApprovedToDraft_ShouldThrow() {
        PaymentOrder order = new PaymentOrder(1L, null, "Provider", BigDecimal.TEN,
                "Test", java.time.LocalDateTime.now(), null, OrderStatus.APPROVED, 0L, null);
        assertThrows(BusinessException.class, () -> order.transitionTo(OrderStatus.DRAFT));
    }

    @Test
    void transition_FromPaid_ShouldThrow() {
        PaymentOrder order = new PaymentOrder(1L, null, "Provider", BigDecimal.TEN,
                "Test", java.time.LocalDateTime.now(), null, OrderStatus.PAID, 0L, null);
        assertThrows(BusinessException.class, () -> order.transitionTo(OrderStatus.APPROVED));
    }

    @Test
    void transition_FromRejected_ShouldThrow() {
        PaymentOrder order = new PaymentOrder(1L, null, "Provider", BigDecimal.TEN,
                "Test", java.time.LocalDateTime.now(), null, OrderStatus.REJECTED, 0L, null);
        assertThrows(BusinessException.class, () -> order.transitionTo(OrderStatus.APPROVED));
    }

    @Test
    void withId_ShouldSetId() {
        PaymentOrder order = new PaymentOrder(1L, "Provider", BigDecimal.TEN, "Test");
        PaymentOrder withId = order.withId(99L);
        assertNull(order.getId());
        assertEquals(99L, withId.getId());
    }

    @Test
    void withVersion_ShouldSetVersion() {
        PaymentOrder order = new PaymentOrder(1L, "Provider", BigDecimal.TEN, "Test");
        PaymentOrder withVer = order.withVersion(5L);
        assertEquals(5L, withVer.getVersion());
    }

    @Test
    void withIdempotencyKey_ShouldSetKey() {
        PaymentOrder order = new PaymentOrder(1L, "Provider", BigDecimal.TEN, "Test");
        PaymentOrder withKey = order.withIdempotencyKey("key-123");
        assertEquals("key-123", withKey.getIdempotencyKey());
    }

    @Test
    void isAboutToExpire_WhenApprovedAndOld_ReturnsTrue() {
        PaymentOrder order = new PaymentOrder(1L, null, "Provider", BigDecimal.TEN,
                "Test", java.time.LocalDateTime.now().minusDays(40), null, OrderStatus.APPROVED, 0L, null);
        assertTrue(order.isAboutToExpire(30));
    }

    @Test
    void isAboutToExpire_WhenDraft_ReturnsFalse() {
        PaymentOrder order = new PaymentOrder(1L, "Provider", BigDecimal.TEN, "Test");
        assertFalse(order.isAboutToExpire(30));
    }

    @Test
    void isAboutToExpire_WhenRecent_ReturnsFalse() {
        PaymentOrder order = new PaymentOrder(1L, null, "Provider", BigDecimal.TEN,
                "Test", java.time.LocalDateTime.now(), null, OrderStatus.APPROVED, 0L, null);
        assertFalse(order.isAboutToExpire(30));
    }
}
