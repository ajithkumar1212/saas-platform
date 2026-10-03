"use client";

import React, { useState } from "react";
import { useTenantStore } from "@/store/useTenantStore";
import { useBusinessStore } from "@/store/useBusinessStore";
import { useQuery } from "@tanstack/react-query";
import { platformApi } from "@/lib/services";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Building2,
  Database,
  Layers,
  Sparkles,
  ChevronRight,
  ShieldCheck,
  PlusCircle,
  Briefcase,
} from "lucide-react";

export function Header() {
  const { tenantCode, setTenantCode } = useTenantStore();
  const { currentBusiness, setCurrentBusiness } = useBusinessStore();

  const [isSwitchTenantOpen, setIsSwitchTenantOpen] = useState(false);
  const [isProvisionOpen, setIsProvisionOpen] = useState(false);
  const [newTenantCode, setNewTenantCode] = useState("");
  const [provisionCode, setProvisionCode] = useState("");
  const [provisionName, setProvisionName] = useState("");
  const [provisioning, setProvisioning] = useState(false);
  const [provisionError, setProvisionError] = useState("");
  const [provisionSuccess, setProvisionSuccess] = useState("");

  // Query tenants for quick selection
  const { data: tenantsResponse, refetch: refetchTenants } = useQuery({
    queryKey: ["tenants"],
    queryFn: () => platformApi.getTenants(),
    staleTime: 60000,
  });

  const tenants = tenantsResponse?.data || [];

  const handleSwitchTenant = (code: string) => {
    if (code.trim()) {
      setTenantCode(code.trim().toLowerCase());
      setCurrentBusiness(null);
      setIsSwitchTenantOpen(false);
      setNewTenantCode("");
    }
  };

  const handleProvisionTenant = async (e: React.FormEvent) => {
    e.preventDefault();
    setProvisionError("");
    setProvisionSuccess("");
    if (!provisionCode.trim() || !provisionName.trim()) {
      setProvisionError("Tenant code and tenant name are required.");
      return;
    }

    try {
      setProvisioning(true);
      const res = await platformApi.provisionTenant({
        tenantCode: provisionCode.trim().toLowerCase(),
        tenantName: provisionName.trim(),
      });
      if (res.success) {
        setProvisionSuccess(`Tenant '${provisionCode.trim().toLowerCase()}' provisioned successfully!`);
        refetchTenants();
        setTimeout(() => {
          handleSwitchTenant(provisionCode.trim().toLowerCase());
          setIsProvisionOpen(false);
          setProvisionCode("");
          setProvisionName("");
          setProvisionSuccess("");
        }, 1200);
      }
    } catch (err: unknown) {
      const errorMsg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        "Failed to provision tenant. Please check backend logs.";
      setProvisionError(errorMsg);
    } finally {
      setProvisioning(false);
    }
  };

  return (
    <>
      <header className="sticky top-0 z-40 flex h-16 w-full items-center justify-between border-b border-slate-200 bg-white/95 px-6 backdrop-blur-sm">
        {/* Left: Brand & Hierarchy Context */}
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-600 text-white shadow-sm font-bold">
              <Layers className="h-5 w-5" />
            </div>
            <div>
              <span className="text-base font-bold tracking-tight text-slate-900">
                XYZ SaaS Platform
              </span>
              <span className="hidden sm:inline-block ml-2 text-xs font-medium px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-100">
                v1.0
              </span>
            </div>
          </div>

          <div className="h-5 w-px bg-slate-200" />

          {/* Breadcrumb Context */}
          <nav className="hidden md:flex items-center text-xs font-medium text-slate-500">
            <span className="flex items-center gap-1.5 text-slate-700">
              <Building2 className="h-3.5 w-3.5 text-indigo-600" />
              Tenant: <strong className="text-indigo-900 font-semibold">{tenantCode}</strong>
            </span>

            {currentBusiness && (
              <>
                <ChevronRight className="h-3.5 w-3.5 text-slate-400 mx-1" />
                <span className="flex items-center gap-1 text-slate-900 font-semibold">
                  <Briefcase className="h-3.5 w-3.5 text-emerald-600" />
                  Business: {currentBusiness.name} ({currentBusiness.businessCode})
                </span>
              </>
            )}
          </nav>
        </div>

        {/* Right: Tenant Controls & Schema Indicator */}
        <div className="flex items-center gap-3">
          {/* Schema Isolation Indicator */}
          <div className="hidden lg:flex items-center gap-2 rounded-full border border-emerald-200 bg-emerald-50/70 px-3 py-1 text-xs font-medium text-emerald-800">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
            </span>
            <Database className="h-3.5 w-3.5 text-emerald-600" />
            <span>Schema: <code className="font-mono text-emerald-900">tenant_{tenantCode}</code></span>
          </div>

          {/* Switch Tenant Button */}
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsSwitchTenantOpen(true)}
            className="text-xs font-medium border-slate-300"
          >
            <ShieldCheck className="h-3.5 w-3.5 text-indigo-600 mr-1.5" />
            Switch Tenant
          </Button>

          {/* Provision Tenant Button */}
          <Button
            variant="default"
            size="sm"
            onClick={() => setIsProvisionOpen(true)}
            className="text-xs font-medium"
          >
            <PlusCircle className="h-3.5 w-3.5 mr-1.5" />
            Provision Tenant
          </Button>
        </div>
      </header>

      {/* Switch Tenant Dialog */}
      <Dialog open={isSwitchTenantOpen} onOpenChange={setIsSwitchTenantOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Switch Active Tenant Context</DialogTitle>
            <DialogDescription>
              Select an existing provisioned tenant schema or enter a tenant code. All API requests will send the corresponding <code className="text-xs bg-slate-100 px-1 py-0.5 rounded font-mono text-indigo-600">X-Tenant-Code</code> header.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-2">
            {tenants.length > 0 && (
              <div>
                <Label className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2 block">
                  Available Provisioned Tenants
                </Label>
                <div className="grid grid-cols-2 gap-2">
                  {tenants.map((t) => (
                    <button
                      key={t.tenantId || t.tenantCode}
                      type="button"
                      onClick={() => handleSwitchTenant(t.tenantCode)}
                      className={`flex flex-col items-start p-2.5 rounded-lg border text-left transition-all cursor-pointer ${
                        tenantCode === t.tenantCode
                          ? "border-indigo-500 bg-indigo-50/50 ring-1 ring-indigo-500"
                          : "border-slate-200 hover:border-slate-300 hover:bg-slate-50"
                      }`}
                    >
                      <div className="flex items-center justify-between w-full">
                        <span className="font-semibold text-sm text-slate-900">
                          {t.tenantName || t.tenantCode}
                        </span>
                        {tenantCode === t.tenantCode && (
                          <Badge variant="success" className="text-[10px] px-1.5 py-0">
                            Active
                          </Badge>
                        )}
                      </div>
                      <span className="text-xs text-slate-500 font-mono mt-0.5">
                        {t.tenantCode}
                      </span>
                    </button>
                  ))}
                </div>
              </div>
            )}

            <div className="border-t border-slate-100 pt-3">
              <Label htmlFor="custom-tenant-code" className="text-xs font-medium text-slate-700">
                Or Enter Custom Tenant Code:
              </Label>
              <div className="flex gap-2 mt-1.5">
                <Input
                  id="custom-tenant-code"
                  placeholder="e.g. acme, globex, initech"
                  value={newTenantCode}
                  onChange={(e) => setNewTenantCode(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") handleSwitchTenant(newTenantCode);
                  }}
                />
                <Button
                  onClick={() => handleSwitchTenant(newTenantCode)}
                  disabled={!newTenantCode.trim()}
                  size="sm"
                >
                  Switch
                </Button>
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="ghost" size="sm" onClick={() => setIsSwitchTenantOpen(false)}>
              Cancel
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Provision New Tenant Dialog */}
      <Dialog open={isProvisionOpen} onOpenChange={setIsProvisionOpen}>
        <DialogContent className="sm:max-w-md">
          <form onSubmit={handleProvisionTenant}>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Sparkles className="h-5 w-5 text-indigo-600" />
                Provision New Tenant
              </DialogTitle>
              <DialogDescription>
                Provisions a dedicated schema (<code>tenant_&lt;code&gt;</code>) and applies Flyway migrations automatically.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              {provisionError && (
                <div className="rounded-lg bg-red-50 p-3 text-xs text-red-700 border border-red-200">
                  {provisionError}
                </div>
              )}
              {provisionSuccess && (
                <div className="rounded-lg bg-emerald-50 p-3 text-xs text-emerald-700 border border-emerald-200">
                  {provisionSuccess}
                </div>
              )}

              <div className="space-y-1.5">
                <Label htmlFor="tenant-code" required>Tenant Code</Label>
                <Input
                  id="tenant-code"
                  placeholder="e.g. globex"
                  value={provisionCode}
                  onChange={(e) => setProvisionCode(e.target.value)}
                  required
                />
                <p className="text-[11px] text-slate-500">
                  Alphanumeric slug (e.g. <code>globex</code>). Schema will be <code>tenant_globex</code>.
                </p>
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="tenant-name" required>Tenant Display Name</Label>
                <Input
                  id="tenant-name"
                  placeholder="e.g. Globex Corporation"
                  value={provisionName}
                  onChange={(e) => setProvisionName(e.target.value)}
                  required
                />
              </div>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={() => setIsProvisionOpen(false)}
                disabled={provisioning}
              >
                Cancel
              </Button>
              <Button type="submit" size="sm" disabled={provisioning}>
                {provisioning ? "Provisioning Schema..." : "Provision Tenant"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
}
