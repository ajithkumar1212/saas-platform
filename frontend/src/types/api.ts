export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
  validationErrors?: ValidationError[];
  timestamp: string;
  path?: string;
}

export interface PagedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
  first: boolean;
  last: boolean;
}

export interface ValidationError {
  field: string;
  message: string;
  rejectedValue?: unknown;
}

export interface Tenant {
  tenantId: string;
  tenantCode: string;
  tenantName: string;
  schemaName?: string;
  status: string;
  provisioningStatus?: string;
  createdAt?: string;
}

export interface Organization {
  organizationId: string;
  id?: string;
  tenantId?: string;
  organizationCode: string;
  orgCode?: string;
  organizationName: string;
  name?: string;
  status: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateOrganizationRequest {
  organizationCode: string;
  organizationName: string;
  description?: string;
}

export interface Company {
  companyId: string;
  id?: string;
  organizationId: string;
  companyCode: string;
  companyName: string;
  name?: string;
  status: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateCompanyRequest {
  organizationId: string;
  companyCode: string;
  companyName: string;
  description?: string;
}

export interface Business {
  businessId: string;
  id?: string;
  companyId: string;
  organizationId?: string;
  businessCode: string;
  businessName: string;
  name?: string;
  status: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
  businessType?: string;
  country?: string;
  currency?: string;
  timezone?: string;
}

export interface CreateBusinessRequest {
  companyId: string;
  businessCode: string;
  businessName: string;
  description?: string;
}

export interface Store {
  storeId: string;
  id?: string;
  businessId: string;
  brandId?: string;
  storeCode: string;
  storeName: string;
  name?: string;
  storeType: string;
  status: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
  currency?: string;
}

export interface CreateStoreRequest {
  businessId: string;
  brandId?: string;
  storeCode: string;
  storeName: string;
  storeType: string;
  description?: string;
}

export interface SalesChannel {
  channelId: string;
  id?: string;
  businessId: string;
  channelCode: string;
  channelName: string;
  name?: string;
  channelType: string;
  status: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateChannelRequest {
  businessId: string;
  channelCode: string;
  channelName: string;
  channelType: string;
  description?: string;
}

export interface StoreChannelResponse {
  storeId: string;
  channelId: string;
  status: string;
  isPrimary: boolean;
  description?: string;
  createdAt?: string;
}

export interface Brand {
  brandId: string;
  id?: string;
  businessId: string;
  brandCode: string;
  brandName: string;
  name?: string;
  status: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateBrandRequest {
  businessId: string;
  brandCode: string;
  brandName: string;
  description?: string;
}

export interface Warehouse {
  warehouseId: string;
  id?: string;
  storeId: string;
  warehouseCode: string;
  warehouseName: string;
  name?: string;
  warehouseType: string;
  status: string;
  isPrimary?: boolean;
  capacity?: number;
  capacityUnit?: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateWarehouseRequest {
  storeId: string;
  warehouseCode: string;
  warehouseName: string;
  warehouseType: string;
  isPrimary?: boolean;
  capacity?: number;
  capacityUnit?: string;
  description?: string;
}

export interface PosTerminal {
  posId: string;
  id?: string;
  storeId: string;
  storeLocationId?: string;
  posCode: string;
  posName: string;
  name?: string;
  deviceIdentifier?: string;
  deviceModel?: string;
  serialNumber?: string;
  isPrimary?: boolean;
  status: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreatePosRequest {
  storeId: string;
  storeLocationId?: string;
  posCode: string;
  posName: string;
  deviceIdentifier?: string;
  isPrimary?: boolean;
  description?: string;
}

export interface BusinessApplication {
  businessApplicationId: string;
  id?: string;
  businessId: string;
  applicationId?: string;
  applicationCode: string;
  applicationName?: string;
  status: string;
  enabledAt?: string;
  description?: string;
  createdAt?: string;
}

export interface EnableBusinessApplicationRequest {
  applicationCode: string;
  configurationJson?: string;
}

export interface ApplicationCatalogItem {
  applicationId: string;
  id?: string;
  applicationCode: string;
  appCode?: string;
  applicationName: string;
  appName?: string;
  status: string;
  description?: string;
  version?: string;
  createdAt?: string;
}

export interface ReferenceValue {
  referenceValueId: string;
  id?: string;
  typeCode: string;
  valueCode: string;
  code?: string;
  valueName: string;
  displayValue?: string;
  description?: string;
  sortOrder?: number;
  status?: string;
  isActive?: boolean;
  isDefault?: boolean;
}
