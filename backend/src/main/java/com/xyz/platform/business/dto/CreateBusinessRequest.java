package com.xyz.platform.business.dto;

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
public class CreateBusinessRequest {

    @NotNull(message = "Parent company ID is required")
    private UUID companyId;

    @NotBlank(message = "Business code is required")
    @Size(min = 2, max = 50, message = "Business code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Business code must be alphanumeric with dashes or underscores")
    private String businessCode;

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 250, message = "Business name must be between 2 and 250 characters")
    private String businessName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
