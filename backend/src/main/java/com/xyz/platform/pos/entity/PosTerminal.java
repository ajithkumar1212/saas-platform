package com.xyz.platform.pos.entity;

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
@Table(name = "pos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosTerminal extends BaseEntity {

    @Id
    @Column(name = "pos_id", nullable = false, updatable = false)
    private UUID posId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "store_location_id")
    private UUID storeLocationId;

    @Column(name = "pos_code", length = 50, nullable = false)
    private String posCode;

    @Column(name = "pos_name", length = 150, nullable = false)
    private String posName;

    @Column(name = "device_identifier", length = 150)
    private String deviceIdentifier;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.posId == null) {
            this.posId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.posId;
    }
}
