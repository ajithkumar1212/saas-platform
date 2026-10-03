package com.xyz.platform.business.service;

import com.xyz.platform.business.dto.BusinessResponse;
import com.xyz.platform.business.dto.CreateBusinessRequest;
import com.xyz.platform.business.entity.Business;
import com.xyz.platform.business.repository.BusinessRepository;
import com.xyz.platform.company.entity.Company;
import com.xyz.platform.company.repository.CompanyRepository;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final CompanyRepository companyRepository;

    public BusinessService(BusinessRepository businessRepository, CompanyRepository companyRepository) {
        this.businessRepository = businessRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public BusinessResponse createBusiness(CreateBusinessRequest request) {
        Company parentCompany = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.getCompanyId()));

        if (businessRepository.existsByCompanyIdAndBusinessCode(request.getCompanyId(), request.getBusinessCode())) {
            throw new BusinessException("BUSINESS_CODE_EXISTS",
                    "Business code '" + request.getBusinessCode() + "' already exists under this company");
        }

        Business business = Business.builder()
                .businessId(UUID.randomUUID())
                .companyId(parentCompany.getCompanyId())
                .organizationId(parentCompany.getOrganizationId())
                .businessCode(request.getBusinessCode().trim())
                .businessName(request.getBusinessName().trim())
                .status("ACTIVE")
                .description(request.getDescription())
                .build();

        Business saved = businessRepository.save(business);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BusinessResponse> listBusinesses(UUID companyId, String search, Pageable pageable) {
        Page<Business> page;
        boolean hasSearch = (search != null && !search.isBlank());

        if (companyId != null) {
            if (hasSearch) {
                page = businessRepository.searchBusinesses(companyId, search.trim(), pageable);
            } else {
                page = businessRepository.findByCompanyId(companyId, pageable);
            }
        } else {
            page = businessRepository.findAll(pageable);
        }
        return PagedResponse.from(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(UUID businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", businessId));
        return mapToResponse(business);
    }

    private BusinessResponse mapToResponse(Business business) {
        return BusinessResponse.builder()
                .businessId(business.getBusinessId())
                .companyId(business.getCompanyId())
                .organizationId(business.getOrganizationId())
                .businessCode(business.getBusinessCode())
                .businessName(business.getBusinessName())
                .status(business.getStatus())
                .description(business.getDescription())
                .createdAt(business.getCreatedAt())
                .updatedAt(business.getUpdatedAt())
                .build();
    }
}
