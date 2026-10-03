package com.xyz.platform.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWarehouseRequest {

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    @NotBlank(message = "Warehouse code is required")
    @Size(min = 2, max = 50, message = "Warehouse code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Warehouse code must be alphanumeric with dashes or underscores")
    private String warehouseCode;

    @NotBlank(message = "Warehouse name is required")
    @Size(min = 2, max = 200, message = "Warehouse name must be between 2 and 200 characters")
    private String warehouseName;

    @NotBlank(message = "Warehouse type is required")
    @Size(max = 30, message = "Warehouse type must not exceed 30 characters")
    private String warehouseType;

    private boolean isPrimary;

    private BigDecimal capacity;

    @Size(max = 20, message = "Capacity unit must not exceed 20 characters")
    private String capacityUnit;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
