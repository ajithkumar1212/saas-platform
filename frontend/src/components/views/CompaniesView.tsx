"use client";

import React, { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useTenantStore } from "@/store/useTenantStore";
import { platformApi } from "@/lib/services";
import { Company, CreateCompanyRequest, Organization } from "@/types/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Building,
  Plus,
  Search,
  ChevronLeft,
  ChevronRight,
  AlertCircle,
  Calendar,
} from "lucide-react";
import { NavView } from "@/components/layout/Sidebar";

const companySchema = z.object({
  organizationId: z.string().min(1, "Organization is required"),
  companyCode: z.string().min(2, "Company code must be at least 2 characters").max(50),
  companyName: z.string().min(2, "Company name must be at least 2 characters").max(250),
  description: z.string().max(1000).optional(),
});

type CompanyFormValues = z.infer<typeof companySchema>;

interface CompaniesViewProps {
  onNavigate: (view: NavView) => void;
}

export function CompaniesView({ onNavigate }: CompaniesViewProps) {
  const { tenantCode } = useTenantStore();
  const queryClient = useQueryClient();

  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [search, setSearch] = useState("");
  const [selectedOrgFilter, setSelectedOrgFilter] = useState<string>("");
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // Fetch parent organizations for dropdowns
  const { data: orgsResponse } = useQuery({
    queryKey: ["organizations", tenantCode, "all"],
    queryFn: () => platformApi.getOrganizations({ page: 0, size: 100 }),
  });
  const organizations: Organization[] = orgsResponse?.data?.content || [];

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<CompanyFormValues>({
    resolver: zodResolver(companySchema),
    defaultValues: {
      organizationId: "",
      companyCode: "",
      companyName: "",
      description: "",
    },
  });

  const { data: response, isLoading, isError, error } = useQuery({
    queryKey: ["companies", tenantCode, selectedOrgFilter, search, page, size],
    queryFn: () =>
      platformApi.getCompanies({
        organizationId: selectedOrgFilter || undefined,
        search: search || undefined,
        page,
        size,
      }),
  });

  const pagedData = response?.data;
  const companies = pagedData?.content || [];

  const createMutation = useMutation({
    mutationFn: (data: CreateCompanyRequest) => platformApi.createCompany(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["companies", tenantCode] });
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
          setError(ve.field as keyof CompanyFormValues, { message: ve.message });
        });
      } else {
        setFormError(apiData?.message || "Failed to create company.");
      }
    },
  });

  const onSubmit = (values: CompanyFormValues) => {
    setFormError(null);
    createMutation.mutate({
      organizationId: values.organizationId,
      companyCode: values.companyCode,
      companyName: values.companyName,
      description: values.description || undefined,
    });
  };

  return (
    <div className="space-y-6">
      {/* Header bar */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
            <Building className="h-6 w-6 text-indigo-600" />
            Companies
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Operating companies grouped under organizations in tenant <span className="font-semibold text-slate-700 font-mono">tenant_{tenantCode}</span>.
          </p>
        </div>

        <Button onClick={() => setIsCreateOpen(true)} className="gap-2">
          <Plus className="h-4 w-4" />
          Create Company
        </Button>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[220px] max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <Input
            placeholder="Search by company name or code..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            className="pl-9 bg-white"
          />
        </div>

        {/* Organization Filter */}
        <select
          value={selectedOrgFilter}
          onChange={(e) => {
            setSelectedOrgFilter(e.target.value);
            setPage(0);
          }}
          className="h-9 rounded-md border border-slate-300 bg-white px-3 py-1 text-sm shadow-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 text-slate-700 cursor-pointer"
        >
          <option value="">All Organizations</option>
          {organizations.map((org) => (
            <option key={org.organizationId} value={org.organizationId}>
              {org.organizationName} ({org.organizationCode})
            </option>
          ))}
        </select>
      </div>

      {/* Main Table Content */}
      {isLoading ? (
        <div className="flex h-64 items-center justify-center rounded-lg border border-slate-200 bg-white">
          <div className="text-sm text-slate-500 animate-pulse">Loading companies...</div>
        </div>
      ) : isError ? (
        <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-center">
          <AlertCircle className="mx-auto h-8 w-8 text-red-500 mb-2" />
          <h3 className="text-sm font-semibold text-red-900">Failed to load companies</h3>
          <p className="text-xs text-red-700 mt-1">
            {(error as { message?: string })?.message || "Check your backend connection."}
          </p>
        </div>
      ) : companies.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-slate-300 bg-white p-12 text-center">
          <Building className="h-12 w-12 text-slate-300 mb-3" />
          <h3 className="text-base font-semibold text-slate-900">No companies found</h3>
          <p className="text-sm text-slate-500 max-w-sm mt-1 mb-4">
            {search || selectedOrgFilter
              ? "No company matched your filters. Try resetting search or organization filter."
              : "Create a company under an organization to establish your operational structure."}
          </p>
          <Button onClick={() => setIsCreateOpen(true)} size="sm">
            <Plus className="h-4 w-4 mr-1" />
            Create Company
          </Button>
        </div>
      ) : (
        <div className="space-y-4">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Code</TableHead>
                <TableHead>Company Name</TableHead>
                <TableHead>Description</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created At</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {companies.map((comp: Company) => (
                <TableRow key={comp.companyId || comp.id}>
                  <TableCell className="font-mono text-xs font-semibold text-indigo-700">
                    {comp.companyCode}
                  </TableCell>
                  <TableCell className="font-medium text-slate-900">
                    {comp.companyName || comp.name}
                  </TableCell>
                  <TableCell className="text-xs text-slate-500 max-w-xs truncate">
                    {comp.description || "—"}
                  </TableCell>
                  <TableCell>
                    <Badge variant={comp.status === "ACTIVE" ? "success" : "inactive"}>
                      {comp.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-xs text-slate-500">
                    <span className="flex items-center gap-1">
                      <Calendar className="h-3 w-3 text-slate-400" />
                      {comp.createdAt ? new Date(comp.createdAt).toLocaleDateString() : "—"}
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onNavigate("businesses")}
                      className="text-xs text-indigo-600 hover:text-indigo-800"
                    >
                      View Businesses &rarr;
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

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
                of <strong>{pagedData.totalElements}</strong> companies
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

      {/* Create Company Dialog */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className="sm:max-w-lg">
          <form onSubmit={handleSubmit(onSubmit)}>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Building className="h-5 w-5 text-indigo-600" />
                Create New Company
              </DialogTitle>
              <DialogDescription>
                Register an operating company under an organization in tenant <code>{tenantCode}</code>.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              {formError && (
                <div className="rounded-lg bg-red-50 p-3 text-xs text-red-700 border border-red-200">
                  {formError}
                </div>
              )}

              <div className="space-y-1">
                <Label htmlFor="organizationId" required>Parent Organization</Label>
                <select
                  id="organizationId"
                  {...register("organizationId")}
                  className="flex h-9 w-full rounded-md border border-slate-300 bg-white px-3 py-1 text-sm shadow-xs focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="">Select Organization...</option>
                  {organizations.map((org) => (
                    <option key={org.organizationId} value={org.organizationId}>
                      {org.organizationName} ({org.organizationCode})
                    </option>
                  ))}
                </select>
                {errors.organizationId && (
                  <p className="text-xs text-red-600 font-medium">{errors.organizationId.message}</p>
                )}
              </div>

              <div className="space-y-1">
                <Label htmlFor="companyCode" required>Company Code</Label>
                <Input
                  id="companyCode"
                  placeholder="e.g. COMP_RETAIL_US"
                  {...register("companyCode")}
                  error={errors.companyCode?.message}
                />
              </div>

              <div className="space-y-1">
                <Label htmlFor="companyName" required>Company Name</Label>
                <Input
                  id="companyName"
                  placeholder="e.g. Acme Retail US"
                  {...register("companyName")}
                  error={errors.companyName?.message}
                />
              </div>

              <div className="space-y-1">
                <Label htmlFor="description">Description</Label>
                <Input
                  id="description"
                  placeholder="e.g. Primary retail company..."
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
                {isSubmitting ? "Creating..." : "Create Company"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
