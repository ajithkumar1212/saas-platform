package com.xyz.platform.controlplane.application.entity;

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
@Table(name = "application", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCatalog extends BaseEntity {

    @Id
    @Column(name = "application_id", nullable = false, updatable = false)
    private UUID applicationId;

    @Column(name = "application_code", length = 50, nullable = false, unique = true)
    private String applicationCode;

    @Column(name = "application_name", length = 150, nullable = false)
    private String applicationName;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.applicationId == null) {
            this.applicationId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.applicationId;
    }
}
