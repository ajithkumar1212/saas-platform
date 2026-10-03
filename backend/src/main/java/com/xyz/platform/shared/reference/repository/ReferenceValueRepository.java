package com.xyz.platform.shared.reference.repository;

import com.xyz.platform.shared.reference.entity.ReferenceValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReferenceValueRepository extends JpaRepository<ReferenceValue, UUID> {

    List<ReferenceValue> findByReferenceTypeIdOrderBySortOrderAsc(UUID referenceTypeId);
}
