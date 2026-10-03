package com.xyz.platform.company;

import com.xyz.platform.company.dto.CompanyResponse;
import com.xyz.platform.company.dto.CreateCompanyRequest;
import com.xyz.platform.company.entity.Company;
import com.xyz.platform.company.repository.CompanyRepository;
import com.xyz.platform.company.service.CompanyService;
import com.xyz.platform.organization.repository.OrganizationRepository;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
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
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void shouldCreateCompanySuccessfullyUnderExistingOrganization() {
        UUID orgId = UUID.randomUUID();
        CreateCompanyRequest request = CreateCompanyRequest.builder()
                .organizationId(orgId)
                .companyCode("COMP_001")
                .companyName("Acme Retail LLC")
                .build();

        when(organizationRepository.existsById(orgId)).thenReturn(true);
        when(companyRepository.existsByOrganizationIdAndCompanyCode(orgId, "COMP_001")).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
            Company saved = inv.getArgument(0);
            saved.onPrePersist();
            return saved;
        });

        CompanyResponse response = companyService.createCompany(request);

        assertNotNull(response);
        assertEquals("COMP_001", response.getCompanyCode());
        assertEquals(orgId, response.getOrganizationId());
        assertEquals("ACTIVE", response.getStatus());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void shouldRejectWhenOrganizationDoesNotExist() {
        UUID nonExistentOrgId = UUID.randomUUID();
        CreateCompanyRequest request = CreateCompanyRequest.builder()
                .organizationId(nonExistentOrgId)
                .companyCode("COMP_002")
                .companyName("Invalid Parent Company")
                .build();

        when(organizationRepository.existsById(nonExistentOrgId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> companyService.createCompany(request));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateCompanyCodeWithinSameOrganization() {
        UUID orgId = UUID.randomUUID();
        CreateCompanyRequest request = CreateCompanyRequest.builder()
                .organizationId(orgId)
                .companyCode("DUPLICATE_COMP")
                .companyName("Duplicate Company")
                .build();

        when(organizationRepository.existsById(orgId)).thenReturn(true);
        when(companyRepository.existsByOrganizationIdAndCompanyCode(orgId, "DUPLICATE_COMP")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> companyService.createCompany(request));
        assertEquals("COMPANY_CODE_EXISTS", ex.getErrorCode());
        verify(companyRepository, never()).save(any());
    }
}
