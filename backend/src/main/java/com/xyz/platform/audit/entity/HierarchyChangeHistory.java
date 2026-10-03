package com.xyz.platform.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
@Table(name = "hierarchy_change_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HierarchyChangeHistory implements Serializable {

    @Id
    @Column(name = "history_id", nullable = false, updatable = false)
    private UUID historyId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "entity_type", length = 30, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "old_parent_type", length = 30)
    private String oldParentType;

    @Column(name = "old_parent_id")
    private UUID oldParentId;

    @Column(name = "new_parent_type", length = 30)
    private String newParentType;

    @Column(name = "new_parent_id")
    private UUID newParentId;

    @Column(name = "change_type", length = 30, nullable = false)
    private String changeType;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_until")
    private Instant effectiveUntil;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @PrePersist
    public void onPrePersist() {
        if (this.historyId == null) {
            this.historyId = UUID.randomUUID();
        }
        if (this.changedAt == null) {
            this.changedAt = Instant.now();
        }
        if (this.effectiveFrom == null) {
            this.effectiveFrom = Instant.now();
        }
    }
}
