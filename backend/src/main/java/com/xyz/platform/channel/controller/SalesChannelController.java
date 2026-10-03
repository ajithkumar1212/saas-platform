package com.xyz.platform.channel.controller;

import com.xyz.platform.channel.dto.ChannelResponse;
import com.xyz.platform.channel.dto.CreateChannelRequest;
import com.xyz.platform.channel.dto.StoreChannelResponse;
import com.xyz.platform.channel.service.SalesChannelService;
import com.xyz.platform.shared.dto.ApiResponse;
import com.xyz.platform.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Sales Channel Management", description = "Endpoints for managing sales channels and store associations")
public class SalesChannelController {

    private final SalesChannelService salesChannelService;

    public SalesChannelController(SalesChannelService salesChannelService) {
        this.salesChannelService = salesChannelService;
    }

    @PostMapping("/api/v1/channels")
    @Operation(summary = "Create a Sales Channel for an authorized Business")
    public ResponseEntity<ApiResponse<ChannelResponse>> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        ChannelResponse response = salesChannelService.createChannel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Sales Channel created successfully"));
    }

    @GetMapping("/api/v1/businesses/{businessId}/channels")
    @Operation(summary = "List authorized Channels for a Business with search and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ChannelResponse>>> listChannelsByBusiness(
            @PathVariable UUID businessId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<ChannelResponse> response = salesChannelService.listChannelsByBusiness(businessId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Sales Channels retrieved successfully"));
    }

    @GetMapping("/api/v1/channels/{channelId}")
    @Operation(summary = "Read an authorized Channel by ID")
    public ResponseEntity<ApiResponse<ChannelResponse>> getChannel(@PathVariable UUID channelId) {
        ChannelResponse response = salesChannelService.getChannelById(channelId);
        return ResponseEntity.ok(ApiResponse.success(response, "Sales Channel retrieved successfully"));
    }

    @PostMapping("/api/v1/stores/{storeId}/channels/{channelId}")
    @Operation(summary = "Associate a Store with a Channel (must share the same Business)")
    public ResponseEntity<ApiResponse<StoreChannelResponse>> associateStoreChannel(
            @PathVariable UUID storeId,
            @PathVariable UUID channelId,
            @RequestParam(defaultValue = "false") boolean isPrimary,
            @RequestParam(required = false) String description) {
        StoreChannelResponse response = salesChannelService.associateStoreChannel(storeId, channelId, isPrimary, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Store associated with Sales Channel successfully"));
    }
}
