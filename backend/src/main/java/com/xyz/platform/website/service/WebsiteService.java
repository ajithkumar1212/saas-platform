package com.xyz.platform.website.service;

import com.xyz.platform.shared.dto.PagedResponse;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import com.xyz.platform.store.repository.StoreRepository;
import com.xyz.platform.website.dto.CreateWebsiteRequest;
import com.xyz.platform.website.dto.WebsiteResponse;
import com.xyz.platform.website.entity.Website;
import com.xyz.platform.website.repository.WebsiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WebsiteService {

    private final WebsiteRepository websiteRepository;
    private final StoreRepository storeRepository;

    public WebsiteService(WebsiteRepository websiteRepository, StoreRepository storeRepository) {
        this.websiteRepository = websiteRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public WebsiteResponse createWebsite(CreateWebsiteRequest request) {
        if (!storeRepository.existsById(request.getStoreId())) {
            throw new ResourceNotFoundException("Store", request.getStoreId());
        }

        if (websiteRepository.existsByStoreIdAndWebsiteCode(request.getStoreId(), request.getWebsiteCode())) {
            throw new BusinessException("WEBSITE_CODE_EXISTS", 
                    "Website code '" + request.getWebsiteCode() + "' already exists for this store");
        }

        Website website = Website.builder()
                .websiteId(UUID.randomUUID())
                .storeId(request.getStoreId())
                .websiteCode(request.getWebsiteCode().trim())
                .websiteName(request.getWebsiteName().trim())
                .websiteType(request.getWebsiteType().trim())
                .status("ACTIVE")
                .description(request.getDescription())
                .build();

        Website saved = websiteRepository.save(website);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WebsiteResponse> listWebsitesByStore(UUID storeId, String search, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
        Page<Website> page;
        if (search != null && !search.isBlank()) {
            page = websiteRepository.searchWebsites(storeId, search.trim(), pageable);
        } else {
            page = websiteRepository.findByStoreId(storeId, pageable);
        }
        return PagedResponse.from(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public WebsiteResponse getWebsiteById(UUID websiteId) {
        Website website = websiteRepository.findById(websiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Website", websiteId));
        return mapToResponse(website);
    }

    private WebsiteResponse mapToResponse(Website website) {
        return WebsiteResponse.builder()
                .websiteId(website.getWebsiteId())
                .storeId(website.getStoreId())
                .websiteCode(website.getWebsiteCode())
                .websiteName(website.getWebsiteName())
                .websiteType(website.getWebsiteType())
                .status(website.getStatus())
                .description(website.getDescription())
                .createdAt(website.getCreatedAt())
                .updatedAt(website.getUpdatedAt())
                .build();
    }
}
