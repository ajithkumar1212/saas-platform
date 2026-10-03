package com.xyz.platform.organization.service;

import com.xyz.platform.organization.dto.CreateOrganizationRequest;
import com.xyz.platform.organization.dto.OrganizationResponse;
import com.xyz.platform.organization.entity.Organization;
import com.xyz.platform.organization.repository.OrganizationRepository;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import com.xyz.platform.shared.exception.TenantNotFoundException;
import com.xyz.platform.tenancy.context.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new TenantNotFoundException("Active tenant context is required to create an Organization");
        }

        if (organizationRepository.existsByOrganizationCode(request.getOrganizationCode())) {
            throw new BusinessException("ORGANIZATION_CODE_EXISTS", 
                    "Organization with code '" + request.getOrganizationCode() + "' already exists");
        }

        Organization org = Organization.builder()
                .organizationId(UUID.randomUUID())
                .tenantId(tenantId)
                .organizationCode(request.getOrganizationCode().trim())
                .organizationName(request.getOrganizationName().trim())
                .status("ACTIVE")
                .description(request.getDescription())
                .build();

        Organization saved = organizationRepository.save(org);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrganizationResponse> listOrganizations(String search, Pageable pageable) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new TenantNotFoundException("Active tenant context is required");
        }
        Page<Organization> page;
        if (search != null && !search.isBlank()) {
            page = organizationRepository.searchOrganizations(tenantId, search.trim(), pageable);
        } else {
            page = organizationRepository.findByTenantId(tenantId, pageable);
        }
        return PagedResponse.from(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));
        return mapToResponse(org);
    }

    private OrganizationResponse mapToResponse(Organization org) {
        return OrganizationResponse.builder()
                .organizationId(org.getOrganizationId())
                .tenantId(org.getTenantId())
                .organizationCode(org.getOrganizationCode())
                .organizationName(org.getOrganizationName())
                .status(org.getStatus())
                .description(org.getDescription())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}
