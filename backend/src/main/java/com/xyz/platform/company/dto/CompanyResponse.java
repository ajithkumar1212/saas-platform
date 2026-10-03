package com.xyz.platform.company.dto;

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
public class CompanyResponse {
    private UUID companyId;
    private UUID organizationId;
    private String companyCode;
    private String companyName;
    private String status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
