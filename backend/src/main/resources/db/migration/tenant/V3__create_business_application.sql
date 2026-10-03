-- Phase 1 Tenant Plane
-- Business-specific enablement of a platform-owned application.

CREATE TABLE business_application (
    business_application_id uuid NOT NULL,
    business_id uuid NOT NULL,
    application_id uuid NOT NULL,
    status varchar(30) NOT NULL,
    enabled_at timestamptz NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,

    CONSTRAINT pk_business_application PRIMARY KEY (business_application_id),
    CONSTRAINT uq_business_application_business_application
        UNIQUE (business_id, application_id),
    CONSTRAINT fk_business_application_business
        FOREIGN KEY (business_id)
        REFERENCES business (business_id),
    CONSTRAINT fk_business_application_application
        FOREIGN KEY (application_id)
        REFERENCES public.application (application_id)
);

CREATE INDEX ix_business_application_application_id
    ON business_application (application_id);
