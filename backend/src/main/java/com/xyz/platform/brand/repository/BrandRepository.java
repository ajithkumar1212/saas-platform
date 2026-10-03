package com.xyz.platform.brand.repository;

import com.xyz.platform.brand.entity.Brand;
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
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findByBusinessIdAndBrandCode(UUID businessId, String brandCode);

    List<Brand> findByBusinessId(UUID businessId);

    Page<Brand> findByBusinessId(UUID businessId, Pageable pageable);

    boolean existsByBusinessIdAndBrandCode(UUID businessId, String brandCode);

    @Query("SELECT b FROM Brand b WHERE b.businessId = :businessId AND " +
           "(LOWER(b.brandName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.brandCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Brand> searchBrands(@Param("businessId") UUID businessId, @Param("search") String search, Pageable pageable);
}
