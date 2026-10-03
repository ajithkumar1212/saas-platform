package com.xyz.platform.shared.reference.controller;

import com.xyz.platform.shared.dto.ApiResponse;
import com.xyz.platform.shared.reference.dto.ReferenceValueResponse;
import com.xyz.platform.shared.reference.service.ReferenceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reference-data")
@Tag(name = "Reference Data", description = "Read-only endpoints for approved platform and tenant reference values")
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/{typeCode}")
    @Operation(summary = "Read approved reference values by type code (e.g. STATUS, CHANNEL_TYPE, BUSINESS_TYPE)")
    public ResponseEntity<ApiResponse<List<ReferenceValueResponse>>> getReferenceValues(@PathVariable String typeCode) {
        List<ReferenceValueResponse> response = referenceDataService.getReferenceValues(typeCode);
        return ResponseEntity.ok(ApiResponse.success(response, "Reference values retrieved successfully"));
    }
}
