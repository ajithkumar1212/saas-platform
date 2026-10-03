package com.xyz.platform.company.dto;

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
public class CreateCompanyRequest {

    @NotNull(message = "Parent organization ID is required")
    private UUID organizationId;

    @NotBlank(message = "Company code is required")
    @Size(min = 2, max = 50, message = "Company code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Company code must be alphanumeric with dashes or underscores")
    private String companyCode;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 250, message = "Company name must be between 2 and 250 characters")
    private String companyName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
