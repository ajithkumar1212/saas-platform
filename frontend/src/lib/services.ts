import { apiClient } from "./api-client";
import {
  ApiResponse,
  PagedResponse,
  Tenant,
  Organization,
  CreateOrganizationRequest,
  Company,
  CreateCompanyRequest,
  Business,
  CreateBusinessRequest,
  Store,
  CreateStoreRequest,
  SalesChannel,
  CreateChannelRequest,
  StoreChannelResponse,
  Brand,
  CreateBrandRequest,
  Warehouse,
  CreateWarehouseRequest,
  PosTerminal,
  CreatePosRequest,
  ApplicationCatalogItem,
  BusinessApplication,
  EnableBusinessApplicationRequest,
  ReferenceValue,
} from "@/types/api";

export const platformApi = {
  // Tenant Provisioning & Listing
  getTenants: async (): Promise<ApiResponse<Tenant[]>> => {
    const res = await apiClient.get<ApiResponse<Tenant[]>>("/api/v1/provisioning/tenants");
    return res.data;
  },

  provisionTenant: async (data: { tenantCode: string; tenantName: string }): Promise<ApiResponse<unknown>> => {
    const res = await apiClient.post<ApiResponse<unknown>>("/api/v1/provisioning/tenants", data);
    return res.data;
  },

  // Organizations
  getOrganizations: async (params?: { search?: string; page?: number; size?: number }): Promise<ApiResponse<PagedResponse<Organization>>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<Organization>>>("/api/v1/organizations", { params });
    return res.data;
  },

  createOrganization: async (data: CreateOrganizationRequest): Promise<ApiResponse<Organization>> => {
    const res = await apiClient.post<ApiResponse<Organization>>("/api/v1/organizations", data);
    return res.data;
  },

  getOrganization: async (id: string): Promise<ApiResponse<Organization>> => {
    const res = await apiClient.get<ApiResponse<Organization>>(`/api/v1/organizations/${id}`);
    return res.data;
  },

  // Companies
  getCompanies: async (params?: { organizationId?: string; search?: string; page?: number; size?: number }): Promise<ApiResponse<PagedResponse<Company>>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<Company>>>("/api/v1/companies", { params });
    return res.data;
  },

  createCompany: async (data: CreateCompanyRequest): Promise<ApiResponse<Company>> => {
    const res = await apiClient.post<ApiResponse<Company>>("/api/v1/companies", data);
    return res.data;
  },

  // Businesses
  getBusinesses: async (params?: { companyId?: string; search?: string; page?: number; size?: number }): Promise<ApiResponse<PagedResponse<Business>>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<Business>>>("/api/v1/businesses", { params });
    return res.data;
  },

  createBusiness: async (data: CreateBusinessRequest): Promise<ApiResponse<Business>> => {
    const res = await apiClient.post<ApiResponse<Business>>("/api/v1/businesses", data);
    return res.data;
  },

  getBusiness: async (id: string): Promise<ApiResponse<Business>> => {
    const res = await apiClient.get<ApiResponse<Business>>(`/api/v1/businesses/${id}`);
    return res.data;
  },

  // Stores
  getStoresByBusiness: async (businessId: string, params?: { search?: string; page?: number; size?: number }): Promise<ApiResponse<PagedResponse<Store>>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<Store>>>(`/api/v1/businesses/${businessId}/stores`, { params });
    return res.data;
  },

  createStore: async (data: CreateStoreRequest): Promise<ApiResponse<Store>> => {
    const res = await apiClient.post<ApiResponse<Store>>("/api/v1/stores", data);
    return res.data;
  },

  // Sales Channels
  getChannelsByBusiness: async (businessId: string, params?: { search?: string; page?: number; size?: number }): Promise<ApiResponse<PagedResponse<SalesChannel>>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<SalesChannel>>>(`/api/v1/businesses/${businessId}/channels`, { params });
    return res.data;
  },

  createChannel: async (data: CreateChannelRequest): Promise<ApiResponse<SalesChannel>> => {
    const res = await apiClient.post<ApiResponse<SalesChannel>>("/api/v1/channels", data);
    return res.data;
  },

  associateStoreChannel: async (storeId: string, channelId: string, isPrimary = false, description?: string): Promise<ApiResponse<StoreChannelResponse>> => {
    const res = await apiClient.post<ApiResponse<StoreChannelResponse>>(
      `/api/v1/stores/${storeId}/channels/${channelId}`,
      null,
      { params: { isPrimary, description } }
    );
    return res.data;
  },

  // Brands
  getBrandsByBusiness: async (businessId: string, params?: { search?: string; page?: number; size?: number }): Promise<ApiResponse<PagedResponse<Brand>>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<Brand>>>(`/api/v1/businesses/${businessId}/brands`, { params });
    return res.data;
  },

  createBrand: async (data: CreateBrandRequest): Promise<ApiResponse<Brand>> => {
    const res = await apiClient.post<ApiResponse<Brand>>("/api/v1/brands", data);
    return res.data;
  },

  // Warehouses
  getWarehousesByStore: async (storeId: string, params?: { search?: string; page?: number; size?: number }): Promise<ApiResponse<PagedResponse<Warehouse>>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<Warehouse>>>(`/api/v1/stores/${storeId}/warehouses`, { params });
    return res.data;
  },

  createWarehouse: async (data: CreateWarehouseRequest): Promise<ApiResponse<Warehouse>> => {
    const res = await apiClient.post<ApiResponse<Warehouse>>("/api/v1/warehouses", data);
    return res.data;
  },

  // POS
  getPosByStore: async (storeId: string, params?: { search?: string; page?: number; size?: number }): Promise<ApiResponse<PagedResponse<PosTerminal>>> => {
    const res = await apiClient.get<ApiResponse<PagedResponse<PosTerminal>>>(`/api/v1/stores/${storeId}/pos`, { params });
    return res.data;
  },

  createPos: async (data: CreatePosRequest): Promise<ApiResponse<PosTerminal>> => {
    const res = await apiClient.post<ApiResponse<PosTerminal>>("/api/v1/pos", data);
    return res.data;
  },

  // Applications
  getApplicationCatalog: async (): Promise<ApiResponse<ApplicationCatalogItem[]>> => {
    const res = await apiClient.get<ApiResponse<ApplicationCatalogItem[]>>("/api/v1/applications");
    return res.data;
  },

  getBusinessApplications: async (businessId: string): Promise<ApiResponse<BusinessApplication[]>> => {
    const res = await apiClient.get<ApiResponse<BusinessApplication[]>>(`/api/v1/businesses/${businessId}/applications`);
    return res.data;
  },

  enableBusinessApplication: async (businessId: string, data: EnableBusinessApplicationRequest): Promise<ApiResponse<BusinessApplication>> => {
    const res = await apiClient.post<ApiResponse<BusinessApplication>>(`/api/v1/businesses/${businessId}/applications`, data);
    return res.data;
  },

  // Reference Data
  getReferenceValues: async (typeCode: string): Promise<ApiResponse<ReferenceValue[]>> => {
    const res = await apiClient.get<ApiResponse<ReferenceValue[]>>(`/api/v1/reference-data/${typeCode}`);
    return res.data;
  },
};
