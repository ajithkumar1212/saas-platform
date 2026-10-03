package com.xyz.platform.audit.repository;

import com.xyz.platform.audit.entity.HierarchyChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HierarchyChangeHistoryRepository extends JpaRepository<HierarchyChangeHistory, UUID> {

    List<HierarchyChangeHistory> findByTenantIdAndEntityId(UUID tenantId, UUID entityId);

    List<HierarchyChangeHistory> findByTenantIdAndEntityType(UUID tenantId, String entityType);
}
