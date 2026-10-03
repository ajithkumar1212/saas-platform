package com.xyz.platform.tenancy.context;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Thread-safe context holder for the current authenticated tenant's identity and isolated schema.
 */
public final class TenantContext {

    private static final ThreadLocal<TenantInfo> CURRENT_TENANT = new ThreadLocal<>();
    public static final String DEFAULT_SCHEMA = "public";

    @Getter
    @Builder
    public static class TenantInfo {
        private final UUID tenantId;
        private final String tenantCode;
        private final String schemaName;
    }

    private TenantContext() {}

    public static void setTenant(UUID tenantId, String tenantCode, String schemaName) {
        CURRENT_TENANT.set(TenantInfo.builder()
                .tenantId(tenantId)
                .tenantCode(tenantCode)
                .schemaName(schemaName)
                .build());
    }

    public static void setTenant(TenantInfo info) {
        CURRENT_TENANT.set(info);
    }

    public static TenantInfo getTenant() {
        return CURRENT_TENANT.get();
    }

    public static UUID getTenantId() {
        TenantInfo info = CURRENT_TENANT.get();
        return info != null ? info.getTenantId() : null;
    }

    public static String getTenantCode() {
        TenantInfo info = CURRENT_TENANT.get();
        return info != null ? info.getTenantCode() : null;
    }

    public static String getCurrentSchema() {
        TenantInfo info = CURRENT_TENANT.get();
        return (info != null && info.getSchemaName() != null) ? info.getSchemaName() : DEFAULT_SCHEMA;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static <T> T executeInTenantContext(TenantInfo tenantInfo, Callable<T> task) throws Exception {
        TenantInfo previous = CURRENT_TENANT.get();
        try {
            CURRENT_TENANT.set(tenantInfo);
            return task.call();
        } finally {
            if (previous != null) {
                CURRENT_TENANT.set(previous);
            } else {
                CURRENT_TENANT.remove();
            }
        }
    }
}
