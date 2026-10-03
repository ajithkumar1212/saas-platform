# Phase 1 Final Data Dictionary — 40 Tenant Domain Tables

Generated from the finalized PostgreSQL DDL v5. Types, nullability, defaults, keys, and finalized relational constraints below follow that authoritative DDL.

## `tenant`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `tenant_id` | `uuid` | NULL | — | PK |
| `tenant_code` | `varchar(50)` | NOT NULL | — | UNIQUE |
| `tenant_name` | `varchar(200)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `organization`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `organization_id` | `uuid` | NULL | — | PK |
| `tenant_id` | `uuid` | NOT NULL | — |  |
| `organization_code` | `varchar(50)` | NOT NULL | — |  |
| `organization_name` | `varchar(250)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `organization_legal_detail`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `legal_detail_id` | `uuid` | NULL | — | PK |
| `organization_id` | `uuid` | NOT NULL | — | UNIQUE |
| `registration_number` | `varchar(50)` | NULL | — |  |
| `registration_type` | `varchar(50)` | NULL | — |  |
| `pan` | `varchar(10)` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `tax_registration`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `tax_registration_id` | `uuid` | NULL | — | PK |
| `organization_id` | `uuid` | NOT NULL | — |  |
| `registration_type` | `varchar(30)` | NOT NULL | — |  |
| `registration_number` | `varchar(50)` | NULL | — |  |
| `gstin` | `varchar(15)` | NULL | — |  |
| `country_id` | `uuid` | NULL | — |  |
| `state_id` | `uuid` | NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `valid_from` | `timestamptz` | NOT NULL | — |  |
| `valid_until` | `timestamptz` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `business_tax_registration`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `business_id` | `uuid` | NOT NULL | — | COMPOSITE PK |
| `tax_registration_id` | `uuid` | NOT NULL | — | COMPOSITE PK |
| `organization_id` | `uuid` | NOT NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `valid_from` | `timestamptz` | NOT NULL | — |  |
| `valid_until` | `timestamptz` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `organization_address`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `address_id` | `uuid` | NULL | — | PK |
| `organization_id` | `uuid` | NOT NULL | — |  |
| `address_type` | `varchar(30)` | NOT NULL | — |  |
| `address_line1` | `varchar(250)` | NOT NULL | — |  |
| `address_line2` | `varchar(250)` | NULL | — |  |
| `landmark` | `varchar(150)` | NULL | — |  |
| `city` | `varchar(100)` | NOT NULL | — |  |
| `country_id` | `uuid` | NOT NULL | — |  |
| `state_id` | `uuid` | NOT NULL | — |  |
| `district_id` | `uuid` | NOT NULL | — |  |
| `postal_code` | `varchar(20)` | NOT NULL | — |  |
| `latitude` | `decimal(10,7)` | NULL | — |  |
| `longitude` | `decimal(10,7)` | NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `organization_contact`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `contact_id` | `uuid` | NULL | — | PK |
| `organization_id` | `uuid` | NOT NULL | — |  |
| `contact_type` | `varchar(30)` | NOT NULL | — |  |
| `first_name` | `varchar(100)` | NULL | — |  |
| `last_name` | `varchar(100)` | NULL | — |  |
| `designation` | `varchar(100)` | NULL | — |  |
| `email` | `varchar(254)` | NULL | — |  |
| `mobile_country_code` | `varchar(5)` | NULL | — |  |
| `mobile_number` | `varchar(15)` | NULL | — |  |
| `alternate_mobile` | `varchar(15)` | NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `company`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `company_id` | `uuid` | NULL | — | PK |
| `organization_id` | `uuid` | NOT NULL | — |  |
| `company_code` | `varchar(50)` | NOT NULL | — |  |
| `company_name` | `varchar(250)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `company_legal_detail`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `legal_detail_id` | `uuid` | NULL | — | PK |
| `company_id` | `uuid` | NOT NULL | — | UNIQUE |
| `legal_name` | `varchar(250)` | NULL | — |  |
| `gstin` | `varchar(15)` | NULL | — |  |
| `pan` | `varchar(10)` | NULL | — |  |
| `cin` | `varchar(21)` | NULL | — |  |
| `tan` | `varchar(10)` | NULL | — |  |
| `registration_number` | `varchar(50)` | NULL | — |  |
| `registration_type` | `varchar(50)` | NULL | — |  |
| `gst_registered` | `boolean` | NOT NULL | FALSE |  |
| `gst_registration_date` | `date` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `company_address`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `address_id` | `uuid` | NULL | — | PK |
| `company_id` | `uuid` | NOT NULL | — |  |
| `address_type` | `varchar(30)` | NOT NULL | — |  |
| `address_line1` | `varchar(250)` | NOT NULL | — |  |
| `address_line2` | `varchar(250)` | NULL | — |  |
| `landmark` | `varchar(150)` | NULL | — |  |
| `city` | `varchar(100)` | NOT NULL | — |  |
| `country_id` | `uuid` | NOT NULL | — |  |
| `state_id` | `uuid` | NOT NULL | — |  |
| `district_id` | `uuid` | NOT NULL | — |  |
| `postal_code` | `varchar(20)` | NOT NULL | — |  |
| `latitude` | `decimal(10,7)` | NULL | — |  |
| `longitude` | `decimal(10,7)` | NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `company_contact`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `contact_id` | `uuid` | NULL | — | PK |
| `company_id` | `uuid` | NOT NULL | — |  |
| `contact_type` | `varchar(30)` | NOT NULL | — |  |
| `first_name` | `varchar(100)` | NULL | — |  |
| `last_name` | `varchar(100)` | NULL | — |  |
| `designation` | `varchar(100)` | NULL | — |  |
| `email` | `varchar(254)` | NULL | — |  |
| `mobile_country_code` | `varchar(5)` | NULL | — |  |
| `mobile_number` | `varchar(15)` | NULL | — |  |
| `alternate_mobile` | `varchar(15)` | NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `business`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `business_id` | `uuid` | NULL | — | COMPOSITE PK |
| `company_id` | `uuid` | NOT NULL | — |  |
| `organization_id` | `uuid` | NOT NULL | — |  |
| `business_code` | `varchar(50)` | NOT NULL | — |  |
| `business_name` | `varchar(250)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `brand`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `brand_id` | `uuid` | NULL | — | PK |
| `business_id` | `uuid` | NOT NULL | — |  |
| `brand_code` | `varchar(50)` | NOT NULL | — |  |
| `brand_name` | `varchar(200)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `store`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `store_id` | `uuid` | NULL | — | PK |
| `business_id` | `uuid` | NOT NULL | — |  |
| `brand_id` | `uuid` | NULL | — |  |
| `store_code` | `varchar(50)` | NOT NULL | — |  |
| `store_name` | `varchar(200)` | NOT NULL | — |  |
| `store_type` | `varchar(30)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `store_location`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `store_location_id` | `uuid` | NULL | — | PK |
| `store_id` | `uuid` | NOT NULL | — |  |
| `location_code` | `varchar(50)` | NOT NULL | — |  |
| `location_name` | `varchar(150)` | NOT NULL | — |  |
| `location_type` | `varchar(30)` | NOT NULL | — |  |
| `address_line1` | `varchar(250)` | NOT NULL | — |  |
| `address_line2` | `varchar(250)` | NULL | — |  |
| `landmark` | `varchar(150)` | NULL | — |  |
| `country_id` | `uuid` | NOT NULL | — |  |
| `state_id` | `uuid` | NOT NULL | — |  |
| `district_id` | `uuid` | NOT NULL | — |  |
| `city` | `varchar(100)` | NOT NULL | — |  |
| `postal_code` | `varchar(20)` | NOT NULL | — |  |
| `latitude` | `decimal(10,7)` | NULL | — |  |
| `longitude` | `decimal(10,7)` | NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `warehouse`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `warehouse_id` | `uuid` | NULL | — | PK |
| `store_id` | `uuid` | NOT NULL | — |  |
| `warehouse_code` | `varchar(50)` | NOT NULL | — |  |
| `warehouse_name` | `varchar(200)` | NOT NULL | — |  |
| `warehouse_type` | `varchar(30)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `capacity` | `numeric(15,2)` | NULL | — |  |
| `capacity_unit` | `varchar(20)` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `warehouse_location`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `warehouse_location_id` | `uuid` | NULL | — | PK |
| `warehouse_id` | `uuid` | NOT NULL | — | UNIQUE |
| `address_line1` | `varchar(250)` | NOT NULL | — |  |
| `address_line2` | `varchar(250)` | NULL | — |  |
| `landmark` | `varchar(150)` | NULL | — |  |
| `country_id` | `uuid` | NOT NULL | — |  |
| `state_id` | `uuid` | NOT NULL | — |  |
| `district_id` | `uuid` | NOT NULL | — |  |
| `city` | `varchar(100)` | NOT NULL | — |  |
| `postal_code` | `varchar(20)` | NOT NULL | — |  |
| `latitude` | `decimal(10,7)` | NULL | — |  |
| `longitude` | `decimal(10,7)` | NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `pos`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `pos_id` | `uuid` | NULL | — | PK |
| `store_id` | `uuid` | NOT NULL | — |  |
| `store_location_id` | `uuid` | NULL | — |  |
| `pos_code` | `varchar(50)` | NOT NULL | — |  |
| `pos_name` | `varchar(150)` | NOT NULL | — |  |
| `device_identifier` | `varchar(150)` | NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `website`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `website_id` | `uuid` | NULL | — | PK |
| `store_id` | `uuid` | NOT NULL | — |  |
| `website_code` | `varchar(50)` | NOT NULL | — |  |
| `website_name` | `varchar(200)` | NOT NULL | — |  |
| `website_type` | `varchar(30)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `website_domain`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `website_domain_id` | `uuid` | NULL | — | PK |
| `website_id` | `uuid` | NOT NULL | — |  |
| `domain_name` | `varchar(255)` | NOT NULL | — | UNIQUE |
| `domain_type` | `varchar(30)` | NOT NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `verification_status` | `varchar(30)` | NOT NULL | — |  |
| `verification_token` | `varchar(255)` | NULL | — |  |
| `verified_at` | `timestamptz` | NULL | — |  |
| `ssl_status` | `varchar(30)` | NULL | — |  |
| `ssl_expiry_at` | `timestamptz` | NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `channel`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `channel_id` | `uuid` | NULL | — | PK |
| `business_id` | `uuid` | NOT NULL | — |  |
| `channel_code` | `varchar(50)` | NOT NULL | — |  |
| `channel_name` | `varchar(150)` | NOT NULL | — |  |
| `channel_type` | `varchar(30)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `store_channel`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `store_id` | `uuid` | NOT NULL | — | COMPOSITE PK |
| `channel_id` | `uuid` | NOT NULL | — | COMPOSITE PK |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `store_branding`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `store_branding_id` | `uuid` | NULL | — | PK |
| `store_id` | `uuid` | NOT NULL | — | UNIQUE |
| `brand_name` | `varchar(200)` | NULL | — |  |
| `tagline` | `varchar(500)` | NULL | — |  |
| `logo_url` | `varchar(1000)` | NULL | — |  |
| `logo_mobile_url` | `varchar(1000)` | NULL | — |  |
| `favicon_url` | `varchar(1000)` | NULL | — |  |
| `primary_color` | `varchar(20)` | NULL | — |  |
| `secondary_color` | `varchar(20)` | NULL | — |  |
| `accent_color` | `varchar(20)` | NULL | — |  |
| `text_color` | `varchar(20)` | NULL | — |  |
| `background_color` | `varchar(20)` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `store_theme`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `store_theme_id` | `uuid` | NULL | — | PK |
| `store_id` | `uuid` | NOT NULL | — | UNIQUE |
| `theme_code` | `varchar(50)` | NOT NULL | — |  |
| `theme_name` | `varchar(100)` | NULL | — |  |
| `layout_code` | `varchar(50)` | NULL | — |  |
| `header_style` | `varchar(50)` | NULL | — |  |
| `footer_style` | `varchar(50)` | NULL | — |  |
| `product_card_style` | `varchar(50)` | NULL | — |  |
| `configuration` | `jsonb` | NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `country`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `country_id` | `uuid` | NULL | — | PK |
| `country_code` | `char(2)` | NOT NULL | — | UNIQUE |
| `country_name` | `varchar(100)` | NOT NULL | — | UNIQUE |
| `phone_code` | `varchar(10)` | NULL | — |  |
| `currency_code` | `varchar(3)` | NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `sort_order` | `int` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `state`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `state_id` | `uuid` | NULL | — | PK |
| `country_id` | `uuid` | NOT NULL | — |  |
| `state_code` | `varchar(20)` | NOT NULL | — |  |
| `state_name` | `varchar(100)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `sort_order` | `int` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `district`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `district_id` | `uuid` | NULL | — | PK |
| `state_id` | `uuid` | NOT NULL | — |  |
| `district_code` | `varchar(20)` | NOT NULL | — |  |
| `district_name` | `varchar(100)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `sort_order` | `int` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `user_account`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `user_id` | `uuid` | NULL | — | PK |
| `tenant_id` | `uuid` | NOT NULL | — |  |
| `username` | `varchar(100)` | NOT NULL | — |  |
| `email` | `varchar(254)` | NOT NULL | — |  |
| `mobile_country_code` | `varchar(5)` | NULL | — |  |
| `mobile_number` | `varchar(15)` | NULL | — |  |
| `first_name` | `varchar(100)` | NOT NULL | — |  |
| `last_name` | `varchar(100)` | NULL | — |  |
| `display_name` | `varchar(200)` | NULL | — |  |
| `password_hash` | `varchar(255)` | NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `email_verified` | `boolean` | NOT NULL | FALSE |  |
| `mobile_verified` | `boolean` | NOT NULL | FALSE |  |
| `last_login_at` | `timestamptz` | NULL | — |  |
| `failed_login_count` | `int` | NOT NULL | 0 |  |
| `locked_until` | `timestamptz` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `role`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `role_id` | `uuid` | NULL | — | PK |
| `tenant_id` | `uuid` | NULL | — |  |
| `role_code` | `varchar(50)` | NOT NULL | — |  |
| `role_name` | `varchar(100)` | NOT NULL | — |  |
| `is_system_role` | `boolean` | NOT NULL | FALSE |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `permission`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `permission_id` | `uuid` | NULL | — | PK |
| `permission_code` | `varchar(100)` | NOT NULL | — | UNIQUE |
| `permission_name` | `varchar(150)` | NOT NULL | — |  |
| `module` | `varchar(50)` | NOT NULL | — |  |
| `action` | `varchar(50)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `role_permission`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `role_id` | `uuid` | NOT NULL | — | COMPOSITE PK |
| `permission_id` | `uuid` | NOT NULL | — | COMPOSITE PK |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `user_access_scope`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `access_scope_id` | `uuid` | NULL | — | PK |
| `user_id` | `uuid` | NOT NULL | — |  |
| `role_id` | `uuid` | NOT NULL | — |  |
| `scope_type` | `varchar(30)` | NOT NULL | — |  |
| `scope_id` | `uuid` | NOT NULL | — | COMPOSITE PK |
| `access_status` | `varchar(30)` | NOT NULL | — |  |
| `is_primary` | `boolean` | NOT NULL | FALSE |  |
| `valid_from` | `timestamptz` | NOT NULL | — |  |
| `valid_until` | `timestamptz` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `hierarchy_change_history`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `history_id` | `uuid` | NULL | — | PK |
| `tenant_id` | `uuid` | NOT NULL | — |  |
| `entity_type` | `varchar(30)` | NOT NULL | — |  |
| `entity_id` | `uuid` | NOT NULL | — |  |
| `old_parent_type` | `varchar(30)` | NULL | — |  |
| `old_parent_id` | `uuid` | NULL | — |  |
| `new_parent_type` | `varchar(30)` | NULL | — |  |
| `new_parent_id` | `uuid` | NULL | — |  |
| `change_type` | `varchar(30)` | NOT NULL | — |  |
| `effective_from` | `timestamptz` | NOT NULL | — |  |
| `effective_until` | `timestamptz` | NULL | — |  |
| `reason` | `varchar(500)` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `changed_by` | `uuid` | NULL | — |  |
| `changed_at` | `timestamptz` | NOT NULL | — |  |

## `user_access_history`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `history_id` | `uuid` | NULL | — | PK |
| `tenant_id` | `uuid` | NOT NULL | — |  |
| `user_id` | `uuid` | NOT NULL | — |  |
| `access_scope_id` | `uuid` | NULL | — |  |
| `action` | `varchar(30)` | NOT NULL | — |  |
| `old_role_id` | `uuid` | NULL | — |  |
| `new_role_id` | `uuid` | NULL | — |  |
| `old_scope_type` | `varchar(30)` | NULL | — |  |
| `old_scope_id` | `uuid` | NULL | — |  |
| `new_scope_type` | `varchar(30)` | NULL | — |  |
| `new_scope_id` | `uuid` | NULL | — |  |
| `effective_from` | `timestamptz` | NOT NULL | — |  |
| `effective_until` | `timestamptz` | NULL | — |  |
| `reason` | `varchar(500)` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `changed_by` | `uuid` | NULL | — |  |
| `changed_at` | `timestamptz` | NOT NULL | — |  |

## `organization_change_history`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `history_id` | `uuid` | NULL | — | PK |
| `tenant_id` | `uuid` | NOT NULL | — |  |
| `entity_type` | `varchar(30)` | NOT NULL | — |  |
| `entity_id` | `uuid` | NOT NULL | — |  |
| `field_name` | `varchar(100)` | NOT NULL | — |  |
| `old_value` | `text` | NULL | — |  |
| `new_value` | `text` | NULL | — |  |
| `action` | `varchar(30)` | NOT NULL | — |  |
| `reason` | `varchar(500)` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `changed_by` | `uuid` | NULL | — |  |
| `changed_at` | `timestamptz` | NOT NULL | — |  |

## `user_account_history`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `history_id` | `uuid` | NULL | — | PK |
| `tenant_id` | `uuid` | NOT NULL | — |  |
| `user_id` | `uuid` | NOT NULL | — |  |
| `action` | `varchar(50)` | NOT NULL | — |  |
| `old_status` | `varchar(30)` | NULL | — |  |
| `new_status` | `varchar(30)` | NULL | — |  |
| `reason` | `varchar(500)` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `metadata` | `jsonb` | NULL | — |  |
| `changed_by` | `uuid` | NULL | — |  |
| `changed_at` | `timestamptz` | NOT NULL | — |  |

## `audit_log`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `audit_id` | `uuid` | NULL | — | PK |
| `tenant_id` | `uuid` | NOT NULL | — |  |
| `user_id` | `uuid` | NULL | — |  |
| `entity_type` | `varchar(50)` | NOT NULL | — |  |
| `entity_id` | `uuid` | NOT NULL | — |  |
| `action` | `varchar(50)` | NOT NULL | — |  |
| `old_data` | `jsonb` | NULL | — |  |
| `new_data` | `jsonb` | NULL | — |  |
| `ip_address` | `varchar(45)` | NULL | — |  |
| `user_agent` | `varchar(1000)` | NULL | — |  |
| `request_id` | `varchar(100)` | NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |

## `reference_type`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `reference_type_id` | `uuid` | NULL | — | PK |
| `type_code` | `varchar(50)` | NOT NULL | — | UNIQUE |
| `type_name` | `varchar(100)` | NOT NULL | — |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## `reference_value`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `reference_value_id` | `uuid` | NULL | — | PK |
| `reference_type_id` | `uuid` | NOT NULL | — |  |
| `value_code` | `varchar(50)` | NOT NULL | — |  |
| `value_name` | `varchar(100)` | NOT NULL | — |  |
| `description` | `varchar(1000)` | NULL | — |  |
| `sort_order` | `int` | NULL | — |  |
| `is_default` | `boolean` | NOT NULL | FALSE |  |
| `is_system_value` | `boolean` | NOT NULL | TRUE |  |
| `status` | `varchar(30)` | NOT NULL | — |  |
| `created_at` | `timestamptz` | NOT NULL | — |  |
| `created_by` | `uuid` | NULL | — |  |
| `updated_at` | `timestamptz` | NULL | — |  |
| `updated_by` | `uuid` | NULL | — |  |

## Finalized constraints and relationships

- UUID primary keys are supplied by Java; no database UUID defaults.
- Timestamp fields use `TIMESTAMPTZ`; the application manages values and no `now()` defaults are used.
- `organization_legal_detail` no longer stores `gstin`.
- `tax_registration` stores GST/tax registration details at Organization level.
- `business_tax_registration` supports sharing one Tax Registration across multiple Businesses.
- `business.organization_id` and composite foreign keys enforce that Business and Tax Registration belong to the same Organization.
- `store(business_id, brand_id)` references `brand(business_id, brand_id)`.
- `store_channel` and `role_permission` use composite primary keys.
- `user_access_scope.scope_type` is limited to ORGANIZATION, COMPANY, BUSINESS, BRAND, STORE, CHANNEL.
- Coordinate bounds and validity-range checks are applied where defined.
- Partial unique indexes enforce one primary website domain per website, one primary store location, one primary warehouse per store, one primary tax registration per business, and role-code uniqueness globally or within tenant.

## `business_application`

| Column | Type | Nullability | Default | Key / source index flags |
|---|---|---|---|---|
| `business_application_id` | `uuid` | NOT NULL | — | PK |
| `business_id` | `uuid` | NOT NULL | — | FK → `business(business_id)`; UNIQUE with `application_id` |
| `application_id` | `uuid` | NOT NULL | — | FK → `public.application(application_id)`; UNIQUE with `business_id`; INDEX |
| `status` | `varchar(30)` | NOT NULL | — | |
| `enabled_at` | `timestamptz` | NOT NULL | — | |
| `description` | `varchar(1000)` | NULL | — | |
| `created_at` | `timestamptz` | NOT NULL | — | |
| `created_by` | `uuid` | NULL | — | |
| `updated_at` | `timestamptz` | NULL | — | |
| `updated_by` | `uuid` | NULL | — | |

`business_application.application_id` is the only intentional tenant-to-public application-catalog relationship. No tenant schema references another tenant schema.
