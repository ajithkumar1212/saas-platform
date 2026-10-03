package com.xyz.platform.brand.service;

import com.xyz.platform.brand.dto.BrandResponse;
import com.xyz.platform.brand.dto.CreateBrandRequest;
import com.xyz.platform.brand.entity.Brand;
import com.xyz.platform.brand.repository.BrandRepository;
import com.xyz.platform.business.repository.BusinessRepository;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final BusinessRepository businessRepository;

    public BrandService(BrandRepository brandRepository, BusinessRepository businessRepository) {
        this.brandRepository = brandRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        if (!businessRepository.existsById(request.getBusinessId())) {
            throw new ResourceNotFoundException("Business", request.getBusinessId());
        }

        if (brandRepository.existsByBusinessIdAndBrandCode(request.getBusinessId(), request.getBrandCode())) {
            throw new BusinessException("BRAND_CODE_EXISTS", 
                    "Brand code '" + request.getBrandCode() + "' already exists for this business");
        }

        Brand brand = Brand.builder()
                .brandId(UUID.randomUUID())
                .businessId(request.getBusinessId())
                .brandCode(request.getBrandCode().trim())
                .brandName(request.getBrandName().trim())
                .status("ACTIVE")
                .description(request.getDescription())
                .build();

        Brand saved = brandRepository.save(brand);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BrandResponse> listBrandsByBusiness(UUID businessId, String search, Pageable pageable) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business", businessId);
        }
        Page<Brand> page;
        if (search != null && !search.isBlank()) {
            page = brandRepository.searchBrands(businessId, search.trim(), pageable);
        } else {
            page = brandRepository.findByBusinessId(businessId, pageable);
        }
        return PagedResponse.from(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrandById(UUID brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", brandId));
        return mapToResponse(brand);
    }

    private BrandResponse mapToResponse(Brand brand) {
        return BrandResponse.builder()
                .brandId(brand.getBrandId())
                .businessId(brand.getBusinessId())
                .brandCode(brand.getBrandCode())
                .brandName(brand.getBrandName())
                .status(brand.getStatus())
                .description(brand.getDescription())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }
}
