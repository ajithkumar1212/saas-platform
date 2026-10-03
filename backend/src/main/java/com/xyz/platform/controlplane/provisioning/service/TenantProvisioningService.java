package com.xyz.platform.controlplane.provisioning.service;

import com.xyz.platform.controlplane.provisioning.dto.TenantProvisionRequest;
import com.xyz.platform.controlplane.provisioning.dto.TenantProvisionResponse;
import com.xyz.platform.controlplane.tenant.entity.TenantRegistry;
import com.xyz.platform.controlplane.tenant.repository.TenantRegistryRepository;
import com.xyz.platform.shared.exception.BusinessException;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Control-plane service responsible for tenant schema lifecycle:
 * 1. Checks and provisions public.tenant_registry record
 * 2. Executes CREATE SCHEMA via JdbcTemplate
 * 3. Programmatically executes Flyway migrations for tenant plane (tenant V1, V2, V3)
 * 4. Activates tenant in public.tenant_registry
 */
@Service
public class TenantProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(TenantProvisioningService.class);
    private static final Pattern SAFE_SCHEMA_PATTERN = Pattern.compile("^[a-z0-9_]{1,63}$");

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final TenantRegistryRepository tenantRegistryRepository;

    @Value("${platform.tenancy.tenant-migration-locations:classpath:db/migration/tenant}")
    private String tenantMigrationLocations;

    @Value("${platform.tenancy.schema-prefix:tenant_}")
    private String schemaPrefix;

    public TenantProvisioningService(JdbcTemplate jdbcTemplate,
                                     DataSource dataSource,
                                     TenantRegistryRepository tenantRegistryRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.tenantRegistryRepository = tenantRegistryRepository;
    }

    public TenantProvisionResponse provisionTenant(TenantProvisionRequest request) {
        String cleanCode = request.getTenantCode().trim().toLowerCase().replaceAll("-", "_");
        String schemaName = schemaPrefix + cleanCode;

        if (schemaName.length() > 63) {
            schemaName = schemaName.substring(0, 63);
        }

        if (!SAFE_SCHEMA_PATTERN.matcher(schemaName).matches()) {
            throw new BusinessException("INVALID_SCHEMA_NAME", "Generated schema name contains invalid characters: " + schemaName);
        }

        if (tenantRegistryRepository.existsByTenantCode(request.getTenantCode())) {
            throw new BusinessException("TENANT_CODE_EXISTS", "Tenant code already exists: " + request.getTenantCode());
        }

        if (tenantRegistryRepository.existsBySchemaName(schemaName)) {
            throw new BusinessException("SCHEMA_NAME_EXISTS", "Schema name already assigned: " + schemaName);
        }

        UUID tenantId = UUID.randomUUID();
        log.info("Starting provisioning for tenant '{}' with schema '{}' (ID: {})", request.getTenantCode(), schemaName, tenantId);

        // 1. Register in public.tenant_registry as IN_PROGRESS
        TenantRegistry registry = TenantRegistry.builder()
                .tenantId(tenantId)
                .tenantCode(request.getTenantCode())
                .tenantName(request.getTenantName())
                .schemaName(schemaName)
                .status("PENDING")
                .provisioningStatus("IN_PROGRESS")
                .build();

        registry = tenantRegistryRepository.saveAndFlush(registry);

        try {
            // 2. Control plane executes CREATE SCHEMA via JdbcTemplate
            createSchemaIfNotExists(schemaName);

            // 3. Programmatic Flyway executes tenant schema migrations
            applyTenantMigrations(schemaName);

            // 4. Bootstrap tenant-domain tenant row in tenant schema
            bootstrapTenantDomain(schemaName, tenantId, request.getTenantCode(), request.getTenantName());

            // 5. Update status to ACTIVE / COMPLETED
            registry.setStatus("ACTIVE");
            registry.setProvisioningStatus("COMPLETED");
            registry = tenantRegistryRepository.saveAndFlush(registry);

            log.info("Tenant '{}' successfully provisioned with schema '{}'", request.getTenantCode(), schemaName);

            return TenantProvisionResponse.builder()
                    .tenantId(registry.getTenantId())
                    .tenantCode(registry.getTenantCode())
                    .tenantName(registry.getTenantName())
                    .schemaName(registry.getSchemaName())
                    .status(registry.getStatus())
                    .provisioningStatus(registry.getProvisioningStatus())
                    .createdAt(registry.getCreatedAt())
                    .build();

        } catch (Exception e) {
            log.error("Failed to provision tenant schema '{}'", schemaName, e);
            registry.setStatus("FAILED");
            registry.setProvisioningStatus("FAILED");
            tenantRegistryRepository.saveAndFlush(registry);
            throw new BusinessException("PROVISIONING_FAILED", "Failed to provision tenant schema: " + e.getMessage());
        }
    }

    private void createSchemaIfNotExists(String schemaName) {
        log.info("Creating schema if not exists: {}", schemaName);
        // Validated against SAFE_SCHEMA_PATTERN above
        jdbcTemplate.execute(String.format("CREATE SCHEMA IF NOT EXISTS \"%s\"", schemaName));
    }

    private void applyTenantMigrations(String schemaName) {
        log.info("Applying programmatic Flyway migrations to schema: {}", schemaName);
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .defaultSchema(schemaName)
                .locations(tenantMigrationLocations)
                .baselineOnMigrate(true)
                .table("flyway_schema_history")
                .load();

        flyway.migrate();
        log.info("Flyway migrations completed successfully for schema: {}", schemaName);
    }

    private void bootstrapTenantDomain(String schemaName, UUID tenantId, String tenantCode, String tenantName) {
        log.info("Bootstrapping tenant-domain tenant row in schema: {}", schemaName);
        java.time.Instant now = java.time.Instant.now();
        jdbcTemplate.update(
                String.format("INSERT INTO \"%s\".tenant (tenant_id, tenant_code, tenant_name, status, created_at, updated_at) VALUES (?, ?, ?, 'ACTIVE', ?, ?)", schemaName),
                tenantId, tenantCode, tenantName, java.sql.Timestamp.from(now), java.sql.Timestamp.from(now)
        );
    }
}
