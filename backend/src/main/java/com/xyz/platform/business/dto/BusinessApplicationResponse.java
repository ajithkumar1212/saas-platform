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
public class BusinessApplicationResponse {
    private UUID businessApplicationId;
    private UUID businessId;
    private UUID applicationId;
    private String applicationCode;
    private String applicationName;
    private String status;
    private Instant enabledAt;
    private String description;
    private Instant createdAt;
}
