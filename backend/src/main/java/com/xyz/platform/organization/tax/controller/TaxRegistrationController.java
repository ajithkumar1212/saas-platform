package com.xyz.platform.organization.tax.controller;

import com.xyz.platform.organization.tax.dto.BusinessTaxRegistrationResponse;
import com.xyz.platform.organization.tax.service.TaxRegistrationService;
import com.xyz.platform.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/tax-registrations")
@Tag(name = "Tax Registrations", description = "Endpoints for viewing tax registrations assigned to a business")
public class TaxRegistrationController {

    private final TaxRegistrationService taxRegistrationService;

    public TaxRegistrationController(TaxRegistrationService taxRegistrationService) {
        this.taxRegistrationService = taxRegistrationService;
    }

    @GetMapping
    @Operation(summary = "Read authorized tax registrations for a Business")
    public ResponseEntity<ApiResponse<List<BusinessTaxRegistrationResponse>>> listTaxRegistrations(@PathVariable UUID businessId) {
        List<BusinessTaxRegistrationResponse> response = taxRegistrationService.listTaxRegistrationsByBusiness(businessId);
        return ResponseEntity.ok(ApiResponse.success(response, "Tax registrations retrieved successfully"));
    }
}
