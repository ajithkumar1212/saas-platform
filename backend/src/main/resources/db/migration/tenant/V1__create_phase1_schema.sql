-- Phase 1 PostgreSQL tenant schema DDL v6 (tax-integrity cleanup); canonical migration source.
-- UUIDs and timestamps are application managed; no database generation defaults.
-- Deletion behavior is RESTRICT/NO ACTION (PostgreSQL default); no blanket cascades.

CREATE TABLE tenant (
    tenant_id uuid,
    tenant_code varchar(50) NOT NULL,
    CONSTRAINT uq_tenant_tenant_code UNIQUE (tenant_code),
    tenant_name varchar(200) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_tenant PRIMARY KEY (tenant_id)
);

CREATE TABLE organization (
    organization_id uuid,
    tenant_id uuid NOT NULL,
    organization_code varchar(50) NOT NULL,
    organization_name varchar(250) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_organization PRIMARY KEY (organization_id)
);
CREATE UNIQUE INDEX uq_organization_tenant_id_organization_code ON organization (tenant_id, organization_code);

CREATE TABLE organization_legal_detail (
    legal_detail_id uuid,
    organization_id uuid NOT NULL,
    CONSTRAINT uq_organization_legal_detail_organization_id UNIQUE (organization_id),
    registration_number varchar(50),
    registration_type varchar(50),
    pan varchar(10),
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_organization_legal_detail PRIMARY KEY (legal_detail_id)
);

CREATE TABLE tax_registration (
    tax_registration_id uuid,
    organization_id uuid NOT NULL,
    registration_type varchar(30) NOT NULL,
    registration_number varchar(50),
    gstin varchar(15),
    country_id uuid,
    state_id uuid,
    status varchar(30) NOT NULL,
    valid_from timestamptz NOT NULL,
    valid_until timestamptz,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_tax_registration PRIMARY KEY (tax_registration_id),
    CONSTRAINT uq_tax_registration_id_organization_id UNIQUE (tax_registration_id, organization_id),
    CONSTRAINT ck_tax_registration_valid_until_range
        CHECK (
            valid_until IS NULL
            OR valid_until >= valid_from
        )
);

CREATE TABLE business_tax_registration (
    business_id uuid NOT NULL,
    tax_registration_id uuid NOT NULL,
    organization_id uuid NOT NULL,
    is_primary boolean NOT NULL DEFAULT FALSE,
    valid_from timestamptz NOT NULL,
    valid_until timestamptz,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_business_tax_registration
        PRIMARY KEY (business_id, tax_registration_id),
    CONSTRAINT ck_business_tax_registration_valid_until_range
        CHECK (
            valid_until IS NULL
            OR valid_until >= valid_from
        )
);

CREATE TABLE organization_address (
    address_id uuid,
    organization_id uuid NOT NULL,
    address_type varchar(30) NOT NULL,
    address_line1 varchar(250) NOT NULL,
    address_line2 varchar(250),
    landmark varchar(150),
    city varchar(100) NOT NULL,
    country_id uuid NOT NULL,
    state_id uuid NOT NULL,
    district_id uuid NOT NULL,
    postal_code varchar(20) NOT NULL,
    latitude decimal(10,7),
    longitude decimal(10,7),
    is_primary boolean NOT NULL DEFAULT FALSE,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_organization_address PRIMARY KEY (address_id)
);

CREATE TABLE organization_contact (
    contact_id uuid,
    organization_id uuid NOT NULL,
    contact_type varchar(30) NOT NULL,
    first_name varchar(100),
    last_name varchar(100),
    designation varchar(100),
    email varchar(254),
    mobile_country_code varchar(5),
    mobile_number varchar(15),
    alternate_mobile varchar(15),
    is_primary boolean NOT NULL DEFAULT FALSE,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_organization_contact PRIMARY KEY (contact_id)
);

CREATE TABLE company (
    company_id uuid,
    organization_id uuid NOT NULL,
    company_code varchar(50) NOT NULL,
    company_name varchar(250) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_company PRIMARY KEY (company_id),
    CONSTRAINT uq_company_company_id_organization_id UNIQUE (company_id, organization_id)
);
CREATE UNIQUE INDEX uq_company_organization_id_company_code ON company (organization_id, company_code);

CREATE TABLE company_legal_detail (
    legal_detail_id uuid,
    company_id uuid NOT NULL,
    CONSTRAINT uq_company_legal_detail_company_id UNIQUE (company_id),
    legal_name varchar(250),
    pan varchar(10),
    cin varchar(21),
    tan varchar(10),
    registration_number varchar(50),
    registration_type varchar(50),
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_company_legal_detail PRIMARY KEY (legal_detail_id)
);

CREATE TABLE company_address (
    address_id uuid,
    company_id uuid NOT NULL,
    address_type varchar(30) NOT NULL,
    address_line1 varchar(250) NOT NULL,
    address_line2 varchar(250),
    landmark varchar(150),
    city varchar(100) NOT NULL,
    country_id uuid NOT NULL,
    state_id uuid NOT NULL,
    district_id uuid NOT NULL,
    postal_code varchar(20) NOT NULL,
    latitude decimal(10,7),
    longitude decimal(10,7),
    is_primary boolean NOT NULL DEFAULT FALSE,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_company_address PRIMARY KEY (address_id)
);

CREATE TABLE company_contact (
    contact_id uuid,
    company_id uuid NOT NULL,
    contact_type varchar(30) NOT NULL,
    first_name varchar(100),
    last_name varchar(100),
    designation varchar(100),
    email varchar(254),
    mobile_country_code varchar(5),
    mobile_number varchar(15),
    alternate_mobile varchar(15),
    is_primary boolean NOT NULL DEFAULT FALSE,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_company_contact PRIMARY KEY (contact_id)
);

CREATE TABLE business (
    business_id uuid,
    company_id uuid NOT NULL,
    organization_id uuid NOT NULL,
    business_code varchar(50) NOT NULL,
    business_name varchar(250) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_business PRIMARY KEY (business_id),
    CONSTRAINT uq_business_business_id_organization_id UNIQUE (business_id, organization_id)
);
CREATE UNIQUE INDEX uq_business_company_id_business_code ON business (company_id, business_code);

CREATE TABLE brand (
    brand_id uuid,
    business_id uuid NOT NULL,
    brand_code varchar(50) NOT NULL,
    brand_name varchar(200) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_brand PRIMARY KEY (brand_id)
);
CREATE UNIQUE INDEX uq_brand_business_id_brand_code ON brand (business_id, brand_code);
CREATE UNIQUE INDEX uq_brand_business_id_brand_id ON brand (business_id, brand_id);

CREATE TABLE store (
    store_id uuid,
    business_id uuid NOT NULL,
    brand_id uuid,
    store_code varchar(50) NOT NULL,
    store_name varchar(200) NOT NULL,
    store_type varchar(30) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_store PRIMARY KEY (store_id)
);
CREATE UNIQUE INDEX uq_store_business_id_store_code ON store (business_id, store_code);

CREATE TABLE store_location (
    store_location_id uuid,
    store_id uuid NOT NULL,
    location_code varchar(50) NOT NULL,
    location_name varchar(150) NOT NULL,
    location_type varchar(30) NOT NULL,
    address_line1 varchar(250) NOT NULL,
    address_line2 varchar(250),
    landmark varchar(150),
    country_id uuid NOT NULL,
    state_id uuid NOT NULL,
    district_id uuid NOT NULL,
    city varchar(100) NOT NULL,
    postal_code varchar(20) NOT NULL,
    latitude decimal(10,7),
    longitude decimal(10,7),
    is_primary boolean NOT NULL DEFAULT FALSE,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_store_location PRIMARY KEY (store_location_id)
);
CREATE UNIQUE INDEX uq_store_location_store_id_location_code ON store_location (store_id, location_code);

CREATE TABLE warehouse (
    warehouse_id uuid,
    store_id uuid NOT NULL,
    warehouse_code varchar(50) NOT NULL,
    warehouse_name varchar(200) NOT NULL,
    warehouse_type varchar(30) NOT NULL,
    status varchar(30) NOT NULL,
    is_primary boolean NOT NULL DEFAULT FALSE,
    capacity numeric(15,2),
    capacity_unit varchar(20),
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_warehouse PRIMARY KEY (warehouse_id)
);
CREATE UNIQUE INDEX uq_warehouse_store_id_warehouse_code ON warehouse (store_id, warehouse_code);

CREATE TABLE warehouse_location (
    warehouse_location_id uuid,
    warehouse_id uuid NOT NULL,
    CONSTRAINT uq_warehouse_location_warehouse_id UNIQUE (warehouse_id),
    address_line1 varchar(250) NOT NULL,
    address_line2 varchar(250),
    landmark varchar(150),
    country_id uuid NOT NULL,
    state_id uuid NOT NULL,
    district_id uuid NOT NULL,
    city varchar(100) NOT NULL,
    postal_code varchar(20) NOT NULL,
    latitude decimal(10,7),
    longitude decimal(10,7),
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_warehouse_location PRIMARY KEY (warehouse_location_id)
);

CREATE TABLE pos (
    pos_id uuid,
    store_id uuid NOT NULL,
    store_location_id uuid,
    pos_code varchar(50) NOT NULL,
    pos_name varchar(150) NOT NULL,
    device_identifier varchar(150),
    is_primary boolean NOT NULL DEFAULT FALSE,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_pos PRIMARY KEY (pos_id)
);
CREATE UNIQUE INDEX uq_pos_store_id_pos_code ON pos (store_id, pos_code);

CREATE TABLE website (
    website_id uuid,
    store_id uuid NOT NULL,
    website_code varchar(50) NOT NULL,
    website_name varchar(200) NOT NULL,
    website_type varchar(30) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_website PRIMARY KEY (website_id)
);
CREATE UNIQUE INDEX uq_website_store_id_website_code ON website (store_id, website_code);

CREATE TABLE website_domain (
    website_domain_id uuid,
    website_id uuid NOT NULL,
    domain_name varchar(255) NOT NULL,
    CONSTRAINT uq_website_domain_domain_name UNIQUE (domain_name),
    domain_type varchar(30) NOT NULL,
    is_primary boolean NOT NULL DEFAULT FALSE,
    verification_status varchar(30) NOT NULL,
    verification_token varchar(255),
    verified_at timestamptz,
    ssl_status varchar(30),
    ssl_expiry_at timestamptz,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_website_domain PRIMARY KEY (website_domain_id)
);

CREATE TABLE channel (
    channel_id uuid,
    business_id uuid NOT NULL,
    channel_code varchar(50) NOT NULL,
    channel_name varchar(150) NOT NULL,
    channel_type varchar(30) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_channel PRIMARY KEY (channel_id)
);
CREATE UNIQUE INDEX uq_channel_business_id_channel_code ON channel (business_id, channel_code);

CREATE TABLE store_channel (
    store_id uuid NOT NULL,
    channel_id uuid NOT NULL,
    status varchar(30) NOT NULL,
    is_primary boolean NOT NULL DEFAULT FALSE,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_store_channel PRIMARY KEY (store_id, channel_id)
);

CREATE TABLE store_branding (
    store_branding_id uuid,
    store_id uuid NOT NULL,
    CONSTRAINT uq_store_branding_store_id UNIQUE (store_id),
    brand_name varchar(200),
    tagline varchar(500),
    logo_url varchar(1000),
    logo_mobile_url varchar(1000),
    favicon_url varchar(1000),
    primary_color varchar(20),
    secondary_color varchar(20),
    accent_color varchar(20),
    text_color varchar(20),
    background_color varchar(20),
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_store_branding PRIMARY KEY (store_branding_id)
);

CREATE TABLE store_theme (
    store_theme_id uuid,
    store_id uuid NOT NULL,
    CONSTRAINT uq_store_theme_store_id UNIQUE (store_id),
    theme_code varchar(50) NOT NULL,
    theme_name varchar(100),
    layout_code varchar(50),
    header_style varchar(50),
    footer_style varchar(50),
    product_card_style varchar(50),
    configuration jsonb,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_store_theme PRIMARY KEY (store_theme_id)
);

CREATE TABLE country (
    country_id uuid,
    country_code char(2) NOT NULL,
    CONSTRAINT uq_country_country_code UNIQUE (country_code),
    country_name varchar(100) NOT NULL,
    CONSTRAINT uq_country_country_name UNIQUE (country_name),
    phone_code varchar(10),
    currency_code varchar(3),
    status varchar(30) NOT NULL,
    sort_order int,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_country PRIMARY KEY (country_id)
);

CREATE TABLE state (
    state_id uuid,
    country_id uuid NOT NULL,
    state_code varchar(20) NOT NULL,
    state_name varchar(100) NOT NULL,
    status varchar(30) NOT NULL,
    sort_order int,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_state PRIMARY KEY (state_id)
);
CREATE UNIQUE INDEX uq_state_country_id_state_code ON state (country_id, state_code);
CREATE UNIQUE INDEX uq_state_country_id_state_name ON state (country_id, state_name);
CREATE UNIQUE INDEX uq_state_country_id_state_id ON state (country_id, state_id);

CREATE TABLE district (
    district_id uuid,
    state_id uuid NOT NULL,
    district_code varchar(20) NOT NULL,
    district_name varchar(100) NOT NULL,
    status varchar(30) NOT NULL,
    sort_order int,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_district PRIMARY KEY (district_id)
);
CREATE UNIQUE INDEX uq_district_state_id_district_code ON district (state_id, district_code);
CREATE UNIQUE INDEX uq_district_state_id_district_name ON district (state_id, district_name);
CREATE UNIQUE INDEX uq_district_state_id_district_id ON district (state_id, district_id);

CREATE TABLE user_account (
    user_id uuid,
    tenant_id uuid NOT NULL,
    username varchar(100) NOT NULL,
    email varchar(254) NOT NULL,
    mobile_country_code varchar(5),
    mobile_number varchar(15),
    first_name varchar(100) NOT NULL,
    last_name varchar(100),
    display_name varchar(200),
    password_hash varchar(255),
    status varchar(30) NOT NULL,
    email_verified boolean NOT NULL DEFAULT FALSE,
    mobile_verified boolean NOT NULL DEFAULT FALSE,
    last_login_at timestamptz,
    failed_login_count int NOT NULL DEFAULT 0,
    locked_until timestamptz,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_user_account PRIMARY KEY (user_id)
);
CREATE UNIQUE INDEX uq_user_account_tenant_id_username ON user_account (tenant_id, username);
CREATE UNIQUE INDEX uq_user_account_tenant_id_email ON user_account (tenant_id, email);

CREATE TABLE role (
    role_id uuid,
    tenant_id uuid,
    role_code varchar(50) NOT NULL,
    role_name varchar(100) NOT NULL,
    is_system_role boolean NOT NULL DEFAULT FALSE,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_role PRIMARY KEY (role_id)
);

CREATE TABLE permission (
    permission_id uuid,
    permission_code varchar(100) NOT NULL,
    CONSTRAINT uq_permission_permission_code UNIQUE (permission_code),
    permission_name varchar(150) NOT NULL,
    module varchar(50) NOT NULL,
    action varchar(50) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_permission PRIMARY KEY (permission_id)
);

CREATE TABLE role_permission (
    role_id uuid NOT NULL,
    permission_id uuid NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_role_permission PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_access_scope (
    access_scope_id uuid,
    user_id uuid NOT NULL,
    role_id uuid NOT NULL,
    scope_type varchar(30) NOT NULL,
    scope_id uuid NOT NULL,
    access_status varchar(30) NOT NULL,
    is_primary boolean NOT NULL DEFAULT FALSE,
    valid_from timestamptz NOT NULL,
    valid_until timestamptz,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_user_access_scope PRIMARY KEY (access_scope_id)
);
CREATE UNIQUE INDEX uq_user_access_scope_user_id_role_id_scope_type_scope_id ON user_access_scope (user_id, role_id, scope_type, scope_id);

CREATE TABLE hierarchy_change_history (
    history_id uuid,
    tenant_id uuid NOT NULL,
    entity_type varchar(30) NOT NULL,
    entity_id uuid NOT NULL,
    old_parent_type varchar(30),
    old_parent_id uuid,
    new_parent_type varchar(30),
    new_parent_id uuid,
    change_type varchar(30) NOT NULL,
    effective_from timestamptz NOT NULL,
    effective_until timestamptz,
    reason varchar(500),
    description varchar(1000),
    changed_by uuid,
    changed_at timestamptz NOT NULL,
    CONSTRAINT pk_hierarchy_change_history PRIMARY KEY (history_id)
);

CREATE TABLE user_access_history (
    history_id uuid,
    tenant_id uuid NOT NULL,
    user_id uuid NOT NULL,
    access_scope_id uuid,
    action varchar(30) NOT NULL,
    old_role_id uuid,
    new_role_id uuid,
    old_scope_type varchar(30),
    old_scope_id uuid,
    new_scope_type varchar(30),
    new_scope_id uuid,
    effective_from timestamptz NOT NULL,
    effective_until timestamptz,
    reason varchar(500),
    description varchar(1000),
    changed_by uuid,
    changed_at timestamptz NOT NULL,
    CONSTRAINT pk_user_access_history PRIMARY KEY (history_id)
);

CREATE TABLE organization_change_history (
    history_id uuid,
    tenant_id uuid NOT NULL,
    entity_type varchar(30) NOT NULL,
    entity_id uuid NOT NULL,
    field_name varchar(100) NOT NULL,
    old_value text,
    new_value text,
    action varchar(30) NOT NULL,
    reason varchar(500),
    description varchar(1000),
    changed_by uuid,
    changed_at timestamptz NOT NULL,
    CONSTRAINT pk_organization_change_history PRIMARY KEY (history_id)
);

CREATE TABLE user_account_history (
    history_id uuid,
    tenant_id uuid NOT NULL,
    user_id uuid NOT NULL,
    action varchar(50) NOT NULL,
    old_status varchar(30),
    new_status varchar(30),
    reason varchar(500),
    description varchar(1000),
    metadata jsonb,
    changed_by uuid,
    changed_at timestamptz NOT NULL,
    CONSTRAINT pk_user_account_history PRIMARY KEY (history_id)
);

CREATE TABLE audit_log (
    audit_id uuid,
    tenant_id uuid NOT NULL,
    user_id uuid,
    entity_type varchar(50) NOT NULL,
    entity_id uuid NOT NULL,
    action varchar(50) NOT NULL,
    old_data jsonb,
    new_data jsonb,
    ip_address varchar(45),
    user_agent varchar(1000),
    request_id varchar(100),
    description varchar(1000),
    created_at timestamptz NOT NULL,
    CONSTRAINT pk_audit_log PRIMARY KEY (audit_id)
);

CREATE TABLE reference_type (
    reference_type_id uuid,
    type_code varchar(50) NOT NULL,
    CONSTRAINT uq_reference_type_type_code UNIQUE (type_code),
    type_name varchar(100) NOT NULL,
    status varchar(30) NOT NULL,
    description varchar(1000),
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_reference_type PRIMARY KEY (reference_type_id)
);

CREATE TABLE reference_value (
    reference_value_id uuid,
    reference_type_id uuid NOT NULL,
    value_code varchar(50) NOT NULL,
    value_name varchar(100) NOT NULL,
    description varchar(1000),
    sort_order int,
    is_default boolean NOT NULL DEFAULT FALSE,
    is_system_value boolean NOT NULL DEFAULT TRUE,
    status varchar(30) NOT NULL,
    created_at timestamptz NOT NULL,
    created_by uuid,
    updated_at timestamptz,
    updated_by uuid,
    CONSTRAINT pk_reference_value PRIMARY KEY (reference_value_id)
);
CREATE UNIQUE INDEX uq_reference_value_reference_type_id_value_code ON reference_value (reference_type_id, value_code);

ALTER TABLE organization ADD CONSTRAINT fk_organization_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id);
ALTER TABLE organization_legal_detail ADD CONSTRAINT fk_organization_legal_detail_organization_id FOREIGN KEY (organization_id) REFERENCES organization (organization_id);
ALTER TABLE tax_registration ADD CONSTRAINT fk_tax_registration_organization_id FOREIGN KEY (organization_id) REFERENCES organization (organization_id);
ALTER TABLE tax_registration ADD CONSTRAINT fk_tax_registration_country_id FOREIGN KEY (country_id) REFERENCES country (country_id);
ALTER TABLE tax_registration ADD CONSTRAINT fk_tax_registration_state_id FOREIGN KEY (state_id) REFERENCES state (state_id);

ALTER TABLE tax_registration
ADD CONSTRAINT fk_tax_registration_country_state
FOREIGN KEY (country_id, state_id)
REFERENCES state (country_id, state_id);
ALTER TABLE business_tax_registration ADD CONSTRAINT fk_business_tax_registration_business_organization FOREIGN KEY (business_id, organization_id) REFERENCES business (business_id, organization_id);
ALTER TABLE business_tax_registration ADD CONSTRAINT fk_business_tax_registration_tax_organization FOREIGN KEY (tax_registration_id, organization_id) REFERENCES tax_registration (tax_registration_id, organization_id);
ALTER TABLE organization_address ADD CONSTRAINT fk_organization_address_organization_id FOREIGN KEY (organization_id) REFERENCES organization (organization_id);
ALTER TABLE organization_contact ADD CONSTRAINT fk_organization_contact_organization_id FOREIGN KEY (organization_id) REFERENCES organization (organization_id);
ALTER TABLE company ADD CONSTRAINT fk_company_organization_id FOREIGN KEY (organization_id) REFERENCES organization (organization_id);
ALTER TABLE company_legal_detail ADD CONSTRAINT fk_company_legal_detail_company_id FOREIGN KEY (company_id) REFERENCES company (company_id);
ALTER TABLE company_address ADD CONSTRAINT fk_company_address_company_id FOREIGN KEY (company_id) REFERENCES company (company_id);
ALTER TABLE company_contact ADD CONSTRAINT fk_company_contact_company_id FOREIGN KEY (company_id) REFERENCES company (company_id);
ALTER TABLE business ADD CONSTRAINT fk_business_company_organization FOREIGN KEY (company_id, organization_id) REFERENCES company (company_id, organization_id);
ALTER TABLE brand ADD CONSTRAINT fk_brand_business_id FOREIGN KEY (business_id) REFERENCES business (business_id);
ALTER TABLE store ADD CONSTRAINT fk_store_business_id FOREIGN KEY (business_id) REFERENCES business (business_id);
ALTER TABLE store_location ADD CONSTRAINT fk_store_location_store_id FOREIGN KEY (store_id) REFERENCES store (store_id);
ALTER TABLE warehouse ADD CONSTRAINT fk_warehouse_store_id FOREIGN KEY (store_id) REFERENCES store (store_id);
ALTER TABLE warehouse_location ADD CONSTRAINT fk_warehouse_location_warehouse_id FOREIGN KEY (warehouse_id) REFERENCES warehouse (warehouse_id);
ALTER TABLE pos ADD CONSTRAINT fk_pos_store_id FOREIGN KEY (store_id) REFERENCES store (store_id);
ALTER TABLE website ADD CONSTRAINT fk_website_store_id FOREIGN KEY (store_id) REFERENCES store (store_id);
ALTER TABLE website_domain ADD CONSTRAINT fk_website_domain_website_id FOREIGN KEY (website_id) REFERENCES website (website_id);
ALTER TABLE channel ADD CONSTRAINT fk_channel_business_id FOREIGN KEY (business_id) REFERENCES business (business_id);
ALTER TABLE store_channel ADD CONSTRAINT fk_store_channel_store_id FOREIGN KEY (store_id) REFERENCES store (store_id);
ALTER TABLE store_channel ADD CONSTRAINT fk_store_channel_channel_id FOREIGN KEY (channel_id) REFERENCES channel (channel_id);
ALTER TABLE store_branding ADD CONSTRAINT fk_store_branding_store_id FOREIGN KEY (store_id) REFERENCES store (store_id);
ALTER TABLE store_theme ADD CONSTRAINT fk_store_theme_store_id FOREIGN KEY (store_id) REFERENCES store (store_id);
ALTER TABLE organization_address ADD CONSTRAINT fk_organization_address_country_id FOREIGN KEY (country_id) REFERENCES country (country_id);
ALTER TABLE organization_address ADD CONSTRAINT fk_organization_address_state_id FOREIGN KEY (state_id) REFERENCES state (state_id);
ALTER TABLE organization_address ADD CONSTRAINT fk_organization_address_district_id FOREIGN KEY (district_id) REFERENCES district (district_id);
ALTER TABLE company_address ADD CONSTRAINT fk_company_address_country_id FOREIGN KEY (country_id) REFERENCES country (country_id);
ALTER TABLE company_address ADD CONSTRAINT fk_company_address_state_id FOREIGN KEY (state_id) REFERENCES state (state_id);
ALTER TABLE company_address ADD CONSTRAINT fk_company_address_district_id FOREIGN KEY (district_id) REFERENCES district (district_id);
ALTER TABLE store_location ADD CONSTRAINT fk_store_location_country_id FOREIGN KEY (country_id) REFERENCES country (country_id);
ALTER TABLE store_location ADD CONSTRAINT fk_store_location_state_id FOREIGN KEY (state_id) REFERENCES state (state_id);
ALTER TABLE store_location ADD CONSTRAINT fk_store_location_district_id FOREIGN KEY (district_id) REFERENCES district (district_id);
ALTER TABLE warehouse_location ADD CONSTRAINT fk_warehouse_location_country_id FOREIGN KEY (country_id) REFERENCES country (country_id);
ALTER TABLE warehouse_location ADD CONSTRAINT fk_warehouse_location_state_id FOREIGN KEY (state_id) REFERENCES state (state_id);
ALTER TABLE warehouse_location ADD CONSTRAINT fk_warehouse_location_district_id FOREIGN KEY (district_id) REFERENCES district (district_id);
ALTER TABLE state ADD CONSTRAINT fk_state_country_id FOREIGN KEY (country_id) REFERENCES country (country_id);
ALTER TABLE district ADD CONSTRAINT fk_district_state_id FOREIGN KEY (state_id) REFERENCES state (state_id);
ALTER TABLE user_account ADD CONSTRAINT fk_user_account_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id);
ALTER TABLE role_permission ADD CONSTRAINT fk_role_permission_role_id FOREIGN KEY (role_id) REFERENCES role (role_id);
ALTER TABLE role_permission ADD CONSTRAINT fk_role_permission_permission_id FOREIGN KEY (permission_id) REFERENCES permission (permission_id);
ALTER TABLE user_access_scope ADD CONSTRAINT fk_user_access_scope_user_id FOREIGN KEY (user_id) REFERENCES user_account (user_id);
ALTER TABLE user_access_scope ADD CONSTRAINT fk_user_access_scope_role_id FOREIGN KEY (role_id) REFERENCES role (role_id);
ALTER TABLE hierarchy_change_history ADD CONSTRAINT fk_hierarchy_change_history_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id);
ALTER TABLE user_access_history ADD CONSTRAINT fk_user_access_history_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id);
ALTER TABLE user_access_history ADD CONSTRAINT fk_user_access_history_user_id FOREIGN KEY (user_id) REFERENCES user_account (user_id);
ALTER TABLE organization_change_history ADD CONSTRAINT fk_organization_change_history_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id);
ALTER TABLE user_account_history ADD CONSTRAINT fk_user_account_history_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id);
ALTER TABLE user_account_history ADD CONSTRAINT fk_user_account_history_user_id FOREIGN KEY (user_id) REFERENCES user_account (user_id);
ALTER TABLE audit_log ADD CONSTRAINT fk_audit_log_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id);
ALTER TABLE reference_value ADD CONSTRAINT fk_reference_value_reference_type_id FOREIGN KEY (reference_type_id) REFERENCES reference_type (reference_type_id);
ALTER TABLE pos ADD CONSTRAINT fk_pos_store_location_id FOREIGN KEY (store_location_id) REFERENCES store_location (store_location_id);
ALTER TABLE role ADD CONSTRAINT fk_role_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id);
ALTER TABLE hierarchy_change_history ADD CONSTRAINT fk_hierarchy_change_history_changed_by FOREIGN KEY (changed_by) REFERENCES user_account (user_id);
ALTER TABLE user_access_history ADD CONSTRAINT fk_user_access_history_access_scope_id FOREIGN KEY (access_scope_id) REFERENCES user_access_scope (access_scope_id);
ALTER TABLE user_access_history ADD CONSTRAINT fk_user_access_history_old_role_id FOREIGN KEY (old_role_id) REFERENCES role (role_id);
ALTER TABLE user_access_history ADD CONSTRAINT fk_user_access_history_new_role_id FOREIGN KEY (new_role_id) REFERENCES role (role_id);
ALTER TABLE user_access_history ADD CONSTRAINT fk_user_access_history_changed_by FOREIGN KEY (changed_by) REFERENCES user_account (user_id);
ALTER TABLE organization_change_history ADD CONSTRAINT fk_organization_change_history_changed_by FOREIGN KEY (changed_by) REFERENCES user_account (user_id);
ALTER TABLE user_account_history ADD CONSTRAINT fk_user_account_history_changed_by FOREIGN KEY (changed_by) REFERENCES user_account (user_id);
ALTER TABLE audit_log ADD CONSTRAINT fk_audit_log_user_id FOREIGN KEY (user_id) REFERENCES user_account (user_id);
ALTER TABLE store ADD CONSTRAINT fk_store_business_brand FOREIGN KEY (business_id, brand_id) REFERENCES brand (business_id, brand_id);
CREATE UNIQUE INDEX uq_website_domain_primary ON website_domain (website_id) WHERE is_primary = TRUE;
CREATE UNIQUE INDEX uq_store_location_primary ON store_location (store_id) WHERE is_primary = TRUE;
CREATE UNIQUE INDEX uq_warehouse_primary ON warehouse (store_id) WHERE is_primary = TRUE;
CREATE UNIQUE INDEX uq_business_tax_registration_primary
ON business_tax_registration (business_id)
WHERE is_primary = TRUE;
CREATE UNIQUE INDEX uq_tax_registration_gstin
ON tax_registration (gstin)
WHERE gstin IS NOT NULL;
CREATE UNIQUE INDEX uq_role_global_code ON role (role_code) WHERE tenant_id IS NULL;
CREATE UNIQUE INDEX uq_role_tenant_code ON role (tenant_id, role_code) WHERE tenant_id IS NOT NULL;
ALTER TABLE user_access_scope ADD CONSTRAINT ck_user_access_scope_scope_type CHECK (scope_type IN ('ORGANIZATION','COMPANY','BUSINESS','BRAND','STORE','CHANNEL'));
ALTER TABLE organization_address ADD CONSTRAINT ck_organization_address_latitude CHECK (latitude BETWEEN -90 AND 90);
ALTER TABLE organization_address ADD CONSTRAINT ck_organization_address_longitude CHECK (longitude BETWEEN -180 AND 180);
ALTER TABLE company_address ADD CONSTRAINT ck_company_address_latitude CHECK (latitude BETWEEN -90 AND 90);
ALTER TABLE company_address ADD CONSTRAINT ck_company_address_longitude CHECK (longitude BETWEEN -180 AND 180);
ALTER TABLE store_location ADD CONSTRAINT ck_store_location_latitude CHECK (latitude BETWEEN -90 AND 90);
ALTER TABLE store_location ADD CONSTRAINT ck_store_location_longitude CHECK (longitude BETWEEN -180 AND 180);
ALTER TABLE warehouse_location ADD CONSTRAINT ck_warehouse_location_latitude CHECK (latitude BETWEEN -90 AND 90);
ALTER TABLE warehouse_location ADD CONSTRAINT ck_warehouse_location_longitude CHECK (longitude BETWEEN -180 AND 180);
ALTER TABLE user_access_scope ADD CONSTRAINT ck_user_access_scope_valid_until_range CHECK (valid_until IS NULL OR valid_until >= valid_from);
ALTER TABLE hierarchy_change_history ADD CONSTRAINT ck_hierarchy_change_history_effective_until_range CHECK (effective_until IS NULL OR effective_until >= effective_from);
ALTER TABLE user_access_history ADD CONSTRAINT ck_user_access_history_effective_until_range CHECK (effective_until IS NULL OR effective_until >= effective_from);
