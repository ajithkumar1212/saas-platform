package com.xyz.platform.warehouse.repository;

import com.xyz.platform.warehouse.entity.Warehouse;
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
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Optional<Warehouse> findByStoreIdAndWarehouseCode(UUID storeId, String warehouseCode);

    List<Warehouse> findByStoreId(UUID storeId);

    Page<Warehouse> findByStoreId(UUID storeId, Pageable pageable);

    boolean existsByStoreIdAndWarehouseCode(UUID storeId, String warehouseCode);

    @Query("SELECT w FROM Warehouse w WHERE w.storeId = :storeId AND " +
           "(LOWER(w.warehouseName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.warehouseCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warehouse> searchWarehouses(@Param("storeId") UUID storeId, @Param("search") String search, Pageable pageable);
}
