package com.xyz.platform.business.dto;

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
public class BusinessResponse {
    private UUID businessId;
    private UUID companyId;
    private UUID organizationId;
    private String businessCode;
    private String businessName;
    private String status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
