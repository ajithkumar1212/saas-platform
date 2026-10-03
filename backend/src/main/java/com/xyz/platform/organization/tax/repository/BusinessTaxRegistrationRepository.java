package com.xyz.platform.organization.tax.repository;

import com.xyz.platform.organization.tax.entity.BusinessTaxRegistration;
import com.xyz.platform.organization.tax.entity.BusinessTaxRegistrationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessTaxRegistrationRepository extends JpaRepository<BusinessTaxRegistration, BusinessTaxRegistrationId> {

    List<BusinessTaxRegistration> findByIdBusinessId(UUID businessId);
}
