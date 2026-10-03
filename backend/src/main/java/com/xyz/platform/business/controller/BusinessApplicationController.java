package com.xyz.platform.business.controller;

import com.xyz.platform.business.dto.BusinessApplicationResponse;
import com.xyz.platform.business.dto.EnableBusinessApplicationRequest;
import com.xyz.platform.business.service.BusinessApplicationService;
import com.xyz.platform.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/applications")
@Tag(name = "Business Applications", description = "Endpoints for managing application enablement for a business")
public class BusinessApplicationController {

    private final BusinessApplicationService businessApplicationService;

    public BusinessApplicationController(BusinessApplicationService businessApplicationService) {
        this.businessApplicationService = businessApplicationService;
    }

    @PostMapping
    @Operation(summary = "Enable an approved Phase 1 application for an authorized Business")
    public ResponseEntity<ApiResponse<BusinessApplicationResponse>> enableApplication(
            @PathVariable UUID businessId,
            @Valid @RequestBody EnableBusinessApplicationRequest request) {
        BusinessApplicationResponse response = businessApplicationService.enableApplication(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Application enabled successfully"));
    }

    @GetMapping
    @Operation(summary = "List application enablements for an authorized Business")
    public ResponseEntity<ApiResponse<List<BusinessApplicationResponse>>> listApplications(@PathVariable UUID businessId) {
        List<BusinessApplicationResponse> response = businessApplicationService.listBusinessApplications(businessId);
        return ResponseEntity.ok(ApiResponse.success(response, "Business applications retrieved successfully"));
    }
}
