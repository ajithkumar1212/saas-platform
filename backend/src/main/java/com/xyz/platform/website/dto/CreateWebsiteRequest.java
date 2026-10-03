package com.xyz.platform.website.dto;

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
public class CreateWebsiteRequest {

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    @NotBlank(message = "Website code is required")
    @Size(min = 2, max = 50, message = "Website code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Website code must be alphanumeric with dashes or underscores")
    private String websiteCode;

    @NotBlank(message = "Website name is required")
    @Size(min = 2, max = 200, message = "Website name must be between 2 and 200 characters")
    private String websiteName;

    @NotBlank(message = "Website type is required")
    @Size(max = 30, message = "Website type must not exceed 30 characters")
    private String websiteType;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
