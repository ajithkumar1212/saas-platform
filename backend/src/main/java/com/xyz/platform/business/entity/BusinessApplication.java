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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "business_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessApplication extends BaseEntity {

    @Id
    @Column(name = "business_application_id", nullable = false, updatable = false)
    private UUID businessApplicationId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "enabled_at", nullable = false)
    private Instant enabledAt;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.businessApplicationId == null) {
            this.businessApplicationId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.businessApplicationId;
    }
}
