package com.xyz.platform.website.entity;

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
@Table(name = "website")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Website extends BaseEntity {

    @Id
    @Column(name = "website_id", nullable = false, updatable = false)
    private UUID websiteId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "website_code", length = 50, nullable = false)
    private String websiteCode;

    @Column(name = "website_name", length = 200, nullable = false)
    private String websiteName;

    @Column(name = "website_type", length = 30, nullable = false)
    private String websiteType;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.websiteId == null) {
            this.websiteId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.websiteId;
    }
}
