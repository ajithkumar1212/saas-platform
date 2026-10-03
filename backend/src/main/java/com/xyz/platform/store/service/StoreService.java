package com.xyz.platform.store.service;

import com.xyz.platform.brand.repository.BrandRepository;
import com.xyz.platform.business.repository.BusinessRepository;
import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import com.xyz.platform.store.dto.CreateStoreRequest;
import com.xyz.platform.store.dto.StoreResponse;
import com.xyz.platform.store.entity.Store;
import com.xyz.platform.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final BusinessRepository businessRepository;
    private final BrandRepository brandRepository;

    public StoreService(StoreRepository storeRepository,
                        BusinessRepository businessRepository,
                        BrandRepository brandRepository) {
        this.storeRepository = storeRepository;
        this.businessRepository = businessRepository;
        this.brandRepository = brandRepository;
    }

    @Transactional
    public StoreResponse createStore(CreateStoreRequest request) {
        if (!businessRepository.existsById(request.getBusinessId())) {
            throw new ResourceNotFoundException("Business", request.getBusinessId());
        }

        if (request.getBrandId() != null) {
            brandRepository.findByBusinessIdAndBrandCode(request.getBusinessId(), "") // just check business brand
                    .or(() -> brandRepository.findById(request.getBrandId()))
                    .filter(b -> b.getBusinessId().equals(request.getBusinessId()))
                    .orElseThrow(() -> new BusinessException("INVALID_BRAND", 
                            "Brand with ID " + request.getBrandId() + " does not belong to the given business"));
        }

        if (storeRepository.existsByBusinessIdAndStoreCode(request.getBusinessId(), request.getStoreCode())) {
            throw new BusinessException("STORE_CODE_EXISTS", 
                    "Store code '" + request.getStoreCode() + "' already exists for this business");
        }

        Store store = Store.builder()
                .storeId(UUID.randomUUID())
                .businessId(request.getBusinessId())
                .brandId(request.getBrandId())
                .storeCode(request.getStoreCode().trim())
                .storeName(request.getStoreName().trim())
                .storeType(request.getStoreType().trim())
                .status("ACTIVE")
                .description(request.getDescription())
                .build();

        Store saved = storeRepository.save(store);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StoreResponse> listStoresByBusiness(UUID businessId, String search, Pageable pageable) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business", businessId);
        }
        Page<Store> page;
        if (search != null && !search.isBlank()) {
            page = storeRepository.searchStores(businessId, search.trim(), pageable);
        } else {
            page = storeRepository.findByBusinessId(businessId, pageable);
        }
        return PagedResponse.from(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public StoreResponse getStoreById(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));
        return mapToResponse(store);
    }

    private StoreResponse mapToResponse(Store store) {
        return StoreResponse.builder()
                .storeId(store.getStoreId())
                .businessId(store.getBusinessId())
                .brandId(store.getBrandId())
                .storeCode(store.getStoreCode())
                .storeName(store.getStoreName())
                .storeType(store.getStoreType())
                .status(store.getStatus())
                .description(store.getDescription())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
