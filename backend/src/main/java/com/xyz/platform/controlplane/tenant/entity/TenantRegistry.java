package com.xyz.platform.controlplane.tenant.entity;

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
@Table(name = "tenant_registry", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantRegistry extends BaseEntity {

    @Id
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "tenant_code", length = 50, nullable = false, unique = true)
    private String tenantCode;

    @Column(name = "tenant_name", length = 200, nullable = false)
    private String tenantName;

    @Column(name = "schema_name", length = 63, nullable = false, unique = true)
    private String schemaName;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "provisioning_status", length = 30, nullable = false)
    private String provisioningStatus;

    @Override
    protected void initIdIfNull() {
        if (this.tenantId == null) {
            this.tenantId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.tenantId;
    }
}
