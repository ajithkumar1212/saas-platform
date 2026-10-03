package com.xyz.platform.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {
    private UUID warehouseId;
    private UUID storeId;
    private String warehouseCode;
    private String warehouseName;
    private String warehouseType;
    private String status;
    private boolean isPrimary;
    private BigDecimal capacity;
    private String capacityUnit;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
