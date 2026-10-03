package com.xyz.platform.controlplane.application.controller;

import com.xyz.platform.controlplane.application.dto.ApplicationCatalogResponse;
import com.xyz.platform.controlplane.application.service.ApplicationCatalogService;
import com.xyz.platform.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "Application Catalog", description = "Endpoints for viewing platform-owned applications catalog")
public class ApplicationCatalogController {

    private final ApplicationCatalogService applicationCatalogService;

    public ApplicationCatalogController(ApplicationCatalogService applicationCatalogService) {
        this.applicationCatalogService = applicationCatalogService;
    }

    @GetMapping
    @Operation(summary = "Read the platform-owned Phase 1 application catalog")
    public ResponseEntity<ApiResponse<List<ApplicationCatalogResponse>>> listApplications() {
        List<ApplicationCatalogResponse> response = applicationCatalogService.listCatalogApplications();
        return ResponseEntity.ok(ApiResponse.success(response, "Application catalog retrieved successfully"));
    }
}
