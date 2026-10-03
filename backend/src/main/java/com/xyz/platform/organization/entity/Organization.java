package com.xyz.platform.organization.entity;

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
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Id
    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "organization_code", length = 50, nullable = false)
    private String organizationCode;

    @Column(name = "organization_name", length = 250, nullable = false)
    private String organizationName;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.organizationId == null) {
            this.organizationId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.organizationId;
    }
}
