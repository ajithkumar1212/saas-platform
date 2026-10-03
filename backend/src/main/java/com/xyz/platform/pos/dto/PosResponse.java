package com.xyz.platform.pos.dto;

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
public class PosResponse {
    private UUID posId;
    private UUID storeId;
    private UUID storeLocationId;
    private String posCode;
    private String posName;
    private String deviceIdentifier;
    private boolean isPrimary;
    private String status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
