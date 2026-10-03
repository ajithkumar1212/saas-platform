"use client";

import React from "react";
import { useQuery } from "@tanstack/react-query";
import { platformApi } from "@/lib/services";
import { ApplicationCatalogItem } from "@/types/api";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Layers, CheckCircle2, Box } from "lucide-react";

export function ApplicationCatalogView() {
  const { data: response, isLoading } = useQuery({
    queryKey: ["catalog"],
    queryFn: () => platformApi.getApplicationCatalog(),
  });

  const catalog = response?.data || [];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
          <Layers className="h-6 w-6 text-indigo-600" />
          Global Application Catalog
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Platform-level application catalog available for enablement across all tenant business workspaces.
        </p>
      </div>

      {isLoading ? (
        <div className="flex h-64 items-center justify-center rounded-lg border border-slate-200 bg-white">
          <div className="text-sm text-slate-500 animate-pulse">Loading catalog...</div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {catalog.map((app: ApplicationCatalogItem) => (
            <Card key={app.applicationId || app.applicationCode} className="hover:shadow-md transition-shadow">
              <CardHeader>
                <div className="flex items-center justify-between mb-1">
                  <Badge variant="outline" className="font-mono text-xs border-indigo-200 text-indigo-700 bg-indigo-50">
                    {app.applicationCode}
                  </Badge>
                  <span className="text-xs text-slate-400 font-mono">Catalog Item</span>
                </div>
                <CardTitle className="text-lg flex items-center gap-2">
                  <Box className="h-5 w-5 text-indigo-600" />
                  {app.applicationName || app.applicationCode}
                </CardTitle>
                <CardDescription className="text-xs mt-1">
                  {app.description || "Core SaaS business domain module."}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="rounded-lg bg-slate-50 p-3 text-xs text-slate-600 space-y-1.5 border border-slate-100">
                  <div className="flex items-center gap-1.5 text-emerald-700 font-medium">
                    <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
                    Global Catalog Registered
                  </div>
                  <div className="text-[11px] text-slate-400">
                    Status: <span className="font-semibold text-slate-700">{app.status || "ACTIVE"}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
