package com.xyz.platform.brand.dto;

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
public class CreateBrandRequest {

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    @NotBlank(message = "Brand code is required")
    @Size(min = 2, max = 50, message = "Brand code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Brand code must be alphanumeric with dashes or underscores")
    private String brandCode;

    @NotBlank(message = "Brand name is required")
    @Size(min = 2, max = 200, message = "Brand name must be between 2 and 200 characters")
    private String brandName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
