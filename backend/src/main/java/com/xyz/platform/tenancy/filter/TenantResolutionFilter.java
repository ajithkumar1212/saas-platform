package com.xyz.platform.tenancy.filter;

import com.xyz.platform.controlplane.tenant.entity.TenantRegistry;
import com.xyz.platform.controlplane.tenant.repository.TenantRegistryRepository;
import com.xyz.platform.shared.exception.TenantNotFoundException;
import com.xyz.platform.tenancy.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter that intercepts incoming HTTP requests, resolves tenant identity from trusted
 * server-side registry (never blindly trusting client input), and populates TenantContext.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class TenantResolutionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantResolutionFilter.class);
    public static final String TENANT_HEADER = "X-Tenant-Code";

    private final TenantRegistryRepository tenantRegistryRepository;

    public TenantResolutionFilter(TenantRegistryRepository tenantRegistryRepository) {
        this.tenantRegistryRepository = tenantRegistryRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Bypass preflight OPTIONS requests for CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bypass public/infrastructure endpoints
        if (isPublicOrInfraEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tenantCode = request.getHeader(TENANT_HEADER);

        try {
            if (tenantCode != null && !tenantCode.isBlank()) {
                Optional<TenantRegistry> tenantOpt = tenantRegistryRepository.findByTenantCode(tenantCode.trim());

                if (tenantOpt.isEmpty()) {
                    log.warn("Access attempt with unknown tenant code: {}", tenantCode);
                    throw new TenantNotFoundException("Tenant not recognized: " + tenantCode);
                }

                TenantRegistry tenant = tenantOpt.get();
                if (!"ACTIVE".equalsIgnoreCase(tenant.getStatus()) ||
                        !"COMPLETED".equalsIgnoreCase(tenant.getProvisioningStatus())) {
                    log.warn("Access attempt to inactive or unprovisioned tenant: {}", tenantCode);
                    throw new TenantNotFoundException("Tenant is not active or provisioning incomplete: " + tenantCode);
                }

                TenantContext.setTenant(tenant.getTenantId(), tenant.getTenantCode(), tenant.getSchemaName());
                log.trace("Resolved tenant context for code '{}' -> schema '{}'", tenantCode, tenant.getSchemaName());
            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }

    private boolean isPublicOrInfraEndpoint(String path) {
        return path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api/v1/provisioning") ||
                path.startsWith("/api/v1/auth") ||
                path.equals("/") ||
                path.equals("/favicon.ico");
    }
}
