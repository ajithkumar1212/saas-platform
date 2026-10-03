package com.xyz.platform.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization code is required")
    @Size(min = 2, max = 50, message = "Organization code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Organization code must be alphanumeric with dashes or underscores")
    private String organizationCode;

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 250, message = "Organization name must be between 2 and 250 characters")
    private String organizationName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
