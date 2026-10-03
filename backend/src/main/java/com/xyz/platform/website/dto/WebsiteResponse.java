package com.xyz.platform.website.dto;

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
public class WebsiteResponse {
    private UUID websiteId;
    private UUID storeId;
    private String websiteCode;
    private String websiteName;
    private String websiteType;
    private String status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
