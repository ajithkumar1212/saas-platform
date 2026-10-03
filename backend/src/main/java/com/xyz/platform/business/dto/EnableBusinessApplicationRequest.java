package com.xyz.platform.business.dto;

import jakarta.validation.constraints.NotNull;
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
public class EnableBusinessApplicationRequest {

    @NotNull(message = "Application ID is required")
    private UUID applicationId;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
