package com.xyz.platform.tenancy.provider;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

/**
 * HikariCP connection provider that applies schema isolation via PostgreSQL search_path.
 * Validates schema identifiers to guard against SQL injection.
 */
@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private static final Logger log = LoggerFactory.getLogger(MultiTenantConnectionProviderImpl.class);
    private static final Pattern SAFE_SCHEMA_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,63}$");
    private static final String DEFAULT_SCHEMA = "public";

    private final DataSource dataSource;

    public MultiTenantConnectionProviderImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        try {
            if (tenantIdentifier != null && !DEFAULT_SCHEMA.equalsIgnoreCase(tenantIdentifier)) {
                validateSchemaName(tenantIdentifier);
                try (Statement statement = connection.createStatement()) {
                    statement.execute(String.format("SET search_path TO \"%s\", public", tenantIdentifier));
                    log.trace("Switched search_path to tenant schema: {}", tenantIdentifier);
                }
            } else {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET search_path TO public");
                }
            }
        } catch (SQLException e) {
            log.error("Failed to switch search_path to schema '{}'", tenantIdentifier, e);
            connection.close();
            throw e;
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET search_path TO public");
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to reset search_path to public on release", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType) ||
                MultiTenantConnectionProviderImpl.class.isAssignableFrom(unwrapType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> unwrapType) {
        if (isUnwrappableAs(unwrapType)) {
            return (T) this;
        }
        throw new IllegalArgumentException("Cannot unwrap to " + unwrapType.getName());
    }

    private void validateSchemaName(String schemaName) {
        if (schemaName == null || !SAFE_SCHEMA_PATTERN.matcher(schemaName).matches()) {
            throw new IllegalArgumentException("Invalid schema identifier format: " + schemaName);
        }
    }
}
