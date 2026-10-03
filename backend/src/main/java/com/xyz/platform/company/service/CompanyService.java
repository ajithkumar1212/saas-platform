package com.xyz.platform.company.service;

import com.xyz.platform.company.dto.CreateCompanyRequest;
import com.xyz.platform.company.dto.CompanyResponse;
import com.xyz.platform.company.entity.Company;
import com.xyz.platform.company.repository.CompanyRepository;
import com.xyz.platform.organization.repository.OrganizationRepository;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final OrganizationRepository organizationRepository;

    public CompanyService(CompanyRepository companyRepository, OrganizationRepository organizationRepository) {
        this.companyRepository = companyRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        if (!organizationRepository.existsById(request.getOrganizationId())) {
            throw new ResourceNotFoundException("Organization", request.getOrganizationId());
        }

        if (companyRepository.existsByOrganizationIdAndCompanyCode(request.getOrganizationId(), request.getCompanyCode())) {
            throw new BusinessException("COMPANY_CODE_EXISTS",
                    "Company code '" + request.getCompanyCode() + "' already exists in the given organization");
        }

        Company company = Company.builder()
                .companyId(UUID.randomUUID())
                .organizationId(request.getOrganizationId())
                .companyCode(request.getCompanyCode().trim())
                .companyName(request.getCompanyName().trim())
                .status("ACTIVE")
                .description(request.getDescription())
                .build();

        Company saved = companyRepository.save(company);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CompanyResponse> listCompanies(UUID organizationId, String search, Pageable pageable) {
        Page<Company> page;
        boolean hasSearch = (search != null && !search.isBlank());

        if (organizationId != null) {
            if (hasSearch) {
                page = companyRepository.searchCompanies(organizationId, search.trim(), pageable);
            } else {
                page = companyRepository.findByOrganizationId(organizationId, pageable);
            }
        } else {
            page = companyRepository.findAll(pageable);
        }

        return PagedResponse.from(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        return mapToResponse(company);
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .companyId(company.getCompanyId())
                .organizationId(company.getOrganizationId())
                .companyCode(company.getCompanyCode())
                .companyName(company.getCompanyName())
                .status(company.getStatus())
                .description(company.getDescription())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
