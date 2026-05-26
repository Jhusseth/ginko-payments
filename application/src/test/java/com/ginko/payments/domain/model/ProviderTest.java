package com.ginko.payments.domain.model;

import com.ginko.payments.domain.model.enums.ProviderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderTest {

    @Test
    void create_ShouldBeActive() {
        Provider p = new Provider("Test Provider", "900123456-7", "test@test.com");
        assertEquals("Test Provider", p.getName());
        assertEquals("900123456-7", p.getTaxIdentificationNumber());
        assertEquals("test@test.com", p.getEmail());
        assertEquals(ProviderStatus.ACTIVE, p.getStatus());
        assertTrue(p.isActive());
    }

    @Test
    void withId_ShouldSetId() {
        Provider p = new Provider("Test", "NIT", "e@e.com");
        Provider withId = p.withId(42L);
        assertNull(p.getId());
        assertEquals(42L, withId.getId());
    }

    @Test
    void withStatus_ShouldChangeStatus() {
        Provider p = new Provider("Test", "NIT", "e@e.com");
        Provider inactive = p.withStatus(ProviderStatus.INACTIVE);
        assertEquals(ProviderStatus.INACTIVE, inactive.getStatus());
        assertFalse(inactive.isActive());
    }

    @Test
    void withUpdatedData_ShouldUpdateFields() {
        Provider p = new Provider(1L, "Old Name", "NIT-OLD", "old@e.com", ProviderStatus.ACTIVE);
        Provider updated = p.withUpdatedData("New Name", "NIT-NEW", "new@e.com");
        assertEquals(1L, updated.getId());
        assertEquals("New Name", updated.getName());
        assertEquals("NIT-NEW", updated.getTaxIdentificationNumber());
        assertEquals("new@e.com", updated.getEmail());
        assertEquals(ProviderStatus.ACTIVE, updated.getStatus());
    }

    @Test
    void isActive_WhenInactive_ReturnsFalse() {
        Provider p = new Provider(1L, "Test", "NIT", "e@e.com", ProviderStatus.INACTIVE);
        assertFalse(p.isActive());
    }
}
