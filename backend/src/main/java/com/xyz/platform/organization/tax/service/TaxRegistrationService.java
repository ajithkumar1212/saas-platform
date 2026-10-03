package com.xyz.platform.organization.tax.service;

import com.xyz.platform.business.repository.BusinessRepository;
import com.xyz.platform.organization.tax.dto.BusinessTaxRegistrationResponse;
import com.xyz.platform.organization.tax.entity.BusinessTaxRegistration;
import com.xyz.platform.organization.tax.entity.TaxRegistration;
import com.xyz.platform.organization.tax.repository.BusinessTaxRegistrationRepository;
import com.xyz.platform.organization.tax.repository.TaxRegistrationRepository;
import com.xyz.platform.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaxRegistrationService {

    private final BusinessTaxRegistrationRepository businessTaxRegistrationRepository;
    private final TaxRegistrationRepository taxRegistrationRepository;
    private final BusinessRepository businessRepository;

    public TaxRegistrationService(BusinessTaxRegistrationRepository businessTaxRegistrationRepository,
                                  TaxRegistrationRepository taxRegistrationRepository,
                                  BusinessRepository businessRepository) {
        this.businessTaxRegistrationRepository = businessTaxRegistrationRepository;
        this.taxRegistrationRepository = taxRegistrationRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional(readOnly = true)
    public List<BusinessTaxRegistrationResponse> listTaxRegistrationsByBusiness(UUID businessId) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business", businessId);
        }

        List<BusinessTaxRegistration> registrations = businessTaxRegistrationRepository.findByIdBusinessId(businessId);

        return registrations.stream().map(btr -> {
            TaxRegistration tr = taxRegistrationRepository.findById(btr.getId().getTaxRegistrationId())
                    .orElse(null);

            return BusinessTaxRegistrationResponse.builder()
                    .businessId(btr.getId().getBusinessId())
                    .taxRegistrationId(btr.getId().getTaxRegistrationId())
                    .organizationId(btr.getOrganizationId())
                    .registrationType(tr != null ? tr.getRegistrationType() : null)
                    .registrationNumber(tr != null ? tr.getRegistrationNumber() : null)
                    .gstin(tr != null ? tr.getGstin() : null)
                    .isPrimary(btr.isPrimary())
                    .status(tr != null ? tr.getStatus() : null)
                    .validFrom(btr.getValidFrom())
                    .validUntil(btr.getValidUntil())
                    .description(btr.getDescription())
                    .build();
        }).collect(Collectors.toList());
    }
}
