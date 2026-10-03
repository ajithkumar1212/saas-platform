package com.xyz.platform.business.repository;

import com.xyz.platform.business.entity.Business;
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
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findByCompanyIdAndBusinessCode(UUID companyId, String businessCode);

    List<Business> findByCompanyId(UUID companyId);

    Page<Business> findByCompanyId(UUID companyId, Pageable pageable);

    List<Business> findByOrganizationId(UUID organizationId);

    boolean existsByCompanyIdAndBusinessCode(UUID companyId, String businessCode);

    @Query("SELECT b FROM Business b WHERE b.companyId = :companyId AND " +
           "(LOWER(b.businessName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.businessCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Business> searchBusinesses(@Param("companyId") UUID companyId, @Param("search") String search, Pageable pageable);
}
