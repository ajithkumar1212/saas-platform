package com.xyz.platform.brand.dto;

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
public class BrandResponse {
    private UUID brandId;
    private UUID businessId;
    private String brandCode;
    private String brandName;
    private String status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
