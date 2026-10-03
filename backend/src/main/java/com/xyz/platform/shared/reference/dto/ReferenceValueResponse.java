package com.xyz.platform.shared.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceValueResponse {
    private UUID referenceValueId;
    private String typeCode;
    private String valueCode;
    private String valueName;
    private String description;
    private Integer sortOrder;
    private boolean isDefault;
    private String status;
}
