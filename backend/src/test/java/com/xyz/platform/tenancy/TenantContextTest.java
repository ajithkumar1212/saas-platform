package com.xyz.platform.tenancy;

import com.xyz.platform.tenancy.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @BeforeEach
    @AfterEach
    void cleanUp() {
        TenantContext.clear();
    }

    @Test
    void shouldDefaultToPublicSchemaWhenNoTenantSet() {
        assertEquals("public", TenantContext.getCurrentSchema());
        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.getTenantCode());
    }

    @Test
    void shouldStoreAndRetrieveTenantContext() {
        UUID tenantId = UUID.randomUUID();
        String tenantCode = "acme_corp";
        String schemaName = "tenant_acme_corp";

        TenantContext.setTenant(tenantId, tenantCode, schemaName);

        assertEquals(tenantId, TenantContext.getTenantId());
        assertEquals(tenantCode, TenantContext.getTenantCode());
        assertEquals(schemaName, TenantContext.getCurrentSchema());
    }

    @Test
    void shouldClearTenantContext() {
        TenantContext.setTenant(UUID.randomUUID(), "test_code", "tenant_test");
        TenantContext.clear();

        assertEquals("public", TenantContext.getCurrentSchema());
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void shouldExecuteInTemporaryTenantContextAndRestorePrevious() throws Exception {
        TenantContext.TenantInfo initial = TenantContext.TenantInfo.builder()
                .tenantId(UUID.randomUUID())
                .tenantCode("initial")
                .schemaName("tenant_initial")
                .build();

        TenantContext.setTenant(initial);

        TenantContext.TenantInfo temp = TenantContext.TenantInfo.builder()
                .tenantId(UUID.randomUUID())
                .tenantCode("temp")
                .schemaName("tenant_temp")
                .build();

        String result = TenantContext.executeInTenantContext(temp, () -> {
            assertEquals("tenant_temp", TenantContext.getCurrentSchema());
            assertEquals("temp", TenantContext.getTenantCode());
            return "SUCCESS";
        });

        assertEquals("SUCCESS", result);
        assertEquals("tenant_initial", TenantContext.getCurrentSchema());
        assertEquals("initial", TenantContext.getTenantCode());
    }
}
