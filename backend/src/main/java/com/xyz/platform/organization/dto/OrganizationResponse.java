package com.xyz.platform.organization.dto;

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
public class OrganizationResponse {
    private UUID organizationId;
    private UUID tenantId;
    private String organizationCode;
    private String organizationName;
    private String status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
