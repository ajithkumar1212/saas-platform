package com.xyz.platform.warehouse.controller;

import com.xyz.platform.shared.dto.ApiResponse;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.warehouse.dto.CreateWarehouseRequest;
import com.xyz.platform.warehouse.dto.WarehouseResponse;
import com.xyz.platform.warehouse.service.WarehouseService;
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
@Tag(name = "Warehouse Management", description = "Endpoints for managing store warehouses and inventory storage locations")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping("/api/v1/warehouses")
    @Operation(summary = "Create a Warehouse under an authorized Store")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        WarehouseResponse response = warehouseService.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Warehouse created successfully"));
    }

    @GetMapping("/api/v1/stores/{storeId}/warehouses")
    @Operation(summary = "List authorized Warehouses owned by a Store with search and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<WarehouseResponse>>> listWarehousesByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<WarehouseResponse> response = warehouseService.listWarehousesByStore(storeId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Warehouses retrieved successfully"));
    }

    @GetMapping("/api/v1/warehouses/{warehouseId}")
    @Operation(summary = "Read an authorized Warehouse by ID")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouse(@PathVariable UUID warehouseId) {
        WarehouseResponse response = warehouseService.getWarehouseById(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(response, "Warehouse retrieved successfully"));
    }
}
