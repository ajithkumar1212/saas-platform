package com.xyz.platform.controlplane.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCatalogResponse {
    private UUID applicationId;
    private String applicationCode;
    private String applicationName;
    private String status;
    private String description;
    private Instant createdAt;
}
