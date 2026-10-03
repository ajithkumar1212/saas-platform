package com.xyz.platform.organization.controller;

import com.xyz.platform.organization.dto.CreateOrganizationRequest;
import com.xyz.platform.organization.dto.OrganizationResponse;
import com.xyz.platform.organization.service.OrganizationService;
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
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organization Management", description = "Endpoints for managing top-level tenant organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(summary = "Create an Organization within trusted tenant context")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Organization created successfully"));
    }

    @GetMapping
    @Operation(summary = "List authorized Organizations in effective tenant scope with pagination and search")
    public ResponseEntity<ApiResponse<PagedResponse<OrganizationResponse>>> listOrganizations(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<OrganizationResponse> response = organizationService.listOrganizations(search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Organizations retrieved successfully"));
    }

    @GetMapping("/{organizationId}")
    @Operation(summary = "Read an authorized Organization by ID")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganization(@PathVariable UUID organizationId) {
        OrganizationResponse response = organizationService.getOrganizationById(organizationId);
        return ResponseEntity.ok(ApiResponse.success(response, "Organization retrieved successfully"));
    }
}
