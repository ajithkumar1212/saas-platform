package com.xyz.platform.website.controller;

import com.xyz.platform.shared.dto.ApiResponse;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.website.dto.CreateWebsiteRequest;
import com.xyz.platform.website.dto.WebsiteResponse;
import com.xyz.platform.website.service.WebsiteService;
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
@Tag(name = "Website Management", description = "Endpoints for managing digital website channels and domains")
public class WebsiteController {

    private final WebsiteService websiteService;

    public WebsiteController(WebsiteService websiteService) {
        this.websiteService = websiteService;
    }

    @PostMapping("/api/v1/websites")
    @Operation(summary = "Create a Website under an authorized Store")
    public ResponseEntity<ApiResponse<WebsiteResponse>> createWebsite(@Valid @RequestBody CreateWebsiteRequest request) {
        WebsiteResponse response = websiteService.createWebsite(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Website created successfully"));
    }

    @GetMapping("/api/v1/stores/{storeId}/websites")
    @Operation(summary = "List authorized Websites owned by a Store with search and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<WebsiteResponse>>> listWebsitesByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<WebsiteResponse> response = websiteService.listWebsitesByStore(storeId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Websites retrieved successfully"));
    }

    @GetMapping("/api/v1/websites/{websiteId}")
    @Operation(summary = "Read an authorized Website by ID")
    public ResponseEntity<ApiResponse<WebsiteResponse>> getWebsite(@PathVariable UUID websiteId) {
        WebsiteResponse response = websiteService.getWebsiteById(websiteId);
        return ResponseEntity.ok(ApiResponse.success(response, "Website retrieved successfully"));
    }
}
