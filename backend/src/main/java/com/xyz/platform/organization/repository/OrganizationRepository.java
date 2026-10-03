package com.xyz.platform.organization.repository;

import com.xyz.platform.organization.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByOrganizationCode(String organizationCode);

    List<Organization> findByTenantId(UUID tenantId);

    Page<Organization> findByTenantId(UUID tenantId, Pageable pageable);

    boolean existsByOrganizationCode(String organizationCode);

    @Query("SELECT o FROM Organization o WHERE o.tenantId = :tenantId AND " +
           "(LOWER(o.organizationName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(o.organizationCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Organization> searchOrganizations(@Param("tenantId") UUID tenantId, @Param("search") String search, Pageable pageable);
}
