package com.xyz.platform.organization.tax.dto;

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
public class BusinessTaxRegistrationResponse {
    private UUID businessId;
    private UUID taxRegistrationId;
    private UUID organizationId;
    private String registrationType;
    private String registrationNumber;
    private String gstin;
    private boolean isPrimary;
    private String status;
    private Instant validFrom;
    private Instant validUntil;
    private String description;
}
