-- Phase 1 Control Plane
-- Platform-owned catalog of selectable Business Applications.
-- Business-specific enablement is stored in tenant.business_application.

CREATE TABLE public.application (
    application_id uuid NOT NULL,
    application_code varchar(50) NOT NULL,
    application_name varchar(150) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,

    CONSTRAINT pk_application PRIMARY KEY (application_id),
    CONSTRAINT uq_application_application_code UNIQUE (application_code)
);
