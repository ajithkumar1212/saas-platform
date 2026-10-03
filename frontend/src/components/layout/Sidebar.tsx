"use client";

import React from "react";
import { useBusinessStore } from "@/store/useBusinessStore";
import {
  LayoutDashboard,
  Building2,
  Building,
  Briefcase,
  Store,
  Tag,
  Share2,
  Warehouse,
  CreditCard,
  Layers,
  ArrowLeft,
  Settings2,
  Sparkles,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";

export type NavView =
  | "dashboard"
  | "organizations"
  | "companies"
  | "businesses"
  | "business-workspace"
  | "catalog"
  | "reference-data";

interface SidebarProps {
  activeView: NavView;
  onSelectView: (view: NavView) => void;
}

export function Sidebar({ activeView, onSelectView }: SidebarProps) {
  const { currentBusiness, setCurrentBusiness } = useBusinessStore();

  return (
    <aside className="w-64 shrink-0 border-r border-slate-200 bg-slate-50/70 p-4 flex flex-col justify-between min-h-[calc(100vh-4rem)]">
      <div className="space-y-6">
        {/* If in Business Workspace mode, show active business context banner */}
        {currentBusiness && (
          <div className="rounded-lg border border-indigo-200 bg-indigo-50/70 p-3 shadow-xs">
            <div className="flex items-center justify-between mb-1">
              <span className="text-[11px] font-bold uppercase tracking-wider text-indigo-700">
                Active Business
              </span>
              <button
                type="button"
                onClick={() => {
                  setCurrentBusiness(null);
                  onSelectView("businesses");
                }}
                className="text-[11px] font-medium text-indigo-600 hover:text-indigo-800 flex items-center gap-0.5 cursor-pointer"
                title="Exit Business Workspace"
              >
                <ArrowLeft className="h-3 w-3" />
                Exit
              </button>
            </div>
            <div className="font-semibold text-slate-900 text-sm truncate">
              {currentBusiness.name}
            </div>
            <div className="flex items-center gap-2 mt-1">
              <Badge variant="secondary" className="text-[10px] px-1.5 py-0">
                {currentBusiness.businessType}
              </Badge>
              <span className="text-xs text-slate-500 font-mono">
                {currentBusiness.currency}
              </span>
            </div>
          </div>
        )}

        {/* Section 1: Core Organization & Hierarchy */}
        <div>
          <div className="px-3 mb-2 text-xs font-semibold uppercase tracking-wider text-slate-400">
            Control Plane
          </div>
          <nav className="space-y-1">
            <button
              type="button"
              onClick={() => onSelectView("dashboard")}
              className={`flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors cursor-pointer ${
                activeView === "dashboard"
                  ? "bg-white text-indigo-600 shadow-xs font-semibold"
                  : "text-slate-600 hover:bg-slate-100/80 hover:text-slate-900"
              }`}
            >
              <LayoutDashboard className="h-4 w-4" />
              Overview
            </button>

            <button
              type="button"
              onClick={() => onSelectView("organizations")}
              className={`flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors cursor-pointer ${
                activeView === "organizations"
                  ? "bg-white text-indigo-600 shadow-xs font-semibold"
                  : "text-slate-600 hover:bg-slate-100/80 hover:text-slate-900"
              }`}
            >
              <Building2 className="h-4 w-4" />
              Organizations
            </button>

            <button
              type="button"
              onClick={() => onSelectView("companies")}
              className={`flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors cursor-pointer ${
                activeView === "companies"
                  ? "bg-white text-indigo-600 shadow-xs font-semibold"
                  : "text-slate-600 hover:bg-slate-100/80 hover:text-slate-900"
              }`}
            >
              <Building className="h-4 w-4" />
              Companies
            </button>

            <button
              type="button"
              onClick={() => onSelectView("businesses")}
              className={`flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors cursor-pointer ${
                activeView === "businesses"
                  ? "bg-white text-indigo-600 shadow-xs font-semibold"
                  : "text-slate-600 hover:bg-slate-100/80 hover:text-slate-900"
              }`}
            >
              <Briefcase className="h-4 w-4" />
              Businesses
            </button>
          </nav>
        </div>

        {/* Section 2: Business Workspace Operations (Active or Shortcut) */}
        <div>
          <div className="px-3 mb-2 flex items-center justify-between text-xs font-semibold uppercase tracking-wider text-slate-400">
            <span>Business Workspace</span>
            {currentBusiness && (
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
            )}
          </div>

          <nav className="space-y-1">
            <button
              type="button"
              onClick={() => onSelectView("business-workspace")}
              className={`flex w-full items-center justify-between rounded-lg px-3 py-2 text-sm font-medium transition-colors cursor-pointer ${
                activeView === "business-workspace"
                  ? "bg-white text-indigo-600 shadow-xs font-semibold"
                  : "text-slate-600 hover:bg-slate-100/80 hover:text-slate-900"
              }`}
            >
              <div className="flex items-center gap-3">
                <Settings2 className="h-4 w-4" />
                <span>Workspace Hub</span>
              </div>
              {currentBusiness ? (
                <Badge variant="success" className="text-[10px] px-1.5 py-0">
                  Ready
                </Badge>
              ) : (
                <span className="text-[11px] text-slate-400">Select</span>
              )}
            </button>
          </nav>
        </div>

        {/* Section 3: Applications & Catalog */}
        <div>
          <div className="px-3 mb-2 text-xs font-semibold uppercase tracking-wider text-slate-400">
            Platform Capabilities
          </div>
          <nav className="space-y-1">
            <button
              type="button"
              onClick={() => onSelectView("catalog")}
              className={`flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors cursor-pointer ${
                activeView === "catalog"
                  ? "bg-white text-indigo-600 shadow-xs font-semibold"
                  : "text-slate-600 hover:bg-slate-100/80 hover:text-slate-900"
              }`}
            >
              <Layers className="h-4 w-4" />
              Application Catalog
            </button>

            <button
              type="button"
              onClick={() => onSelectView("reference-data")}
              className={`flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors cursor-pointer ${
                activeView === "reference-data"
                  ? "bg-white text-indigo-600 shadow-xs font-semibold"
                  : "text-slate-600 hover:bg-slate-100/80 hover:text-slate-900"
              }`}
            >
              <Sparkles className="h-4 w-4" />
              Reference Data
            </button>
          </nav>
        </div>
      </div>

      {/* Footer / System Status */}
      <div className="rounded-lg border border-slate-200 bg-white p-3 text-xs text-slate-500 shadow-2xs">
        <div className="flex items-center justify-between font-medium text-slate-700 mb-1">
          <span>Backend REST API</span>
          <span className="inline-block h-2 w-2 rounded-full bg-emerald-500" />
        </div>
        <div className="font-mono text-[11px] text-slate-400 truncate">
          http://localhost:8081
        </div>
      </div>
    </aside>
  );
}
