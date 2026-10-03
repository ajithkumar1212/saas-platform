-- Phase 1 Control Plane
-- Tenant registry is the platform-owned mapping between tenant identity and tenant schema.
-- Schema names are generated and validated by the backend; clients never supply them.
-- IDs and timestamps are application managed.

CREATE TABLE public.tenant_registry (
    tenant_id uuid NOT NULL,
    tenant_code varchar(50) NOT NULL,
    tenant_name varchar(200) NOT NULL,
    schema_name varchar(63) NOT NULL,
    status varchar(30) NOT NULL,
    provisioning_status varchar(30) NOT NULL,
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,

    CONSTRAINT pk_tenant_registry PRIMARY KEY (tenant_id),
    CONSTRAINT uq_tenant_registry_tenant_code UNIQUE (tenant_code),
    CONSTRAINT uq_tenant_registry_schema_name UNIQUE (schema_name)
);

CREATE INDEX ix_tenant_registry_status
    ON public.tenant_registry (status);

CREATE INDEX ix_tenant_registry_provisioning_status
    ON public.tenant_registry (provisioning_status);
