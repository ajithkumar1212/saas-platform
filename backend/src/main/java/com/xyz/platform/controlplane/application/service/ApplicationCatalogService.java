package com.xyz.platform.controlplane.application.service;

import com.xyz.platform.controlplane.application.dto.ApplicationCatalogResponse;
import com.xyz.platform.controlplane.application.entity.ApplicationCatalog;
import com.xyz.platform.controlplane.application.repository.ApplicationCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationCatalogService {

    private final ApplicationCatalogRepository applicationCatalogRepository;

    public ApplicationCatalogService(ApplicationCatalogRepository applicationCatalogRepository) {
        this.applicationCatalogRepository = applicationCatalogRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationCatalogResponse> listCatalogApplications() {
        return applicationCatalogRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ApplicationCatalogResponse mapToResponse(ApplicationCatalog app) {
        return ApplicationCatalogResponse.builder()
                .applicationId(app.getApplicationId())
                .applicationCode(app.getApplicationCode())
                .applicationName(app.getApplicationName())
                .status(app.getStatus())
                .description(app.getDescription())
                .createdAt(app.getCreatedAt())
                .build();
    }
}
