package com.xyz.platform.channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreChannelResponse {
    private UUID storeId;
    private UUID channelId;
    private String status;
    private boolean isPrimary;
    private String description;
    private Instant createdAt;
}
