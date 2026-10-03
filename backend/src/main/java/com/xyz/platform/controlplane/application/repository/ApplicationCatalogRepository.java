package com.xyz.platform.controlplane.application.repository;

import com.xyz.platform.controlplane.application.entity.ApplicationCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationCatalogRepository extends JpaRepository<ApplicationCatalog, UUID> {

    Optional<ApplicationCatalog> findByApplicationCode(String applicationCode);

    boolean existsByApplicationCode(String applicationCode);
}
