package com.xyz.platform.pos.repository;

import com.xyz.platform.pos.entity.PosTerminal;
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
public interface PosTerminalRepository extends JpaRepository<PosTerminal, UUID> {

    Optional<PosTerminal> findByStoreIdAndPosCode(UUID storeId, String posCode);

    List<PosTerminal> findByStoreId(UUID storeId);

    Page<PosTerminal> findByStoreId(UUID storeId, Pageable pageable);

    boolean existsByStoreIdAndPosCode(UUID storeId, String posCode);

    @Query("SELECT p FROM PosTerminal p WHERE p.storeId = :storeId AND " +
           "(LOWER(p.posName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.posCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PosTerminal> searchPos(@Param("storeId") UUID storeId, @Param("search") String search, Pageable pageable);
}
