package com.xyz.platform.business;

import com.xyz.platform.business.dto.BusinessResponse;
import com.xyz.platform.business.dto.CreateBusinessRequest;
import com.xyz.platform.business.entity.Business;
import com.xyz.platform.business.repository.BusinessRepository;
import com.xyz.platform.business.service.BusinessService;
import com.xyz.platform.company.entity.Company;
import com.xyz.platform.company.repository.CompanyRepository;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private BusinessService businessService;

    @Test
    void shouldCreateBusinessAndAutoInheritOrganizationIdFromCompany() {
        UUID orgId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Company parentCompany = Company.builder()
                .companyId(companyId)
                .organizationId(orgId)
                .companyCode("COMP_001")
                .companyName("Parent Company")
                .status("ACTIVE")
                .build();

        CreateBusinessRequest request = CreateBusinessRequest.builder()
                .companyId(companyId)
                .businessCode("BIZ_001")
                .businessName("Primary Business")
                .build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(parentCompany));
        when(businessRepository.existsByCompanyIdAndBusinessCode(companyId, "BIZ_001")).thenReturn(false);
        when(businessRepository.save(any(Business.class))).thenAnswer(inv -> {
            Business b = inv.getArgument(0);
            b.onPrePersist();
            return b;
        });

        BusinessResponse response = businessService.createBusiness(request);

        assertNotNull(response);
        assertEquals("BIZ_001", response.getBusinessCode());
        assertEquals(companyId, response.getCompanyId());
        assertEquals(orgId, response.getOrganizationId(), "Organization ID must be inherited from the parent Company");
        assertEquals("ACTIVE", response.getStatus());
        verify(businessRepository).save(any(Business.class));
    }

    @Test
    void shouldRejectWhenCompanyDoesNotExist() {
        UUID nonExistentCompanyId = UUID.randomUUID();
        CreateBusinessRequest request = CreateBusinessRequest.builder()
                .companyId(nonExistentCompanyId)
                .businessCode("BIZ_002")
                .businessName("Orphan Business")
                .build();

        when(companyRepository.findById(nonExistentCompanyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.createBusiness(request));
        verify(businessRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateBusinessCodeUnderSameCompany() {
        UUID companyId = UUID.randomUUID();
        Company parentCompany = Company.builder()
                .companyId(companyId)
                .organizationId(UUID.randomUUID())
                .companyCode("COMP_001")
                .build();

        CreateBusinessRequest request = CreateBusinessRequest.builder()
                .companyId(companyId)
                .businessCode("DUPLICATE_BIZ")
                .businessName("Duplicate Business")
                .build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(parentCompany));
        when(businessRepository.existsByCompanyIdAndBusinessCode(companyId, "DUPLICATE_BIZ")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> businessService.createBusiness(request));
        assertEquals("BUSINESS_CODE_EXISTS", ex.getErrorCode());
        verify(businessRepository, never()).save(any());
    }
}
