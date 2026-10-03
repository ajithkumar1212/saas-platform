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
public class ChannelResponse {
    private UUID channelId;
    private UUID businessId;
    private String channelCode;
    private String channelName;
    private String channelType;
    private String status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
