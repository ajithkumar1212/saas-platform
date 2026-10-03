package com.xyz.platform.channel.repository;

import com.xyz.platform.channel.entity.StoreChannel;
import com.xyz.platform.channel.entity.StoreChannelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoreChannelRepository extends JpaRepository<StoreChannel, StoreChannelId> {

    List<StoreChannel> findByIdStoreId(UUID storeId);

    List<StoreChannel> findByIdChannelId(UUID channelId);
}
