package com.xyz.platform.warehouse.entity;

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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "warehouse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse extends BaseEntity {

    @Id
    @Column(name = "warehouse_id", nullable = false, updatable = false)
    private UUID warehouseId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "warehouse_code", length = 50, nullable = false)
    private String warehouseCode;

    @Column(name = "warehouse_name", length = 200, nullable = false)
    private String warehouseName;

    @Column(name = "warehouse_type", length = 30, nullable = false)
    private String warehouseType;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "capacity", precision = 15, scale = 2)
    private BigDecimal capacity;

    @Column(name = "capacity_unit", length = 20)
    private String capacityUnit;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.warehouseId == null) {
            this.warehouseId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.warehouseId;
    }
}
