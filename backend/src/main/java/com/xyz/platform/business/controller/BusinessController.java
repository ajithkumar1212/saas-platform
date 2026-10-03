package com.xyz.platform.business.controller;

import com.xyz.platform.business.dto.BusinessResponse;
import com.xyz.platform.business.dto.CreateBusinessRequest;
import com.xyz.platform.business.service.BusinessService;
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
@RequestMapping("/api/v1/businesses")
@Tag(name = "Business Management", description = "Endpoints for managing operational businesses within companies")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping
    @Operation(summary = "Create a Business under an authorized Company")
    public ResponseEntity<ApiResponse<BusinessResponse>> createBusiness(@Valid @RequestBody CreateBusinessRequest request) {
        BusinessResponse response = businessService.createBusiness(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Business created successfully"));
    }

    @GetMapping
    @Operation(summary = "List authorized Businesses with optional company, search, and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<BusinessResponse>>> listBusinesses(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<BusinessResponse> response = businessService.listBusinesses(companyId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Businesses retrieved successfully"));
    }

    @GetMapping("/{businessId}")
    @Operation(summary = "Read an authorized Business by ID")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusiness(@PathVariable UUID businessId) {
        BusinessResponse response = businessService.getBusinessById(businessId);
        return ResponseEntity.ok(ApiResponse.success(response, "Business retrieved successfully"));
    }
}
