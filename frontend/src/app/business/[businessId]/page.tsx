"use client";

import React, { useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { useTenantStore } from "@/store/useTenantStore";
import { useBusinessStore } from "@/store/useBusinessStore";
import { platformApi } from "@/lib/services";
import { Header } from "@/components/layout/Header";
import { Sidebar } from "@/components/layout/Sidebar";
import { BusinessWorkspaceView } from "@/components/views/BusinessWorkspaceView";

export default function BusinessDeepLinkPage({
  params,
}: {
  params: Promise<{ businessId: string }>;
}) {
  const router = useRouter();
  const { businessId } = use(params);
  const { tenantCode } = useTenantStore();
  const { setCurrentBusiness } = useBusinessStore();

  const { data: businessRes, isLoading } = useQuery({
    queryKey: ["business", tenantCode, businessId],
    queryFn: () => platformApi.getBusiness(businessId),
    enabled: !!businessId,
  });

  useEffect(() => {
    if (businessRes?.data) {
      setCurrentBusiness(businessRes.data);
    }
  }, [businessRes, setCurrentBusiness]);

  return (
    <div className="min-h-screen flex flex-col bg-slate-100/60">
      <Header />
      <div className="flex flex-1">
        <Sidebar
          activeView="business-workspace"
          onSelectView={(view) => {
            if (view !== "business-workspace") {
              router.push("/");
            }
          }}
        />
        <main className="flex-1 p-8 overflow-y-auto max-w-7xl mx-auto w-full">
          {isLoading ? (
            <div className="flex h-64 items-center justify-center rounded-lg border border-slate-200 bg-white">
              <div className="text-sm text-slate-500 animate-pulse">Loading workspace...</div>
            </div>
          ) : (
            <BusinessWorkspaceView
              onNavigate={(view) => {
                if (view !== "business-workspace") {
                  router.push("/");
                }
              }}
            />
          )}
        </main>
      </div>
    </div>
  );
}
