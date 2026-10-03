-- Phase 1 tenant reference data. Flyway owns the transaction boundary.
-- Version: V2
-- Approved reference types and values.
-- No roles, permissions, geography, or other seed data are inserted here.
-- UUIDs are deterministic literals so the migration does not depend on DB UUID generation.
-- ============================================================
-- 1. REFERENCE TYPES
-- ============================================================

INSERT INTO reference_type
    (reference_type_id, type_code, type_name, status, description, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000201', 'STATUS', 'Status', 'ACTIVE', 'Common lifecycle status values used across Phase 1 entities.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000202', 'CHANNEL_TYPE', 'Channel Type', 'ACTIVE', 'Business sales and operating channel types.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000203', 'BUSINESS_TYPE', 'Business Type', 'ACTIVE', 'Business model and operating type values.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000204', 'TAX_REGISTRATION_TYPE', 'Tax Registration Type', 'ACTIVE', 'Tax registration classification values.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000205', 'ORGANIZATION_TYPE', 'Organization Type', 'ACTIVE', 'Organization structure and type values.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000206', 'COMPANY_TYPE', 'Company Type', 'ACTIVE', 'Company and legal structure type values.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 2. REFERENCE VALUES
-- ============================================================

-- STATUS
INSERT INTO reference_value
    (reference_value_id, reference_type_id, value_code, value_name, description,
     sort_order, is_default, is_system_value, status, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000201', 'ACTIVE', 'Active', 'Entity is operational.', 1, TRUE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000201', 'INACTIVE', 'Inactive', 'Entity is temporarily disabled.', 2, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000303', '00000000-0000-0000-0000-000000000201', 'SUSPENDED', 'Suspended', 'Entity access or operations are suspended.', 3, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000304', '00000000-0000-0000-0000-000000000201', 'ARCHIVED', 'Archived', 'Entity is no longer operational.', 4, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CHANNEL_TYPE
INSERT INTO reference_value
    (reference_value_id, reference_type_id, value_code, value_name, description,
     sort_order, is_default, is_system_value, status, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000305', '00000000-0000-0000-0000-000000000202', 'ONLINE', 'Online', 'Online sales channel.', 1, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000306', '00000000-0000-0000-0000-000000000202', 'OFFLINE', 'Offline', 'Offline sales channel.', 2, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000307', '00000000-0000-0000-0000-000000000202', 'MARKETPLACE', 'Marketplace', 'Third-party marketplace channel.', 3, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000308', '00000000-0000-0000-0000-000000000202', 'SOCIAL_COMMERCE', 'Social Commerce', 'Social-media-based commerce channel.', 4, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000309', '00000000-0000-0000-0000-000000000202', 'MOBILE_APP', 'Mobile App', 'Mobile application sales channel.', 5, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000310', '00000000-0000-0000-0000-000000000202', 'POS', 'Point of Sale', 'Point-of-sale channel.', 6, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000311', '00000000-0000-0000-0000-000000000202', 'B2B', 'Business to Business', 'Business-to-business sales channel.', 7, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- BUSINESS_TYPE
INSERT INTO reference_value
    (reference_value_id, reference_type_id, value_code, value_name, description,
     sort_order, is_default, is_system_value, status, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000312', '00000000-0000-0000-0000-000000000203', 'RETAIL', 'Retail', 'Retail business.', 1, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000313', '00000000-0000-0000-0000-000000000203', 'WHOLESALE', 'Wholesale', 'Wholesale business.', 2, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000314', '00000000-0000-0000-0000-000000000203', 'DISTRIBUTION', 'Distribution', 'Distribution business.', 3, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000315', '00000000-0000-0000-0000-000000000203', 'MANUFACTURING', 'Manufacturing', 'Manufacturing business.', 4, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000316', '00000000-0000-0000-0000-000000000203', 'SERVICE', 'Service', 'Service-oriented business.', 5, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000317', '00000000-0000-0000-0000-000000000203', 'E_COMMERCE', 'E-Commerce', 'Electronic commerce business.', 6, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000318', '00000000-0000-0000-0000-000000000203', 'B2B', 'Business to Business', 'Business-to-business operating model.', 7, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000319', '00000000-0000-0000-0000-000000000203', 'B2C', 'Business to Consumer', 'Business-to-consumer operating model.', 8, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000320', '00000000-0000-0000-0000-000000000203', 'D2C', 'Direct to Consumer', 'Direct-to-consumer operating model.', 9, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- TAX_REGISTRATION_TYPE
INSERT INTO reference_value
    (reference_value_id, reference_type_id, value_code, value_name, description,
     sort_order, is_default, is_system_value, status, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000321', '00000000-0000-0000-0000-000000000204', 'GST', 'Goods and Services Tax', 'GST registration.', 1, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000322', '00000000-0000-0000-0000-000000000204', 'VAT', 'Value Added Tax', 'VAT registration.', 2, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000323', '00000000-0000-0000-0000-000000000204', 'SALES_TAX', 'Sales Tax', 'Sales tax registration.', 3, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000324', '00000000-0000-0000-0000-000000000204', 'TAX_ID', 'Tax Identification', 'General tax identification registration.', 4, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000325', '00000000-0000-0000-0000-000000000204', 'OTHER', 'Other', 'Other tax registration type.', 5, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ORGANIZATION_TYPE
INSERT INTO reference_value
    (reference_value_id, reference_type_id, value_code, value_name, description,
     sort_order, is_default, is_system_value, status, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000326', '00000000-0000-0000-0000-000000000205', 'GROUP', 'Group', 'Business or corporate group.', 1, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000327', '00000000-0000-0000-0000-000000000205', 'HOLDING_COMPANY', 'Holding Company', 'Holding company structure.', 2, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000328', '00000000-0000-0000-0000-000000000205', 'CORPORATION', 'Corporation', 'Corporate organization.', 3, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000329', '00000000-0000-0000-0000-000000000205', 'PARTNERSHIP', 'Partnership', 'Partnership organization.', 4, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000330', '00000000-0000-0000-0000-000000000205', 'PROPRIETORSHIP', 'Proprietorship', 'Proprietorship organization.', 5, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000331', '00000000-0000-0000-0000-000000000205', 'TRUST', 'Trust', 'Trust organization.', 6, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000332', '00000000-0000-0000-0000-000000000205', 'SOCIETY', 'Society', 'Society organization.', 7, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000333', '00000000-0000-0000-0000-000000000205', 'NON_PROFIT', 'Non-Profit', 'Non-profit organization.', 8, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000334', '00000000-0000-0000-0000-000000000205', 'OTHER', 'Other', 'Other organization type.', 9, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- COMPANY_TYPE
INSERT INTO reference_value
    (reference_value_id, reference_type_id, value_code, value_name, description,
     sort_order, is_default, is_system_value, status, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000335', '00000000-0000-0000-0000-000000000206', 'PRIVATE_LIMITED', 'Private Limited', 'Private limited company.', 1, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000336', '00000000-0000-0000-0000-000000000206', 'PUBLIC_LIMITED', 'Public Limited', 'Public limited company.', 2, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000337', '00000000-0000-0000-0000-000000000206', 'LLP', 'Limited Liability Partnership', 'Limited liability partnership.', 3, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000338', '00000000-0000-0000-0000-000000000206', 'PARTNERSHIP', 'Partnership', 'Partnership company or organization.', 4, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000339', '00000000-0000-0000-0000-000000000206', 'PROPRIETORSHIP', 'Proprietorship', 'Proprietorship company or organization.', 5, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000340', '00000000-0000-0000-0000-000000000206', 'ONE_PERSON_COMPANY', 'One Person Company', 'One person company.', 6, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000341', '00000000-0000-0000-0000-000000000206', 'GOVERNMENT', 'Government', 'Government-owned or operated company.', 7, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000342', '00000000-0000-0000-0000-000000000206', 'OTHER', 'Other', 'Other company type.', 8, FALSE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
