"use client";

import React, { useState } from "react";
import { Header } from "@/components/layout/Header";
import { Sidebar, NavView } from "@/components/layout/Sidebar";
import { DashboardView } from "@/components/views/DashboardView";
import { OrganizationsView } from "@/components/views/OrganizationsView";
import { CompaniesView } from "@/components/views/CompaniesView";
import { BusinessesView } from "@/components/views/BusinessesView";
import { BusinessWorkspaceView } from "@/components/views/BusinessWorkspaceView";
import { ApplicationCatalogView } from "@/components/views/ApplicationCatalogView";
import { ReferenceDataView } from "@/components/views/ReferenceDataView";

export default function Home() {
  const [activeView, setActiveView] = useState<NavView>("dashboard");

  return (
    <div className="min-h-screen flex flex-col bg-slate-100/60">
      {/* Top Navigation & Context Bar */}
      <Header />

      {/* Main Workspace Layout: Sidebar + Viewport */}
      <div className="flex flex-1">
        <Sidebar activeView={activeView} onSelectView={setActiveView} />

        <main className="flex-1 p-8 overflow-y-auto max-w-7xl mx-auto w-full">
          {activeView === "dashboard" && (
            <DashboardView onNavigate={setActiveView} />
          )}
          {activeView === "organizations" && (
            <OrganizationsView onNavigate={setActiveView} />
          )}
          {activeView === "companies" && (
            <CompaniesView onNavigate={setActiveView} />
          )}
          {activeView === "businesses" && (
            <BusinessesView onNavigate={setActiveView} />
          )}
          {activeView === "business-workspace" && (
            <BusinessWorkspaceView onNavigate={setActiveView} />
          )}
          {activeView === "catalog" && <ApplicationCatalogView />}
          {activeView === "reference-data" && <ReferenceDataView />}
        </main>
      </div>
    </div>
  );
}
