package com.xyz.platform.identity.repository;

import com.xyz.platform.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByRoleCode(String roleCode);

    List<Role> findByTenantId(UUID tenantId);

    boolean existsByRoleCode(String roleCode);
}
