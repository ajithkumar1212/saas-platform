import axios from "axios";
import { useTenantStore } from "@/store/useTenantStore";

export const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081",
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  const tenantCode = useTenantStore.getState().tenantCode;
  if (tenantCode) {
    config.headers["X-Tenant-Code"] = tenantCode;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Standard error envelope returned by backend
    if (error.response?.data) {
      return Promise.reject(error.response.data);
    }
    return Promise.reject({
      success: false,
      message: error.message || "Network error. Please check your backend connection.",
      errorCode: "NETWORK_ERROR",
    });
  }
);
