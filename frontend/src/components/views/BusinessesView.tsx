"use client";

import React, { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useTenantStore } from "@/store/useTenantStore";
import { useBusinessStore } from "@/store/useBusinessStore";
import { platformApi } from "@/lib/services";
import { Business, CreateBusinessRequest, Company } from "@/types/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Briefcase,
  Plus,
  Search,
  ChevronLeft,
  ChevronRight,
  AlertCircle,
  ArrowRight,
  Calendar,
} from "lucide-react";
import { NavView } from "@/components/layout/Sidebar";

const businessSchema = z.object({
  companyId: z.string().min(1, "Company is required"),
  businessCode: z.string().min(2, "Business code must be at least 2 characters").max(50),
  businessName: z.string().min(2, "Business name must be at least 2 characters").max(250),
  description: z.string().max(1000).optional(),
});

type BusinessFormValues = z.infer<typeof businessSchema>;

interface BusinessesViewProps {
  onNavigate: (view: NavView) => void;
}

export function BusinessesView({ onNavigate }: BusinessesViewProps) {
  const { tenantCode } = useTenantStore();
  const { setCurrentBusiness } = useBusinessStore();
  const queryClient = useQueryClient();

  const [page, setPage] = useState(0);
  const [size] = useState(9);
  const [search, setSearch] = useState("");
  const [selectedCompFilter, setSelectedCompFilter] = useState<string>("");
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // Fetch parent companies for dropdown
  const { data: compsResponse } = useQuery({
    queryKey: ["companies", tenantCode, "all"],
    queryFn: () => platformApi.getCompanies({ page: 0, size: 100 }),
  });
  const companies: Company[] = compsResponse?.data?.content || [];

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<BusinessFormValues>({
    resolver: zodResolver(businessSchema),
    defaultValues: {
      companyId: "",
      businessCode: "",
      businessName: "",
      description: "",
    },
  });

  const { data: response, isLoading, isError, error } = useQuery({
    queryKey: ["businesses", tenantCode, selectedCompFilter, search, page, size],
    queryFn: () =>
      platformApi.getBusinesses({
        companyId: selectedCompFilter || undefined,
        search: search || undefined,
        page,
        size,
      }),
  });

  const pagedData = response?.data;
  const businesses = pagedData?.content || [];

  const createMutation = useMutation({
    mutationFn: (data: CreateBusinessRequest) => platformApi.createBusiness(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["businesses", tenantCode] });
      setIsCreateOpen(false);
      reset();
      setFormError(null);
    },
    onError: (err: unknown) => {
      const axiosErr = err as {
        response?: {
          data?: {
            message?: string;
            validationErrors?: Array<{ field: string; message: string }>;
          };
        };
      };
      const apiData = axiosErr.response?.data;
      if (apiData?.validationErrors) {
        apiData.validationErrors.forEach((ve) => {
          setError(ve.field as keyof BusinessFormValues, { message: ve.message });
        });
      } else {
        setFormError(apiData?.message || "Failed to create business.");
      }
    },
  });

  const onSubmit = (values: BusinessFormValues) => {
    setFormError(null);
    createMutation.mutate({
      companyId: values.companyId,
      businessCode: values.businessCode,
      businessName: values.businessName,
      description: values.description || undefined,
    });
  };

  const handleManageBusiness = (business: Business) => {
    setCurrentBusiness(business);
    onNavigate("business-workspace");
  };

  return (
    <div className="space-y-6">
      {/* Header bar */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
            <Briefcase className="h-6 w-6 text-indigo-600" />
            Businesses
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Operational business workspaces containing stores, channels, brands, warehouses, and apps.
          </p>
        </div>

        <Button onClick={() => setIsCreateOpen(true)} className="gap-2">
          <Plus className="h-4 w-4" />
          Create Business
        </Button>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[220px] max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <Input
            placeholder="Search by business name or code..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            className="pl-9 bg-white"
          />
        </div>

        {/* Company Filter */}
        <select
          value={selectedCompFilter}
          onChange={(e) => {
            setSelectedCompFilter(e.target.value);
            setPage(0);
          }}
          className="h-9 rounded-md border border-slate-300 bg-white px-3 py-1 text-sm shadow-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 text-slate-700 cursor-pointer"
        >
          <option value="">All Companies</option>
          {companies.map((c) => (
            <option key={c.companyId} value={c.companyId}>
              {c.companyName} ({c.companyCode})
            </option>
          ))}
        </select>
      </div>

      {/* Main Grid Content */}
      {isLoading ? (
        <div className="flex h-64 items-center justify-center rounded-lg border border-slate-200 bg-white">
          <div className="text-sm text-slate-500 animate-pulse">Loading businesses...</div>
        </div>
      ) : isError ? (
        <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-center">
          <AlertCircle className="mx-auto h-8 w-8 text-red-500 mb-2" />
          <h3 className="text-sm font-semibold text-red-900">Failed to load businesses</h3>
          <p className="text-xs text-red-700 mt-1">
            {(error as { message?: string })?.message || "Check your backend connection."}
          </p>
        </div>
      ) : businesses.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-slate-300 bg-white p-12 text-center">
          <Briefcase className="h-12 w-12 text-slate-300 mb-3" />
          <h3 className="text-base font-semibold text-slate-900">No businesses found</h3>
          <p className="text-sm text-slate-500 max-w-sm mt-1 mb-4">
            {search || selectedCompFilter
              ? "No business matched your filters. Try resetting search or company filter."
              : "Create an operational business workspace to configure stores, sales channels, and applications."}
          </p>
          <Button onClick={() => setIsCreateOpen(true)} size="sm">
            <Plus className="h-4 w-4 mr-1" />
            Create Business
          </Button>
        </div>
      ) : (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {businesses.map((biz: Business) => (
              <Card
                key={biz.businessId || biz.id}
                className="flex flex-col justify-between border-slate-200 hover:border-indigo-300 hover:shadow-md transition-all group bg-white"
              >
                <div className="p-5">
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <div>
                      <span className="font-mono text-xs font-bold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">
                        {biz.businessCode}
                      </span>
                      <h3 className="font-bold text-slate-900 text-lg mt-1 group-hover:text-indigo-600 transition-colors">
                        {biz.businessName || biz.name}
                      </h3>
                    </div>
                    <Badge variant={biz.status === "ACTIVE" ? "success" : "inactive"}>
                      {biz.status}
                    </Badge>
                  </div>

                  {biz.description && (
                    <p className="text-xs text-slate-500 line-clamp-2 mt-1 mb-3">
                      {biz.description}
                    </p>
                  )}

                  <div className="text-xs text-slate-400 pt-3 border-t border-slate-100 flex items-center gap-1">
                    <Calendar className="h-3 w-3 text-slate-400" />
                    <span>Created {biz.createdAt ? new Date(biz.createdAt).toLocaleDateString() : "—"}</span>
                  </div>
                </div>

                <div className="bg-slate-50/80 px-5 py-3 border-t border-slate-100 rounded-b-xl flex items-center justify-between">
                  <span className="text-xs text-slate-400">Workspace Hub</span>
                  <Button
                    size="sm"
                    onClick={() => handleManageBusiness(biz)}
                    className="gap-1.5 text-xs bg-indigo-600 hover:bg-indigo-700 text-white shadow-xs font-semibold"
                  >
                    Manage Business
                    <ArrowRight className="h-3.5 w-3.5" />
                  </Button>
                </div>
              </Card>
            ))}
          </div>

          {/* Canonical Pagination Controls */}
          {pagedData && (
            <div className="flex items-center justify-between px-2 text-xs text-slate-500">
              <div>
                Showing <strong>{pagedData.pageNumber * pagedData.pageSize + 1}</strong> to{" "}
                <strong>
                  {Math.min(
                    (pagedData.pageNumber + 1) * pagedData.pageSize,
                    pagedData.totalElements
                  )}
                </strong>{" "}
                of <strong>{pagedData.totalElements}</strong> businesses
              </div>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={!pagedData.hasPrevious}
                  className="h-8 px-2 text-xs"
                >
                  <ChevronLeft className="h-3.5 w-3.5 mr-1" />
                  Previous
                </Button>
                <span className="font-medium text-slate-700">
                  Page {pagedData.pageNumber + 1} of {Math.max(1, pagedData.totalPages)}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => p + 1)}
                  disabled={!pagedData.hasNext}
                  className="h-8 px-2 text-xs"
                >
                  Next
                  <ChevronRight className="h-3.5 w-3.5 ml-1" />
                </Button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Create Business Dialog */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className="sm:max-w-lg">
          <form onSubmit={handleSubmit(onSubmit)}>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Briefcase className="h-5 w-5 text-indigo-600" />
                Create New Business
              </DialogTitle>
              <DialogDescription>
                Establish an operational business workspace under a company in tenant <code>{tenantCode}</code>.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              {formError && (
                <div className="rounded-lg bg-red-50 p-3 text-xs text-red-700 border border-red-200">
                  {formError}
                </div>
              )}

              <div className="space-y-1">
                <Label htmlFor="companyId" required>Parent Company</Label>
                <select
                  id="companyId"
                  {...register("companyId")}
                  className="flex h-9 w-full rounded-md border border-slate-300 bg-white px-3 py-1 text-sm shadow-xs focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="">Select Company...</option>
                  {companies.map((c) => (
                    <option key={c.companyId} value={c.companyId}>
                      {c.companyName} ({c.companyCode})
                    </option>
                  ))}
                </select>
                {errors.companyId && (
                  <p className="text-xs text-red-600 font-medium">{errors.companyId.message}</p>
                )}
              </div>

              <div className="space-y-1">
                <Label htmlFor="businessCode" required>Business Code</Label>
                <Input
                  id="businessCode"
                  placeholder="e.g. BIZ_RETAIL_01"
                  {...register("businessCode")}
                  error={errors.businessCode?.message}
                />
              </div>

              <div className="space-y-1">
                <Label htmlFor="businessName" required>Business Name</Label>
                <Input
                  id="businessName"
                  placeholder="e.g. Acme Retail East"
                  {...register("businessName")}
                  error={errors.businessName?.message}
                />
              </div>

              <div className="space-y-1">
                <Label htmlFor="description">Description</Label>
                <Input
                  id="description"
                  placeholder="Primary retail operations..."
                  {...register("description")}
                  error={errors.description?.message}
                />
              </div>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={() => setIsCreateOpen(false)}
                disabled={isSubmitting}
              >
                Cancel
              </Button>
              <Button type="submit" size="sm" disabled={isSubmitting}>
                {isSubmitting ? "Creating..." : "Create Business"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
