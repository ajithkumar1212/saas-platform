package com.xyz.platform.brand.controller;

import com.xyz.platform.brand.dto.BrandResponse;
import com.xyz.platform.brand.dto.CreateBrandRequest;
import com.xyz.platform.brand.service.BrandService;
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
@Tag(name = "Brand Management", description = "Endpoints for managing brands associated with a business")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/api/v1/brands")
    @Operation(summary = "Create a Brand for an authorized Business")
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Brand created successfully"));
    }

    @GetMapping("/api/v1/businesses/{businessId}/brands")
    @Operation(summary = "List authorized Brands for a Business with search and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<BrandResponse>>> listBrandsByBusiness(
            @PathVariable UUID businessId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<BrandResponse> response = brandService.listBrandsByBusiness(businessId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Brands retrieved successfully"));
    }

    @GetMapping("/api/v1/brands/{brandId}")
    @Operation(summary = "Read an authorized Brand by ID")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrand(@PathVariable UUID brandId) {
        BrandResponse response = brandService.getBrandById(brandId);
        return ResponseEntity.ok(ApiResponse.success(response, "Brand retrieved successfully"));
    }
}
