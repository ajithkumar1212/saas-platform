package com.xyz.platform.shared.reference.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reference_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenceValue implements Serializable {

    @Id
    @Column(name = "reference_value_id", nullable = false, updatable = false)
    private UUID referenceValueId;

    @Column(name = "reference_type_id", nullable = false)
    private UUID referenceTypeId;

    @Column(name = "value_code", length = 50, nullable = false)
    private String valueCode;

    @Column(name = "value_name", length = 100, nullable = false)
    private String valueName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "is_system_value", nullable = false)
    private boolean isSystemValue;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
