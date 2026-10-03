package com.xyz.platform.business.service;

import com.xyz.platform.business.dto.BusinessApplicationResponse;
import com.xyz.platform.business.dto.EnableBusinessApplicationRequest;
import com.xyz.platform.business.entity.BusinessApplication;
import com.xyz.platform.business.repository.BusinessApplicationRepository;
import com.xyz.platform.business.repository.BusinessRepository;
import com.xyz.platform.controlplane.application.entity.ApplicationCatalog;
import com.xyz.platform.controlplane.application.repository.ApplicationCatalogRepository;
import com.xyz.platform.shared.exception.BusinessException;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusinessApplicationService {

    private final BusinessApplicationRepository businessApplicationRepository;
    private final BusinessRepository businessRepository;
    private final ApplicationCatalogRepository applicationCatalogRepository;

    public BusinessApplicationService(BusinessApplicationRepository businessApplicationRepository,
                                      BusinessRepository businessRepository,
                                      ApplicationCatalogRepository applicationCatalogRepository) {
        this.businessApplicationRepository = businessApplicationRepository;
        this.businessRepository = businessRepository;
        this.applicationCatalogRepository = applicationCatalogRepository;
    }

    @Transactional
    public BusinessApplicationResponse enableApplication(UUID businessId, EnableBusinessApplicationRequest request) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business", businessId);
        }

        ApplicationCatalog appCatalog = applicationCatalogRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationCatalog", request.getApplicationId()));

        if (businessApplicationRepository.existsByBusinessIdAndApplicationId(businessId, request.getApplicationId())) {
            throw new BusinessException("APPLICATION_ALREADY_ENABLED", 
                    "Application '" + appCatalog.getApplicationCode() + "' is already enabled for this business");
        }

        BusinessApplication enablement = BusinessApplication.builder()
                .businessApplicationId(UUID.randomUUID())
                .businessId(businessId)
                .applicationId(appCatalog.getApplicationId())
                .status("ACTIVE")
                .enabledAt(Instant.now())
                .description(request.getDescription())
                .build();

        BusinessApplication saved = businessApplicationRepository.save(enablement);
        return mapToResponse(saved, appCatalog);
    }

    @Transactional(readOnly = true)
    public List<BusinessApplicationResponse> listBusinessApplications(UUID businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business", businessId);
        }

        return businessApplicationRepository.findByBusinessId(businessId)
                .stream()
                .map(enablement -> {
                    ApplicationCatalog app = applicationCatalogRepository.findById(enablement.getApplicationId())
                            .orElse(null);
                    return mapToResponse(enablement, app);
                })
                .collect(Collectors.toList());
    }

    private BusinessApplicationResponse mapToResponse(BusinessApplication enablement, ApplicationCatalog app) {
        return BusinessApplicationResponse.builder()
                .businessApplicationId(enablement.getBusinessApplicationId())
                .businessId(enablement.getBusinessId())
                .applicationId(enablement.getApplicationId())
                .applicationCode(app != null ? app.getApplicationCode() : null)
                .applicationName(app != null ? app.getApplicationName() : null)
                .status(enablement.getStatus())
                .enabledAt(enablement.getEnabledAt())
                .description(enablement.getDescription())
                .createdAt(enablement.getCreatedAt())
                .build();
    }
}
