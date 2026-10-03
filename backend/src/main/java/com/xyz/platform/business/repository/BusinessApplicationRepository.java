package com.xyz.platform.business.repository;

import com.xyz.platform.business.entity.BusinessApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessApplicationRepository extends JpaRepository<BusinessApplication, UUID> {

    Optional<BusinessApplication> findByBusinessIdAndApplicationId(UUID businessId, UUID applicationId);

    List<BusinessApplication> findByBusinessId(UUID businessId);

    boolean existsByBusinessIdAndApplicationId(UUID businessId, UUID applicationId);
}
