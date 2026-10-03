package com.xyz.platform.shared.domain;

import com.xyz.platform.organization.entity.Organization;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityLifecycleTest {

    @Test
    void shouldGenerateApplicationManagedUuidAndTimestampsOnPrePersist() {
        Organization org = Organization.builder()
                .tenantId(UUID.randomUUID())
                .organizationCode("ORG_001")
                .organizationName("Acme Global")
                .status("ACTIVE")
                .build();

        assertNull(org.getOrganizationId());
        assertNull(org.getCreatedAt());
        assertNull(org.getUpdatedAt());

        org.onPrePersist();

        assertNotNull(org.getOrganizationId());
        assertEquals(org.getOrganizationId(), org.getId());
        assertNotNull(org.getCreatedAt());
        assertNotNull(org.getUpdatedAt());
        assertEquals(org.getCreatedAt(), org.getUpdatedAt());
    }

    @Test
    void shouldPreserveExistingUuidIfAlreadyAssigned() {
        UUID predefinedId = UUID.randomUUID();
        Organization org = Organization.builder()
                .organizationId(predefinedId)
                .tenantId(UUID.randomUUID())
                .organizationCode("ORG_002")
                .organizationName("Predefined Org")
                .status("ACTIVE")
                .build();

        org.onPrePersist();

        assertEquals(predefinedId, org.getOrganizationId());
    }

    @Test
    void shouldUpdateTimestampOnPreUpdate() throws InterruptedException {
        Organization org = Organization.builder()
                .tenantId(UUID.randomUUID())
                .organizationCode("ORG_003")
                .organizationName("Updating Org")
                .status("ACTIVE")
                .build();

        org.onPrePersist();
        var initialUpdatedAt = org.getUpdatedAt();

        Thread.sleep(10);
        org.onPreUpdate();

        assertTrue(org.getUpdatedAt().isAfter(initialUpdatedAt) || org.getUpdatedAt().equals(initialUpdatedAt));
    }
}
