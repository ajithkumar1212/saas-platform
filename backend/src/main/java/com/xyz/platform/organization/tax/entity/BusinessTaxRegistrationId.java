package com.xyz.platform.organization.tax.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessTaxRegistrationId implements Serializable {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "tax_registration_id", nullable = false)
    private UUID taxRegistrationId;
}
