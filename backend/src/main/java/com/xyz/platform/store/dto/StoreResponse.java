package com.xyz.platform.store.dto;

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
public class StoreResponse {
    private UUID storeId;
    private UUID businessId;
    private UUID brandId;
    private String storeCode;
    private String storeName;
    private String storeType;
    private String status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
