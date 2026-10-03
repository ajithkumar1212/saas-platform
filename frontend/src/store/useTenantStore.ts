import { create } from "zustand";
import { Tenant } from "@/types/api";

interface TenantState {
  currentTenant: Tenant | null;
  tenantCode: string;
  setTenant: (tenant: Tenant) => void;
  setTenantCode: (code: string) => void;
}

export const useTenantStore = create<TenantState>((set) => ({
  currentTenant: null,
  tenantCode: typeof window !== "undefined" ? localStorage.getItem("saas_tenant_code") || "acme" : "acme",
  setTenant: (tenant: Tenant) => {
    if (typeof window !== "undefined") {
      localStorage.setItem("saas_tenant_code", tenant.tenantCode);
    }
    set({ currentTenant: tenant, tenantCode: tenant.tenantCode });
  },
  setTenantCode: (code: string) => {
    if (typeof window !== "undefined") {
      localStorage.setItem("saas_tenant_code", code);
    }
    set({ tenantCode: code });
  },
}));
