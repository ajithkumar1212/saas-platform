package com.xyz.platform.store.entity;

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
@Table(name = "store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Id
    @Column(name = "store_id", nullable = false, updatable = false)
    private UUID storeId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "brand_id")
    private UUID brandId;

    @Column(name = "store_code", length = 50, nullable = false)
    private String storeCode;

    @Column(name = "store_name", length = 200, nullable = false)
    private String storeName;

    @Column(name = "store_type", length = 30, nullable = false)
    private String storeType;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.storeId == null) {
            this.storeId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.storeId;
    }
}
