package com.xyz.platform.controlplane.provisioning.controller;

import com.xyz.platform.controlplane.provisioning.dto.TenantProvisionRequest;
import com.xyz.platform.controlplane.provisioning.dto.TenantProvisionResponse;
import com.xyz.platform.controlplane.provisioning.service.TenantProvisioningService;
import com.xyz.platform.controlplane.tenant.entity.TenantRegistry;
import com.xyz.platform.controlplane.tenant.repository.TenantRegistryRepository;
import com.xyz.platform.shared.dto.ApiResponse;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/provisioning")
@Tag(name = "Control Plane - Tenant Provisioning", description = "Endpoints for managing tenant onboarding and schema lifecycle")
public class TenantProvisioningController {

    private final TenantProvisioningService tenantProvisioningService;
    private final TenantRegistryRepository tenantRegistryRepository;

    public TenantProvisioningController(TenantProvisioningService tenantProvisioningService,
                                        TenantRegistryRepository tenantRegistryRepository) {
        this.tenantProvisioningService = tenantProvisioningService;
        this.tenantRegistryRepository = tenantRegistryRepository;
    }

    @PostMapping("/tenants")
    @Operation(summary = "Provision a new tenant and execute schema migration")
    public ResponseEntity<ApiResponse<TenantProvisionResponse>> provisionTenant(@Valid @RequestBody TenantProvisionRequest request) {
        TenantProvisionResponse response = tenantProvisioningService.provisionTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tenant provisioned successfully"));
    }

    @GetMapping("/tenants")
    @Operation(summary = "List all registered tenants in the platform")
    public ResponseEntity<ApiResponse<List<TenantRegistry>>> listTenants() {
        List<TenantRegistry> tenants = tenantRegistryRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(tenants, "Tenants retrieved successfully"));
    }

    @GetMapping("/tenants/{tenantCode}")
    @Operation(summary = "Retrieve tenant registry details by tenant code")
    public ResponseEntity<ApiResponse<TenantRegistry>> getTenant(@PathVariable String tenantCode) {
        TenantRegistry tenant = tenantRegistryRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ResourceNotFoundException("TenantRegistry", tenantCode));
        return ResponseEntity.ok(ApiResponse.success(tenant, "Tenant retrieved successfully"));
    }
}
