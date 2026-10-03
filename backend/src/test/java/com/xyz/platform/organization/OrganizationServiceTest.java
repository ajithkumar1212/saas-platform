package com.xyz.platform.organization;

import com.xyz.platform.organization.dto.CreateOrganizationRequest;
import com.xyz.platform.organization.dto.OrganizationResponse;
import com.xyz.platform.organization.entity.Organization;
import com.xyz.platform.organization.repository.OrganizationRepository;
import com.xyz.platform.organization.service.OrganizationService;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.TenantNotFoundException;
import com.xyz.platform.tenancy.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContext.setTenant(tenantId, "test_corp", "tenant_test_corp");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateOrganizationSuccessfully() {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .organizationCode("ORG_001")
                .organizationName("Acme Global Org")
                .description("Primary organization")
                .build();

        when(organizationRepository.existsByOrganizationCode("ORG_001")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization saved = inv.getArgument(0);
            saved.onPrePersist();
            return saved;
        });

        OrganizationResponse response = organizationService.createOrganization(request);

        assertNotNull(response);
        assertEquals("ORG_001", response.getOrganizationCode());
        assertEquals(tenantId, response.getTenantId());
        assertEquals("ACTIVE", response.getStatus());
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void shouldRejectWhenTenantContextIsMissing() {
        TenantContext.clear();

        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .organizationCode("ORG_002")
                .organizationName("Orphan Org")
                .build();

        assertThrows(TenantNotFoundException.class, () -> organizationService.createOrganization(request));
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateOrganizationCode() {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .organizationCode("DUPLICATE_CODE")
                .organizationName("Duplicate Org")
                .build();

        when(organizationRepository.existsByOrganizationCode("DUPLICATE_CODE")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> organizationService.createOrganization(request));
        assertEquals("ORGANIZATION_CODE_EXISTS", ex.getErrorCode());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void shouldListOrganizationsWithPaginationAndSearch() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Organization org = Organization.builder()
                .organizationId(UUID.randomUUID())
                .tenantId(tenantId)
                .organizationCode("ORG_001")
                .organizationName("Acme Global Org")
                .status("ACTIVE")
                .build();
        org.onPrePersist();

        org.springframework.data.domain.Page<Organization> mockPage = 
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(org), pageable, 1);

        when(organizationRepository.searchOrganizations(tenantId, "Acme", pageable)).thenReturn(mockPage);

        com.xyz.platform.shared.dto.PagedResponse<OrganizationResponse> result = 
                organizationService.listOrganizations("Acme", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals("ORG_001", result.getContent().get(0).getOrganizationCode());
    }

    @Test
    void shouldListOrganizationsWithPaginationWhenSearchIsNull() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Organization org = Organization.builder()
                .organizationId(UUID.randomUUID())
                .tenantId(tenantId)
                .organizationCode("ORG_001")
                .organizationName("Acme Global Org")
                .status("ACTIVE")
                .build();
        org.onPrePersist();

        org.springframework.data.domain.Page<Organization> mockPage = 
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(org), pageable, 1);

        when(organizationRepository.findByTenantId(tenantId, pageable)).thenReturn(mockPage);

        com.xyz.platform.shared.dto.PagedResponse<OrganizationResponse> result = 
                organizationService.listOrganizations(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(organizationRepository).findByTenantId(tenantId, pageable);
    }
}
