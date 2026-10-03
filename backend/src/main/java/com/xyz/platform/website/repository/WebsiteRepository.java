package com.xyz.platform.website.repository;

import com.xyz.platform.website.entity.Website;
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
public interface WebsiteRepository extends JpaRepository<Website, UUID> {

    Optional<Website> findByStoreIdAndWebsiteCode(UUID storeId, String websiteCode);

    List<Website> findByStoreId(UUID storeId);

    Page<Website> findByStoreId(UUID storeId, Pageable pageable);

    boolean existsByStoreIdAndWebsiteCode(UUID storeId, String websiteCode);

    @Query("SELECT w FROM Website w WHERE w.storeId = :storeId AND " +
           "(LOWER(w.websiteName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.websiteCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Website> searchWebsites(@Param("storeId") UUID storeId, @Param("search") String search, Pageable pageable);
}
