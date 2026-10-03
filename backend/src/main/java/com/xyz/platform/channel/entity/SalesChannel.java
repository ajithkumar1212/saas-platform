package com.xyz.platform.channel.entity;

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
@Table(name = "channel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesChannel extends BaseEntity {

    @Id
    @Column(name = "channel_id", nullable = false, updatable = false)
    private UUID channelId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "channel_code", length = 50, nullable = false)
    private String channelCode;

    @Column(name = "channel_name", length = 150, nullable = false)
    private String channelName;

    @Column(name = "channel_type", length = 30, nullable = false)
    private String channelType;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Override
    protected void initIdIfNull() {
        if (this.channelId == null) {
            this.channelId = UUID.randomUUID();
        }
    }

    @Override
    public UUID getId() {
        return this.channelId;
    }
}
