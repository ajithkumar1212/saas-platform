-- Phase 1 Control Plane
-- Canonical application catalog seed.
-- Fixed IDs make the platform catalog stable across environments.
-- Business Application rows reference these records by application_id.

INSERT INTO public.application (
    application_id,
    application_code,
    application_name,
    status,
    description,
    created_at,
    created_by
)
VALUES
(
    '10000000-0000-0000-0000-000000000001',
    'E_COMMERCE',
    'E-Commerce',
    'ACTIVE',
    'Online commerce capabilities for a Business.',
    TIMESTAMPTZ '2026-01-01 00:00:00+00',
    NULL
),
(
    '10000000-0000-0000-0000-000000000002',
    'DISTRIBUTION',
    'Distribution',
    'ACTIVE',
    'Distribution capabilities for a Business.',
    TIMESTAMPTZ '2026-01-01 00:00:00+00',
    NULL
),
(
    '10000000-0000-0000-0000-000000000003',
    'WAREHOUSE',
    'Warehouse',
    'ACTIVE',
    'Warehouse management capabilities for a Business.',
    TIMESTAMPTZ '2026-01-01 00:00:00+00',
    NULL
);
