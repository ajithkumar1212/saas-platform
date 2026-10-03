package com.xyz.platform.controlplane.provisioning.dto;

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
public class TenantProvisionRequest {

    @NotBlank(message = "Tenant code is required")
    @Size(min = 3, max = 30, message = "Tenant code must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "Tenant code must contain only lowercase alphanumeric characters, dashes, or underscores")
    private String tenantCode;

    @NotBlank(message = "Tenant name is required")
    @Size(min = 2, max = 200, message = "Tenant name must be between 2 and 200 characters")
    private String tenantName;
}
