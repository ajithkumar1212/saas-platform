package com.xyz.platform.shared.reference;

import com.xyz.platform.shared.exception.ResourceNotFoundException;
import com.xyz.platform.shared.reference.dto.ReferenceValueResponse;
import com.xyz.platform.shared.reference.entity.ReferenceType;
import com.xyz.platform.shared.reference.entity.ReferenceValue;
import com.xyz.platform.shared.reference.repository.ReferenceTypeRepository;
import com.xyz.platform.shared.reference.repository.ReferenceValueRepository;
import com.xyz.platform.shared.reference.service.ReferenceDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenceDataServiceTest {

    @Mock
    private ReferenceTypeRepository referenceTypeRepository;

    @Mock
    private ReferenceValueRepository referenceValueRepository;

    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Test
    void shouldReturnReferenceValuesForValidTypeCode() {
        UUID typeId = UUID.randomUUID();
        ReferenceType refType = ReferenceType.builder()
                .referenceTypeId(typeId)
                .typeCode("STATUS")
                .typeName("Status")
                .status("ACTIVE")
                .build();

        ReferenceValue val1 = ReferenceValue.builder()
                .referenceValueId(UUID.randomUUID())
                .referenceTypeId(typeId)
                .valueCode("ACTIVE")
                .valueName("Active")
                .sortOrder(1)
                .isDefault(true)
                .status("ACTIVE")
                .build();

        ReferenceValue val2 = ReferenceValue.builder()
                .referenceValueId(UUID.randomUUID())
                .referenceTypeId(typeId)
                .valueCode("INACTIVE")
                .valueName("Inactive")
                .sortOrder(2)
                .isDefault(false)
                .status("ACTIVE")
                .build();

        when(referenceTypeRepository.findByTypeCode("STATUS")).thenReturn(Optional.of(refType));
        when(referenceValueRepository.findByReferenceTypeIdOrderBySortOrderAsc(typeId)).thenReturn(List.of(val1, val2));

        List<ReferenceValueResponse> responses = referenceDataService.getReferenceValues("STATUS");

        assertEquals(2, responses.size());
        assertEquals("ACTIVE", responses.get(0).getValueCode());
        assertEquals("INACTIVE", responses.get(1).getValueCode());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionForUnknownTypeCode() {
        when(referenceTypeRepository.findByTypeCode("UNKNOWN_TYPE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> referenceDataService.getReferenceValues("UNKNOWN_TYPE"));
    }
}
