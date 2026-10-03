package com.xyz.platform.brand.entity;

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
@Table(name = "brand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand extends BaseEntity {

    @Id
    @Column(name = "brand_id", nullable = false, updatable = false)
    private UUID brandId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "brand_code", length = 50, nullable = false)
    private String brandCode;

    @Column(name = "brand_name", length = 200, nullable = false)
    private String brandName;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.brandId == null) {
            this.brandId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.brandId;
    }
}
