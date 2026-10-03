"use client";

import React, { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useTenantStore } from "@/store/useTenantStore";
import { platformApi } from "@/lib/services";
import { Organization, CreateOrganizationRequest } from "@/types/api";
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
  Building2,
  Plus,
  Search,
  ChevronLeft,
  ChevronRight,
  Calendar,
  AlertCircle,
} from "lucide-react";
import { NavView } from "@/components/layout/Sidebar";

const organizationSchema = z.object({
  organizationCode: z.string().min(2, "Code must be at least 2 characters").max(50),
  organizationName: z.string().min(2, "Name must be at least 2 characters").max(250),
  description: z.string().max(1000).optional(),
});

type OrganizationFormValues = z.infer<typeof organizationSchema>;

interface OrganizationsViewProps {
  onNavigate: (view: NavView) => void;
}

export function OrganizationsView({ onNavigate }: OrganizationsViewProps) {
  const { tenantCode } = useTenantStore();
  const queryClient = useQueryClient();

  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [search, setSearch] = useState("");
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<OrganizationFormValues>({
    resolver: zodResolver(organizationSchema),
    defaultValues: {
      organizationCode: "",
      organizationName: "",
      description: "",
    },
  });

  const { data: response, isLoading, isError, error } = useQuery({
    queryKey: ["organizations", tenantCode, search, page, size],
    queryFn: () => platformApi.getOrganizations({ search: search || undefined, page, size }),
  });

  const pagedData = response?.data;
  const organizations = pagedData?.content || [];

  const createMutation = useMutation({
    mutationFn: (data: CreateOrganizationRequest) => platformApi.createOrganization(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["organizations", tenantCode] });
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
          setError(ve.field as keyof OrganizationFormValues, { message: ve.message });
        });
      } else {
        setFormError(apiData?.message || "Failed to create organization.");
      }
    },
  });

  const onSubmit = (values: OrganizationFormValues) => {
    setFormError(null);
    createMutation.mutate({
      organizationCode: values.organizationCode,
      organizationName: values.organizationName,
      description: values.description || undefined,
    });
  };

  return (
    <div className="space-y-6">
      {/* Header bar */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
            <Building2 className="h-6 w-6 text-indigo-600" />
            Organizations
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Top-level organizational boundaries within tenant <span className="font-semibold text-slate-700 font-mono">tenant_{tenantCode}</span>.
          </p>
        </div>

        <Button onClick={() => setIsCreateOpen(true)} className="gap-2">
          <Plus className="h-4 w-4" />
          Create Organization
        </Button>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex items-center gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <Input
            placeholder="Search by name or code..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            className="pl-9 bg-white"
          />
        </div>
      </div>

      {/* Main Table Content */}
      {isLoading ? (
        <div className="flex h-64 items-center justify-center rounded-lg border border-slate-200 bg-white">
          <div className="text-sm text-slate-500 animate-pulse">Loading organizations...</div>
        </div>
      ) : isError ? (
        <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-center">
          <AlertCircle className="mx-auto h-8 w-8 text-red-500 mb-2" />
          <h3 className="text-sm font-semibold text-red-900">Failed to load organizations</h3>
          <p className="text-xs text-red-700 mt-1">
            {(error as { message?: string })?.message || "Verify your connection to backend service."}
          </p>
        </div>
      ) : organizations.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-slate-300 bg-white p-12 text-center">
          <Building2 className="h-12 w-12 text-slate-300 mb-3" />
          <h3 className="text-base font-semibold text-slate-900">No organizations found</h3>
          <p className="text-sm text-slate-500 max-w-sm mt-1 mb-4">
            {search
              ? `No organization matched "${search}". Try resetting your search filter.`
              : "Get started by creating your first organizational boundary."}
          </p>
          <Button onClick={() => setIsCreateOpen(true)} size="sm">
            <Plus className="h-4 w-4 mr-1" />
            Create Organization
          </Button>
        </div>
      ) : (
        <div className="space-y-4">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Code</TableHead>
                <TableHead>Organization Name</TableHead>
                <TableHead>Description</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created At</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {organizations.map((org: Organization) => (
                <TableRow key={org.organizationId || org.id}>
                  <TableCell className="font-mono text-xs font-semibold text-indigo-700">
                    {org.organizationCode || org.orgCode}
                  </TableCell>
                  <TableCell className="font-medium text-slate-900">
                    {org.organizationName || org.name}
                  </TableCell>
                  <TableCell className="text-xs text-slate-500 max-w-xs truncate">
                    {org.description || "—"}
                  </TableCell>
                  <TableCell>
                    <Badge variant={org.status === "ACTIVE" ? "success" : "inactive"}>
                      {org.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-xs text-slate-500">
                    <span className="flex items-center gap-1">
                      <Calendar className="h-3 w-3 text-slate-400" />
                      {org.createdAt ? new Date(org.createdAt).toLocaleDateString() : "—"}
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onNavigate("companies")}
                      className="text-xs text-indigo-600 hover:text-indigo-800"
                    >
                      View Companies &rarr;
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
                of <strong>{pagedData.totalElements}</strong> organizations
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

      {/* Create Organization Dialog */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className="sm:max-w-lg">
          <form onSubmit={handleSubmit(onSubmit)}>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Building2 className="h-5 w-5 text-indigo-600" />
                Create New Organization
              </DialogTitle>
              <DialogDescription>
                Establish a new top-level corporate or organizational boundary for tenant <code>{tenantCode}</code>.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              {formError && (
                <div className="rounded-lg bg-red-50 p-3 text-xs text-red-700 border border-red-200">
                  {formError}
                </div>
              )}

              <div className="space-y-1">
                <Label htmlFor="organizationCode" required>Organization Code</Label>
                <Input
                  id="organizationCode"
                  placeholder="e.g. ORG_NORTH"
                  {...register("organizationCode")}
                  error={errors.organizationCode?.message}
                />
              </div>

              <div className="space-y-1">
                <Label htmlFor="organizationName" required>Organization Name</Label>
                <Input
                  id="organizationName"
                  placeholder="e.g. Acme Holdings"
                  {...register("organizationName")}
                  error={errors.organizationName?.message}
                />
              </div>

              <div className="space-y-1">
                <Label htmlFor="description">Description</Label>
                <Input
                  id="description"
                  placeholder="Primary corporate group..."
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
                {isSubmitting ? "Creating..." : "Create Organization"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
