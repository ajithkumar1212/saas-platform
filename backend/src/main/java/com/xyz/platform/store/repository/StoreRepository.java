package com.xyz.platform.store.repository;

import com.xyz.platform.store.entity.Store;
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
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByBusinessIdAndStoreCode(UUID businessId, String storeCode);

    List<Store> findByBusinessId(UUID businessId);

    Page<Store> findByBusinessId(UUID businessId, Pageable pageable);

    List<Store> findByBrandId(UUID brandId);

    boolean existsByBusinessIdAndStoreCode(UUID businessId, String storeCode);

    @Query("SELECT s FROM Store s WHERE s.businessId = :businessId AND " +
           "(LOWER(s.storeName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.storeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Store> searchStores(@Param("businessId") UUID businessId, @Param("search") String search, Pageable pageable);
}
