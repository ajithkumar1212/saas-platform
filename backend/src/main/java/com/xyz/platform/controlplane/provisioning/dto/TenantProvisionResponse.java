package com.xyz.platform.controlplane.provisioning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantProvisionResponse {
    private UUID tenantId;
    private String tenantCode;
    private String tenantName;
    private String schemaName;
    private String status;
    private String provisioningStatus;
    private Instant createdAt;
}
