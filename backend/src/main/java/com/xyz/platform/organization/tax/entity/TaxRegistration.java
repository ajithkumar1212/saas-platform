package com.xyz.platform.organization.tax.entity;

import com.xyz.platform.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tax_registration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRegistration extends BaseEntity {

    @Id
    @Column(name = "tax_registration_id", nullable = false, updatable = false)
    private UUID taxRegistrationId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "registration_type", length = 30, nullable = false)
    private String registrationType;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @Column(name = "country_id")
    private UUID countryId;

    @Column(name = "state_id")
    private UUID stateId;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.taxRegistrationId == null) {
            this.taxRegistrationId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.taxRegistrationId;
    }
}
