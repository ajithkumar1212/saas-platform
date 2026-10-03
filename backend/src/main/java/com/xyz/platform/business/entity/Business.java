package com.xyz.platform.business.entity;

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

import java.util.UUID;

@Entity
@Table(name = "business")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business extends BaseEntity {

    @Id
    @Column(name = "business_id", nullable = false, updatable = false)
    private UUID businessId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "business_code", length = 50, nullable = false)
    private String businessCode;

    @Column(name = "business_name", length = 250, nullable = false)
    private String businessName;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.businessId == null) {
            this.businessId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.businessId;
    }
}
