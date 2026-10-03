"use client";

import React from "react";
import { useTenantStore } from "@/store/useTenantStore";
import { useQuery } from "@tanstack/react-query";
import { platformApi } from "@/lib/services";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Building2,
  Building,
  Briefcase,
  Layers,
  ArrowRight,
  Database,
  ShieldCheck,
  CheckCircle2,
  GitBranch,
} from "lucide-react";
import { NavView } from "@/components/layout/Sidebar";

interface DashboardViewProps {
  onNavigate: (view: NavView) => void;
}

export function DashboardView({ onNavigate }: DashboardViewProps) {
  const { tenantCode } = useTenantStore();

  const { data: orgsData, isLoading: orgsLoading } = useQuery({
    queryKey: ["organizations", tenantCode],
    queryFn: () => platformApi.getOrganizations({ page: 0, size: 100 }),
  });

  const { data: companiesData, isLoading: compsLoading } = useQuery({
    queryKey: ["companies", tenantCode],
    queryFn: () => platformApi.getCompanies({ page: 0, size: 100 }),
  });

  const { data: businessesData, isLoading: bizLoading } = useQuery({
    queryKey: ["businesses", tenantCode],
    queryFn: () => platformApi.getBusinesses({ page: 0, size: 100 }),
  });

  const { data: catalogData } = useQuery({
    queryKey: ["catalog"],
    queryFn: () => platformApi.getApplicationCatalog(),
  });

  const orgCount = orgsData?.data?.totalElements ?? 0;
  const compCount = companiesData?.data?.totalElements ?? 0;
  const bizCount = businessesData?.data?.totalElements ?? 0;
  const appCount = catalogData?.data?.length ?? 3;

  return (
    <div className="space-y-8">
      {/* Welcome Banner */}
      <div className="rounded-2xl bg-gradient-to-r from-indigo-900 via-indigo-800 to-indigo-700 p-8 text-white shadow-md relative overflow-hidden">
        <div className="relative z-10 max-w-2xl">
          <div className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1 text-xs font-medium backdrop-blur-sm mb-4 border border-white/20">
            <ShieldCheck className="h-4 w-4 text-emerald-400" />
            <span>Multi-Tenant Schema Isolation Active</span>
          </div>
          <h1 className="text-3xl font-extrabold tracking-tight">
            Tenant: <span className="text-indigo-200 capitalize">{tenantCode}</span>
          </h1>
          <p className="mt-2 text-sm text-indigo-100 leading-relaxed">
            Welcome to the SaaS Control Plane. Manage hierarchical organizations, operating companies, and operational business workspaces with schema-level database isolation.
          </p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Button
              variant="secondary"
              size="sm"
              onClick={() => onNavigate("organizations")}
              className="bg-white text-indigo-900 hover:bg-indigo-50 font-semibold"
            >
              Manage Organizations
              <ArrowRight className="h-4 w-4 ml-1" />
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate("businesses")}
              className="border-white/30 text-white hover:bg-white/10"
            >
              Manage Businesses
            </Button>
          </div>
        </div>

        {/* Decorative elements */}
        <div className="absolute right-6 top-1/2 -translate-y-1/2 hidden md:block opacity-15">
          <Layers className="h-64 w-64 text-white" />
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <Card className="hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Organizations</CardTitle>
            <Building2 className="h-4 w-4 text-indigo-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">
              {orgsLoading ? "..." : orgCount}
            </div>
            <p className="text-xs text-slate-500 mt-1">Tenant Top-Level Entities</p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Companies</CardTitle>
            <Building className="h-4 w-4 text-indigo-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">
              {compsLoading ? "..." : compCount}
            </div>
            <p className="text-xs text-slate-500 mt-1">Legal Operating Entities</p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Businesses</CardTitle>
            <Briefcase className="h-4 w-4 text-indigo-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">
              {bizLoading ? "..." : bizCount}
            </div>
            <p className="text-xs text-slate-500 mt-1">Operational Workspaces</p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-slate-500">Available Apps</CardTitle>
            <Layers className="h-4 w-4 text-indigo-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">
              {appCount}
            </div>
            <p className="text-xs text-slate-500 mt-1">E-Com, Distrib, Warehouse</p>
          </CardContent>
        </Card>
      </div>

      {/* Architecture & Hierarchy Visualizer */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <GitBranch className="h-5 w-5 text-indigo-600" />
                Phase 1 Domain Hierarchy
              </CardTitle>
              <CardDescription className="mt-1">
                Canonical multi-tenant model without entity collapsing: Tenant &rarr; Organization &rarr; Company &rarr; Business &rarr; Operational Units.
              </CardDescription>
            </div>
            <Badge variant="outline" className="border-indigo-200 bg-indigo-50/50 text-indigo-700">
              Schema: tenant_{tenantCode}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <div className="p-6 bg-slate-50/80 rounded-xl border border-slate-200">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              {/* Tenant Box */}
              <div className="bg-white p-4 rounded-lg border border-slate-300 shadow-2xs">
                <div className="flex items-center gap-2 text-indigo-700 font-bold text-sm mb-1">
                  <Database className="h-4 w-4" />
                  1. Tenant Schema
                </div>
                <div className="font-mono text-xs text-slate-600">tenant_{tenantCode}</div>
                <p className="text-[11px] text-slate-400 mt-2">Physical PostgreSQL schema boundary</p>
              </div>

              {/* Organization Box */}
              <div className="bg-white p-4 rounded-lg border border-slate-300 shadow-2xs">
                <div className="flex items-center gap-2 text-indigo-700 font-bold text-sm mb-1">
                  <Building2 className="h-4 w-4" />
                  2. Organization
                </div>
                <div className="text-xs text-slate-700">{orgCount} configured</div>
                <p className="text-[11px] text-slate-400 mt-2">Corporate umbrella & domain group</p>
              </div>

              {/* Company Box */}
              <div className="bg-white p-4 rounded-lg border border-slate-300 shadow-2xs">
                <div className="flex items-center gap-2 text-indigo-700 font-bold text-sm mb-1">
                  <Building className="h-4 w-4" />
                  3. Company
                </div>
                <div className="text-xs text-slate-700">{compCount} configured</div>
                <p className="text-[11px] text-slate-400 mt-2">Legal entity, Tax & Currency context</p>
              </div>

              {/* Business Box */}
              <div className="bg-white p-4 rounded-lg border border-emerald-300 bg-emerald-50/20 shadow-2xs">
                <div className="flex items-center gap-2 text-emerald-800 font-bold text-sm mb-1">
                  <Briefcase className="h-4 w-4" />
                  4. Business Workspace
                </div>
                <div className="text-xs text-slate-700">{bizCount} configured</div>
                <p className="text-[11px] text-slate-500 mt-2">Stores, Channels, Brands, Warehouses</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
