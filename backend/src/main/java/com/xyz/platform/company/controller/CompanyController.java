package com.xyz.platform.company.controller;

import com.xyz.platform.company.dto.CreateCompanyRequest;
import com.xyz.platform.company.dto.CompanyResponse;
import com.xyz.platform.company.service.CompanyService;
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
@RequestMapping("/api/v1/companies")
@Tag(name = "Company Management", description = "Endpoints for managing companies within organizations")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @Operation(summary = "Create a Company under an authorized Organization")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Company created successfully"));
    }

    @GetMapping
    @Operation(summary = "List authorized Companies with optional organization, search, and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyResponse>>> listCompanies(
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<CompanyResponse> response = companyService.listCompanies(organizationId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Companies retrieved successfully"));
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Read an authorized Company by ID")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(@PathVariable UUID companyId) {
        CompanyResponse response = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(ApiResponse.success(response, "Company retrieved successfully"));
    }
}
