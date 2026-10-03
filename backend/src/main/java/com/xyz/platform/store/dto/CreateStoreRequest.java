package com.xyz.platform.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreRequest {

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    private UUID brandId;

    @NotBlank(message = "Store code is required")
    @Size(min = 2, max = 50, message = "Store code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Store code must be alphanumeric with dashes or underscores")
    private String storeCode;

    @NotBlank(message = "Store name is required")
    @Size(min = 2, max = 200, message = "Store name must be between 2 and 200 characters")
    private String storeName;

    @NotBlank(message = "Store type is required")
    @Size(max = 30, message = "Store type must not exceed 30 characters")
    private String storeType;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
