package com.xyz.platform.organization.tax.repository;

import com.xyz.platform.organization.tax.entity.TaxRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaxRegistrationRepository extends JpaRepository<TaxRegistration, UUID> {

    List<TaxRegistration> findByOrganizationId(UUID organizationId);
}
