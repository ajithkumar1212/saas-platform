package com.xyz.platform.controlplane.tenant.repository;

import com.xyz.platform.controlplane.tenant.entity.TenantRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRegistryRepository extends JpaRepository<TenantRegistry, UUID> {

    Optional<TenantRegistry> findByTenantCode(String tenantCode);

    Optional<TenantRegistry> findBySchemaName(String schemaName);

    boolean existsByTenantCode(String tenantCode);

    boolean existsBySchemaName(String schemaName);
}
