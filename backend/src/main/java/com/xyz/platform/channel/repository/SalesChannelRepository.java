package com.xyz.platform.channel.repository;

import com.xyz.platform.channel.entity.SalesChannel;
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
public interface SalesChannelRepository extends JpaRepository<SalesChannel, UUID> {

    Optional<SalesChannel> findByBusinessIdAndChannelCode(UUID businessId, String channelCode);

    List<SalesChannel> findByBusinessId(UUID businessId);

    Page<SalesChannel> findByBusinessId(UUID businessId, Pageable pageable);

    boolean existsByBusinessIdAndChannelCode(UUID businessId, String channelCode);

    @Query("SELECT c FROM SalesChannel c WHERE c.businessId = :businessId AND " +
           "(LOWER(c.channelName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.channelCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SalesChannel> searchChannels(@Param("businessId") UUID businessId, @Param("search") String search, Pageable pageable);
}
