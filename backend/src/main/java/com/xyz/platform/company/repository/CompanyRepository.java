package com.xyz.platform.company.repository;

import com.xyz.platform.company.entity.Company;
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
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByOrganizationIdAndCompanyCode(UUID organizationId, String companyCode);

    List<Company> findByOrganizationId(UUID organizationId);

    Page<Company> findByOrganizationId(UUID organizationId, Pageable pageable);

    boolean existsByOrganizationIdAndCompanyCode(UUID organizationId, String companyCode);

    @Query("SELECT c FROM Company c WHERE c.organizationId = :organizationId AND " +
           "(LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.companyCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Company> searchCompanies(@Param("organizationId") UUID organizationId, @Param("search") String search, Pageable pageable);
}
