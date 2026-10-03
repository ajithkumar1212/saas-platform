package com.xyz.platform.store.controller;

import com.xyz.platform.shared.dto.ApiResponse;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.store.dto.CreateStoreRequest;
import com.xyz.platform.store.dto.StoreResponse;
import com.xyz.platform.store.service.StoreService;
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
@Tag(name = "Store Management", description = "Endpoints for managing physical and digital stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/api/v1/stores")
    @Operation(summary = "Create a Store for an authorized Business")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody CreateStoreRequest request) {
        StoreResponse response = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Store created successfully"));
    }

    @GetMapping("/api/v1/businesses/{businessId}/stores")
    @Operation(summary = "List authorized Stores for a Business with search and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<StoreResponse>>> listStoresByBusiness(
            @PathVariable UUID businessId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<StoreResponse> response = storeService.listStoresByBusiness(businessId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Stores retrieved successfully"));
    }

    @GetMapping("/api/v1/stores/{storeId}")
    @Operation(summary = "Read an authorized Store by ID")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(@PathVariable UUID storeId) {
        StoreResponse response = storeService.getStoreById(storeId);
        return ResponseEntity.ok(ApiResponse.success(response, "Store retrieved successfully"));
    }
}
