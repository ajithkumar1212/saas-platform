package com.xyz.platform.shared.reference.repository;

import com.xyz.platform.shared.reference.entity.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferenceTypeRepository extends JpaRepository<ReferenceType, UUID> {

    Optional<ReferenceType> findByTypeCode(String typeCode);
}
