package com.xyz.platform.pos.dto;

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
public class CreatePosRequest {

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    private UUID storeLocationId;

    @NotBlank(message = "POS code is required")
    @Size(min = 2, max = 50, message = "POS code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "POS code must be alphanumeric with dashes or underscores")
    private String posCode;

    @NotBlank(message = "POS name is required")
    @Size(min = 2, max = 150, message = "POS name must be between 2 and 150 characters")
    private String posName;

    @Size(max = 150, message = "Device identifier must not exceed 150 characters")
    private String deviceIdentifier;

    private boolean isPrimary;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
