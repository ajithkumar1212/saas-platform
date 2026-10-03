package com.xyz.platform.shared.reference.service;

import com.xyz.platform.shared.exception.ResourceNotFoundException;
import com.xyz.platform.shared.reference.dto.ReferenceValueResponse;
import com.xyz.platform.shared.reference.entity.ReferenceType;
import com.xyz.platform.shared.reference.entity.ReferenceValue;
import com.xyz.platform.shared.reference.repository.ReferenceTypeRepository;
import com.xyz.platform.shared.reference.repository.ReferenceValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReferenceDataService {

    private final ReferenceTypeRepository referenceTypeRepository;
    private final ReferenceValueRepository referenceValueRepository;

    public ReferenceDataService(ReferenceTypeRepository referenceTypeRepository,
                                ReferenceValueRepository referenceValueRepository) {
        this.referenceTypeRepository = referenceTypeRepository;
        this.referenceValueRepository = referenceValueRepository;
    }

    @Transactional(readOnly = true)
    public List<ReferenceValueResponse> getReferenceValues(String typeCode) {
        ReferenceType refType = referenceTypeRepository.findByTypeCode(typeCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("ReferenceType", typeCode));

        List<ReferenceValue> values = referenceValueRepository.findByReferenceTypeIdOrderBySortOrderAsc(refType.getReferenceTypeId());

        return values.stream()
                .map(v -> ReferenceValueResponse.builder()
                        .referenceValueId(v.getReferenceValueId())
                        .typeCode(refType.getTypeCode())
                        .valueCode(v.getValueCode())
                        .valueName(v.getValueName())
                        .description(v.getDescription())
                        .sortOrder(v.getSortOrder())
                        .isDefault(v.isDefault())
                        .status(v.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
