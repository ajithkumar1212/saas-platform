"use client";

import React, { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTenantStore } from "@/store/useTenantStore";
import { useBusinessStore } from "@/store/useBusinessStore";
import { platformApi } from "@/lib/services";
import {
  Business,
  Store,
  CreateStoreRequest,
  SalesChannel,
  CreateChannelRequest,
  Brand,
  CreateBrandRequest,
  Warehouse,
  CreateWarehouseRequest,
  PosTerminal,
  CreatePosRequest,
  ApplicationCatalogItem,
  BusinessApplication,
} from "@/types/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
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
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
} from "@/components/ui/tabs";
import {
  Briefcase,
  Store as StoreIcon,
  Tag,
  Share2,
  Warehouse as WarehouseIcon,
  CreditCard,
  Layers,
  Plus,
  CheckCircle2,
  AlertCircle,
  Building,
  Clock,
  ArrowRight,
} from "lucide-react";
import { NavView } from "@/components/layout/Sidebar";

interface BusinessWorkspaceViewProps {
  onNavigate: (view: NavView) => void;
}

export function BusinessWorkspaceView({ onNavigate }: BusinessWorkspaceViewProps) {
  const { tenantCode } = useTenantStore();
  const { currentBusiness, setCurrentBusiness } = useBusinessStore();
  const queryClient = useQueryClient();

  const [activeTab, setActiveTab] = useState("overview");

  // Create Modals State
  const [isCreateStoreOpen, setIsCreateStoreOpen] = useState(false);
  const [isCreateBrandOpen, setIsCreateBrandOpen] = useState(false);
  const [isCreateChannelOpen, setIsCreateChannelOpen] = useState(false);
  const [isCreateWarehouseOpen, setIsCreateWarehouseOpen] = useState(false);
  const [isCreatePosOpen, setIsCreatePosOpen] = useState(false);
  const [modalError, setModalError] = useState<string | null>(null);

  // Store Form
  const [storeName, setStoreName] = useState("");
  const [storeCode, setStoreCode] = useState("");
  const [storeType, setStoreType] = useState("PHYSICAL");
  const [storeDesc, setStoreDesc] = useState("");

  // Brand Form
  const [brandName, setBrandName] = useState("");
  const [brandCode, setBrandCode] = useState("");
  const [brandDescription, setBrandDescription] = useState("");

  // Channel Form
  const [channelName, setChannelName] = useState("");
  const [channelCode, setChannelCode] = useState("");
  const [channelType, setChannelType] = useState("WEB");

  // Warehouse Form (requires a store)
  const [selectedStoreId, setSelectedStoreId] = useState("");
  const [warehouseName, setWarehouseName] = useState("");
  const [warehouseCode, setWarehouseCode] = useState("");
  const [warehouseType, setWarehouseType] = useState("STANDARD");

  // POS Form (requires a store)
  const [posCode, setPosCode] = useState("");
  const [posName, setPosName] = useState("");
  const [deviceIdentifier, setDeviceIdentifier] = useState("");

  // Fetch Businesses if none selected
  const { data: businessesResponse } = useQuery({
    queryKey: ["businesses", tenantCode],
    queryFn: () => platformApi.getBusinesses({ page: 0, size: 50 }),
    enabled: !currentBusiness,
  });
  const availableBusinesses: Business[] = businessesResponse?.data?.content || [];

  const businessId = currentBusiness?.businessId || currentBusiness?.id || "";

  // 1. Fetch Business Applications
  const { data: appsResponse } = useQuery({
    queryKey: ["business-apps", tenantCode, businessId],
    queryFn: () => platformApi.getBusinessApplications(businessId),
    enabled: !!businessId,
  });
  const enabledApps: BusinessApplication[] = appsResponse?.data || [];

  // 2. Fetch Catalog
  const { data: catalogResponse } = useQuery({
    queryKey: ["catalog"],
    queryFn: () => platformApi.getApplicationCatalog(),
  });
  const catalog: ApplicationCatalogItem[] = catalogResponse?.data || [];

  // 3. Fetch Stores
  const { data: storesResponse } = useQuery({
    queryKey: ["stores", tenantCode, businessId],
    queryFn: () => platformApi.getStoresByBusiness(businessId, { page: 0, size: 50 }),
    enabled: !!businessId,
  });
  const stores: Store[] = storesResponse?.data?.content || [];

  // 4. Fetch Brands
  const { data: brandsResponse } = useQuery({
    queryKey: ["brands", tenantCode, businessId],
    queryFn: () => platformApi.getBrandsByBusiness(businessId, { page: 0, size: 50 }),
    enabled: !!businessId,
  });
  const brands: Brand[] = brandsResponse?.data?.content || [];

  // 5. Fetch Channels
  const { data: channelsResponse } = useQuery({
    queryKey: ["channels", tenantCode, businessId],
    queryFn: () => platformApi.getChannelsByBusiness(businessId, { page: 0, size: 50 }),
    enabled: !!businessId,
  });
  const channels: SalesChannel[] = channelsResponse?.data?.content || [];

  // 6. Fetch Warehouses for the selected store or first store
  const activeStoreId = selectedStoreId || (stores.length > 0 ? (stores[0].storeId || stores[0].id || "") : "");
  const { data: warehousesResponse } = useQuery({
    queryKey: ["warehouses", tenantCode, activeStoreId],
    queryFn: () => platformApi.getWarehousesByStore(activeStoreId, { page: 0, size: 50 }),
    enabled: !!activeStoreId,
  });
  const warehouses: Warehouse[] = warehousesResponse?.data?.content || [];

  // 7. Fetch POS for the active store
  const { data: posResponse } = useQuery({
    queryKey: ["pos", tenantCode, activeStoreId],
    queryFn: () => platformApi.getPosByStore(activeStoreId, { page: 0, size: 50 }),
    enabled: !!activeStoreId,
  });
  const posTerminals: PosTerminal[] = posResponse?.data?.content || [];

  // Mutations
  const enableAppMutation = useMutation({
    mutationFn: (appCode: string) =>
      platformApi.enableBusinessApplication(businessId, { applicationCode: appCode }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["business-apps", tenantCode, businessId] });
    },
  });

  const createStoreMutation = useMutation({
    mutationFn: (data: CreateStoreRequest) => platformApi.createStore(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["stores", tenantCode, businessId] });
      setIsCreateStoreOpen(false);
      setStoreName("");
      setStoreCode("");
      setStoreDesc("");
      setModalError(null);
    },
    onError: (err: unknown) => {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setModalError(axiosErr.response?.data?.message || "Failed to create store.");
    },
  });

  const createBrandMutation = useMutation({
    mutationFn: (data: CreateBrandRequest) => platformApi.createBrand(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["brands", tenantCode, businessId] });
      setIsCreateBrandOpen(false);
      setBrandName("");
      setBrandCode("");
      setBrandDescription("");
      setModalError(null);
    },
    onError: (err: unknown) => {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setModalError(axiosErr.response?.data?.message || "Failed to create brand.");
    },
  });

  const createChannelMutation = useMutation({
    mutationFn: (data: CreateChannelRequest) => platformApi.createChannel(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["channels", tenantCode, businessId] });
      setIsCreateChannelOpen(false);
      setChannelName("");
      setChannelCode("");
      setModalError(null);
    },
    onError: (err: unknown) => {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setModalError(axiosErr.response?.data?.message || "Failed to create channel.");
    },
  });

  const createWarehouseMutation = useMutation({
    mutationFn: (data: CreateWarehouseRequest) => platformApi.createWarehouse(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["warehouses", tenantCode, activeStoreId] });
      setIsCreateWarehouseOpen(false);
      setWarehouseName("");
      setWarehouseCode("");
      setModalError(null);
    },
    onError: (err: unknown) => {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setModalError(axiosErr.response?.data?.message || "Failed to create warehouse.");
    },
  });

  const createPosMutation = useMutation({
    mutationFn: (data: CreatePosRequest) => platformApi.createPos(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pos", tenantCode, activeStoreId] });
      setIsCreatePosOpen(false);
      setPosCode("");
      setPosName("");
      setDeviceIdentifier("");
      setModalError(null);
    },
    onError: (err: unknown) => {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setModalError(axiosErr.response?.data?.message || "Failed to create POS terminal.");
    },
  });

  // If no business selected, display selection prompt
  if (!currentBusiness) {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center my-8">
        <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600 mb-4">
          <Briefcase className="h-8 w-8" />
        </div>
        <h2 className="text-xl font-bold text-slate-900">Select a Business Workspace</h2>
        <p className="text-sm text-slate-500 max-w-md mt-1 mb-6">
          The Business Workspace is the operational command center for stores, brands, sales channels, warehouses, and application enablement.
        </p>

        {availableBusinesses.length > 0 ? (
          <div className="w-full max-w-md space-y-2">
            <Label className="text-xs font-semibold text-slate-500 uppercase tracking-wider block text-left">
              Choose an active business:
            </Label>
            <div className="grid gap-2">
              {availableBusinesses.map((b) => (
                <button
                  key={b.businessId || b.id}
                  onClick={() => setCurrentBusiness(b)}
                  className="flex items-center justify-between p-3 rounded-lg border border-slate-200 hover:border-indigo-500 hover:bg-indigo-50/40 text-left transition-colors cursor-pointer group"
                >
                  <div>
                    <span className="font-semibold text-sm text-slate-900 group-hover:text-indigo-600">
                      {b.businessName || b.name}
                    </span>
                    <span className="text-xs text-slate-400 font-mono block">
                      {b.businessCode}
                    </span>
                  </div>
                  <ArrowRight className="h-4 w-4 text-slate-400 group-hover:text-indigo-600 transition-transform group-hover:translate-x-1" />
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="text-center">
            <p className="text-sm text-amber-700 bg-amber-50 p-3 rounded-lg border border-amber-200 mb-4">
              No businesses created yet in tenant <code className="font-mono">tenant_{tenantCode}</code>.
            </p>
            <Button onClick={() => onNavigate("businesses")}>
              Go to Businesses & Create One
            </Button>
          </div>
        )}
      </div>
    );
  }

  const businessDisplayName = currentBusiness.businessName || currentBusiness.name || "Business Workspace";

  return (
    <div className="space-y-6">
      {/* Workspace Header Context */}
      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-xs">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono text-xs font-bold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded">
                {currentBusiness.businessCode}
              </span>
              <Badge variant={currentBusiness.status === "ACTIVE" ? "success" : "inactive"}>
                {currentBusiness.status}
              </Badge>
            </div>
            <h1 className="text-2xl font-bold tracking-tight text-slate-900 mt-1">
              {businessDisplayName}
            </h1>
            <p className="text-sm text-slate-500 mt-1">
              Operational workspace scoped under Company &bull; Schema: <code className="font-mono text-xs text-indigo-700">tenant_{tenantCode}</code>
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                setCurrentBusiness(null);
                onNavigate("businesses");
              }}
              className="text-xs"
            >
              Switch Business
            </Button>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="mt-6 border-t border-slate-100 pt-4">
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="bg-slate-100 p-1 rounded-lg">
              <TabsTrigger value="overview" className="gap-1.5 text-xs">
                <Briefcase className="h-3.5 w-3.5" />
                Overview
              </TabsTrigger>
              <TabsTrigger value="applications" className="gap-1.5 text-xs">
                <Layers className="h-3.5 w-3.5" />
                Applications ({enabledApps.length})
              </TabsTrigger>
              <TabsTrigger value="stores" className="gap-1.5 text-xs">
                <StoreIcon className="h-3.5 w-3.5" />
                Stores ({stores.length})
              </TabsTrigger>
              <TabsTrigger value="brands" className="gap-1.5 text-xs">
                <Tag className="h-3.5 w-3.5" />
                Brands ({brands.length})
              </TabsTrigger>
              <TabsTrigger value="channels" className="gap-1.5 text-xs">
                <Share2 className="h-3.5 w-3.5" />
                Sales Channels ({channels.length})
              </TabsTrigger>
              <TabsTrigger value="warehouses" className="gap-1.5 text-xs">
                <WarehouseIcon className="h-3.5 w-3.5" />
                Warehouses & POS
              </TabsTrigger>
            </TabsList>

            {/* TAB 1: OVERVIEW */}
            <TabsContent value="overview" className="space-y-6 pt-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium text-slate-500">Enabled Apps</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold text-slate-900">{enabledApps.length}</div>
                    <div className="flex flex-wrap gap-1 mt-2">
                      {enabledApps.map((a) => (
                        <Badge key={a.businessApplicationId || a.applicationCode} variant="secondary" className="text-[10px]">
                          {a.applicationCode}
                        </Badge>
                      ))}
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium text-slate-500">Retail & Online Stores</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold text-slate-900">{stores.length}</div>
                    <p className="text-xs text-slate-400 mt-1">Operating points of sale</p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm font-medium text-slate-500">Brands & Channels</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold text-slate-900">
                      {brands.length + channels.length}
                    </div>
                    <p className="text-xs text-slate-400 mt-1">{brands.length} Brands &bull; {channels.length} Channels</p>
                  </CardContent>
                </Card>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Workspace Operations</CardTitle>
                  <CardDescription>
                    Configure operational assets for <span className="font-semibold">{businessDisplayName}</span>.
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="flex items-center justify-between p-3 rounded-lg border border-slate-200 bg-slate-50">
                    <div className="flex items-center gap-3">
                      <div className="p-2 rounded bg-indigo-100 text-indigo-700">
                        <Layers className="h-5 w-5" />
                      </div>
                      <div>
                        <div className="font-semibold text-sm text-slate-900">Enable Business Applications</div>
                        <div className="text-xs text-slate-500">Activate E-Commerce, Distribution, or Warehouse modules.</div>
                      </div>
                    </div>
                    <Button size="sm" variant="outline" onClick={() => setActiveTab("applications")}>
                      Configure Apps &rarr;
                    </Button>
                  </div>

                  <div className="flex items-center justify-between p-3 rounded-lg border border-slate-200 bg-slate-50">
                    <div className="flex items-center gap-3">
                      <div className="p-2 rounded bg-emerald-100 text-emerald-700">
                        <StoreIcon className="h-5 w-5" />
                      </div>
                      <div>
                        <div className="font-semibold text-sm text-slate-900">Create Stores & Outlets</div>
                        <div className="text-xs text-slate-500">Establish physical retail or digital storefronts.</div>
                      </div>
                    </div>
                    <Button size="sm" variant="outline" onClick={() => setActiveTab("stores")}>
                      Manage Stores &rarr;
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* TAB 2: APPLICATIONS */}
            <TabsContent value="applications" className="space-y-4 pt-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-base font-semibold text-slate-900">Business Applications</h3>
                  <p className="text-xs text-slate-500">
                    Enable and configure core SaaS application suites for this business workspace.
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                {catalog.map((app: ApplicationCatalogItem) => {
                  const isEnabled = enabledApps.some((ea) => ea.applicationCode === app.applicationCode);
                  return (
                    <Card key={app.applicationId || app.applicationCode} className={isEnabled ? "border-indigo-300 ring-1 ring-indigo-200" : ""}>
                      <CardHeader>
                        <div className="flex items-center justify-between">
                          <Badge variant={isEnabled ? "success" : "secondary"}>
                            {isEnabled ? "Enabled" : "Available"}
                          </Badge>
                          <span className="font-mono text-xs text-slate-400">Catalog Item</span>
                        </div>
                        <CardTitle className="text-base mt-2 flex items-center gap-2">
                          <Layers className="h-4 w-4 text-indigo-600" />
                          {app.applicationName || app.applicationCode}
                        </CardTitle>
                        <CardDescription className="text-xs line-clamp-2">
                          {app.description || "Core operational module."}
                        </CardDescription>
                      </CardHeader>
                      <CardContent>
                        {isEnabled ? (
                          <div className="flex items-center gap-1.5 text-xs text-emerald-700 font-medium bg-emerald-50 p-2 rounded">
                            <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                            Active for this workspace
                          </div>
                        ) : (
                          <Button
                            size="sm"
                            className="w-full text-xs"
                            onClick={() => enableAppMutation.mutate(app.applicationCode)}
                            disabled={enableAppMutation.isPending}
                          >
                            Enable Application
                          </Button>
                        )}
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            </TabsContent>

            {/* TAB 3: STORES */}
            <TabsContent value="stores" className="space-y-4 pt-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-base font-semibold text-slate-900">Stores & Outlets</h3>
                  <p className="text-xs text-slate-500">
                    Retail stores, outlets, and digital centers under {businessDisplayName}.
                  </p>
                </div>
                <Button size="sm" onClick={() => setIsCreateStoreOpen(true)} className="gap-1.5">
                  <Plus className="h-4 w-4" />
                  Create Store
                </Button>
              </div>

              {stores.length === 0 ? (
                <div className="p-8 text-center rounded-xl border border-dashed border-slate-300 bg-white">
                  <StoreIcon className="mx-auto h-8 w-8 text-slate-300 mb-2" />
                  <p className="text-sm font-medium text-slate-700">No stores created yet</p>
                  <Button size="sm" onClick={() => setIsCreateStoreOpen(true)} className="mt-3">
                    Add First Store
                  </Button>
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Store Code</TableHead>
                      <TableHead>Store Name</TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead>Description</TableHead>
                      <TableHead>Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {stores.map((s: Store) => (
                      <TableRow key={s.storeId || s.id}>
                        <TableCell className="font-mono text-xs font-semibold text-indigo-700">
                          {s.storeCode}
                        </TableCell>
                        <TableCell className="font-medium text-slate-900">{s.storeName || s.name}</TableCell>
                        <TableCell>
                          <Badge variant="secondary" className="text-[10px]">
                            {s.storeType}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-xs text-slate-400">{s.description || "—"}</TableCell>
                        <TableCell>
                          <Badge variant={s.status === "ACTIVE" ? "success" : "inactive"}>
                            {s.status}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </TabsContent>

            {/* TAB 4: BRANDS */}
            <TabsContent value="brands" className="space-y-4 pt-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-base font-semibold text-slate-900">Brands</h3>
                  <p className="text-xs text-slate-500">
                    Product and marketing brands associated with {businessDisplayName}.
                  </p>
                </div>
                <Button size="sm" onClick={() => setIsCreateBrandOpen(true)} className="gap-1.5">
                  <Plus className="h-4 w-4" />
                  Create Brand
                </Button>
              </div>

              {brands.length === 0 ? (
                <div className="p-8 text-center rounded-xl border border-dashed border-slate-300 bg-white">
                  <Tag className="mx-auto h-8 w-8 text-slate-300 mb-2" />
                  <p className="text-sm font-medium text-slate-700">No brands registered</p>
                  <Button size="sm" onClick={() => setIsCreateBrandOpen(true)} className="mt-3">
                    Add First Brand
                  </Button>
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Brand Code</TableHead>
                      <TableHead>Brand Name</TableHead>
                      <TableHead>Description</TableHead>
                      <TableHead>Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {brands.map((b: Brand) => (
                      <TableRow key={b.brandId || b.id}>
                        <TableCell className="font-mono text-xs font-semibold text-indigo-700">
                          {b.brandCode}
                        </TableCell>
                        <TableCell className="font-medium text-slate-900">{b.brandName || b.name}</TableCell>
                        <TableCell className="text-xs text-slate-500">{b.description || "—"}</TableCell>
                        <TableCell>
                          <Badge variant={b.status === "ACTIVE" ? "success" : "inactive"}>
                            {b.status}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </TabsContent>

            {/* TAB 5: SALES CHANNELS */}
            <TabsContent value="channels" className="space-y-4 pt-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-base font-semibold text-slate-900">Sales Channels</h3>
                  <p className="text-xs text-slate-500">
                    Distribution and sales channels (Web, Mobile, B2B Portal, Marketplace).
                  </p>
                </div>
                <Button size="sm" onClick={() => setIsCreateChannelOpen(true)} className="gap-1.5">
                  <Plus className="h-4 w-4" />
                  Create Channel
                </Button>
              </div>

              {channels.length === 0 ? (
                <div className="p-8 text-center rounded-xl border border-dashed border-slate-300 bg-white">
                  <Share2 className="mx-auto h-8 w-8 text-slate-300 mb-2" />
                  <p className="text-sm font-medium text-slate-700">No sales channels configured</p>
                  <Button size="sm" onClick={() => setIsCreateChannelOpen(true)} className="mt-3">
                    Add Sales Channel
                  </Button>
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Channel Code</TableHead>
                      <TableHead>Channel Name</TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead>Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {channels.map((ch: SalesChannel) => (
                      <TableRow key={ch.channelId || ch.id}>
                        <TableCell className="font-mono text-xs font-semibold text-indigo-700">
                          {ch.channelCode}
                        </TableCell>
                        <TableCell className="font-medium text-slate-900">{ch.channelName || ch.name}</TableCell>
                        <TableCell>
                          <Badge variant="secondary" className="text-[10px]">
                            {ch.channelType}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge variant={ch.status === "ACTIVE" ? "success" : "inactive"}>
                            {ch.status}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </TabsContent>

            {/* TAB 6: WAREHOUSES & POS */}
            <TabsContent value="warehouses" className="space-y-6 pt-4">
              {stores.length === 0 ? (
                <div className="rounded-lg bg-amber-50 p-6 text-center border border-amber-200">
                  <AlertCircle className="mx-auto h-8 w-8 text-amber-600 mb-2" />
                  <p className="text-sm font-semibold text-amber-900">
                    Stores are required first
                  </p>
                  <p className="text-xs text-amber-700 mt-1 mb-4">
                    In the Phase 1 schema, Warehouses and POS terminals are configured under a Store.
                  </p>
                  <Button size="sm" onClick={() => setIsCreateStoreOpen(true)}>
                    Create Store First
                  </Button>
                </div>
              ) : (
                <div className="space-y-6">
                  {/* Store Selector */}
                  <div className="flex items-center gap-3 bg-white p-3 rounded-lg border border-slate-200">
                    <Label htmlFor="store-select" className="text-xs font-semibold text-slate-600">
                      Store Scope:
                    </Label>
                    <select
                      id="store-select"
                      value={activeStoreId}
                      onChange={(e) => setSelectedStoreId(e.target.value)}
                      className="h-8 rounded-md border border-slate-300 bg-white px-2 py-1 text-xs focus:ring-1 focus:ring-indigo-500"
                    >
                      {stores.map((s) => (
                        <option key={s.storeId || s.id} value={s.storeId || s.id}>
                          {s.storeName || s.name} ({s.storeCode})
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Warehouses Section */}
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <h4 className="text-sm font-bold text-slate-800 flex items-center gap-2">
                        <WarehouseIcon className="h-4 w-4 text-indigo-600" />
                        Warehouses
                      </h4>
                      <Button size="sm" variant="outline" onClick={() => setIsCreateWarehouseOpen(true)} className="text-xs">
                        <Plus className="h-3.5 w-3.5 mr-1" />
                        Add Warehouse
                      </Button>
                    </div>

                    {warehouses.length === 0 ? (
                      <p className="text-xs text-slate-400 bg-slate-50 p-4 rounded border text-center">
                        No warehouses associated with this store.
                      </p>
                    ) : (
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Warehouse Code</TableHead>
                            <TableHead>Name</TableHead>
                            <TableHead>Type</TableHead>
                            <TableHead>Status</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {warehouses.map((w: Warehouse) => (
                            <TableRow key={w.warehouseId || w.id}>
                              <TableCell className="font-mono text-xs text-indigo-700">{w.warehouseCode}</TableCell>
                              <TableCell className="font-medium text-slate-900">{w.warehouseName || w.name}</TableCell>
                              <TableCell><Badge variant="secondary" className="text-[10px]">{w.warehouseType}</Badge></TableCell>
                              <TableCell><Badge variant={w.status === "ACTIVE" ? "success" : "inactive"}>{w.status}</Badge></TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    )}
                  </div>

                  {/* POS Terminals Section */}
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <h4 className="text-sm font-bold text-slate-800 flex items-center gap-2">
                        <CreditCard className="h-4 w-4 text-indigo-600" />
                        POS Terminals
                      </h4>
                      <Button size="sm" variant="outline" onClick={() => setIsCreatePosOpen(true)} className="text-xs">
                        <Plus className="h-3.5 w-3.5 mr-1" />
                        Add POS Terminal
                      </Button>
                    </div>

                    {posTerminals.length === 0 ? (
                      <p className="text-xs text-slate-400 bg-slate-50 p-4 rounded border text-center">
                        No POS terminals associated with this store.
                      </p>
                    ) : (
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Terminal Code</TableHead>
                            <TableHead>Terminal Name</TableHead>
                            <TableHead>Device ID</TableHead>
                            <TableHead>Status</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {posTerminals.map((pos: PosTerminal) => (
                            <TableRow key={pos.posId || pos.id}>
                              <TableCell className="font-mono text-xs text-indigo-700">{pos.posCode}</TableCell>
                              <TableCell className="font-medium text-slate-900">{pos.posName || pos.name}</TableCell>
                              <TableCell className="font-mono text-xs text-slate-500">{pos.deviceIdentifier || "—"}</TableCell>
                              <TableCell><Badge variant={pos.status === "ACTIVE" ? "success" : "inactive"}>{pos.status}</Badge></TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    )}
                  </div>
                </div>
              )}
            </TabsContent>
          </Tabs>
        </div>
      </div>

      {/* CREATE STORE DIALOG */}
      <Dialog open={isCreateStoreOpen} onOpenChange={setIsCreateStoreOpen}>
        <DialogContent className="sm:max-w-md">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              createStoreMutation.mutate({
                businessId,
                storeCode,
                storeName,
                storeType,
                description: storeDesc || undefined,
              });
            }}
          >
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <StoreIcon className="h-5 w-5 text-indigo-600" />
                Create New Store
              </DialogTitle>
              <DialogDescription>
                Add a physical or digital retail storefront under {businessDisplayName}.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-3 py-4">
              {modalError && (
                <div className="rounded bg-red-50 p-2.5 text-xs text-red-700 border border-red-200">
                  {modalError}
                </div>
              )}
              <div className="space-y-1">
                <Label htmlFor="stCode" required>Store Code</Label>
                <Input
                  id="stCode"
                  placeholder="e.g. STR_FLAGSHIP_01"
                  value={storeCode}
                  onChange={(e) => setStoreCode(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="stName" required>Store Name</Label>
                <Input
                  id="stName"
                  placeholder="e.g. Flagship Broadway"
                  value={storeName}
                  onChange={(e) => setStoreName(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="stType" required>Type</Label>
                <select
                  id="stType"
                  value={storeType}
                  onChange={(e) => setStoreType(e.target.value)}
                  className="flex h-9 w-full rounded-md border border-slate-300 bg-white px-2 text-xs"
                >
                  <option value="PHYSICAL">PHYSICAL</option>
                  <option value="ONLINE">ONLINE</option>
                  <option value="HYBRID">HYBRID</option>
                </select>
              </div>
              <div className="space-y-1">
                <Label htmlFor="stDesc">Description</Label>
                <Input
                  id="stDesc"
                  placeholder="Store details..."
                  value={storeDesc}
                  onChange={(e) => setStoreDesc(e.target.value)}
                />
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="ghost" size="sm" onClick={() => setIsCreateStoreOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" disabled={createStoreMutation.isPending}>
                {createStoreMutation.isPending ? "Creating..." : "Create Store"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* CREATE BRAND DIALOG */}
      <Dialog open={isCreateBrandOpen} onOpenChange={setIsCreateBrandOpen}>
        <DialogContent className="sm:max-w-md">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              createBrandMutation.mutate({
                businessId,
                brandCode,
                brandName,
                description: brandDescription || undefined,
              });
            }}
          >
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Tag className="h-5 w-5 text-indigo-600" />
                Create New Brand
              </DialogTitle>
              <DialogDescription>
                Register a distinct product/market brand under {businessDisplayName}.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-3 py-4">
              {modalError && (
                <div className="rounded bg-red-50 p-2.5 text-xs text-red-700 border border-red-200">
                  {modalError}
                </div>
              )}
              <div className="space-y-1">
                <Label htmlFor="brCode" required>Brand Code</Label>
                <Input
                  id="brCode"
                  placeholder="e.g. BRD_APEX"
                  value={brandCode}
                  onChange={(e) => setBrandCode(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="brName" required>Brand Name</Label>
                <Input
                  id="brName"
                  placeholder="e.g. Apex Living"
                  value={brandName}
                  onChange={(e) => setBrandName(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="brDesc">Description</Label>
                <Input
                  id="brDesc"
                  placeholder="Premium home line"
                  value={brandDescription}
                  onChange={(e) => setBrandDescription(e.target.value)}
                />
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="ghost" size="sm" onClick={() => setIsCreateBrandOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" disabled={createBrandMutation.isPending}>
                {createBrandMutation.isPending ? "Creating..." : "Create Brand"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* CREATE CHANNEL DIALOG */}
      <Dialog open={isCreateChannelOpen} onOpenChange={setIsCreateChannelOpen}>
        <DialogContent className="sm:max-w-md">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              createChannelMutation.mutate({
                businessId,
                channelCode,
                channelName,
                channelType,
              });
            }}
          >
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Share2 className="h-5 w-5 text-indigo-600" />
                Create Sales Channel
              </DialogTitle>
              <DialogDescription>
                Define an omnichannel route to market for {businessDisplayName}.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-3 py-4">
              {modalError && (
                <div className="rounded bg-red-50 p-2.5 text-xs text-red-700 border border-red-200">
                  {modalError}
                </div>
              )}
              <div className="space-y-1">
                <Label htmlFor="chCode" required>Channel Code</Label>
                <Input
                  id="chCode"
                  placeholder="e.g. CH_WEB_DIRECT"
                  value={channelCode}
                  onChange={(e) => setChannelCode(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="chName" required>Channel Name</Label>
                <Input
                  id="chName"
                  placeholder="e.g. Direct Web Store"
                  value={channelName}
                  onChange={(e) => setChannelName(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="chType" required>Channel Type</Label>
                <select
                  id="chType"
                  value={channelType}
                  onChange={(e) => setChannelType(e.target.value)}
                  className="flex h-9 w-full rounded-md border border-slate-300 bg-white px-2 text-xs"
                >
                  <option value="WEB">WEB</option>
                  <option value="MOBILE">MOBILE</option>
                  <option value="MARKETPLACE">MARKETPLACE</option>
                  <option value="B2B_PORTAL">B2B_PORTAL</option>
                  <option value="IN_STORE">IN_STORE</option>
                </select>
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="ghost" size="sm" onClick={() => setIsCreateChannelOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" disabled={createChannelMutation.isPending}>
                {createChannelMutation.isPending ? "Creating..." : "Create Channel"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* CREATE WAREHOUSE DIALOG */}
      <Dialog open={isCreateWarehouseOpen} onOpenChange={setIsCreateWarehouseOpen}>
        <DialogContent className="sm:max-w-md">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              createWarehouseMutation.mutate({
                storeId: activeStoreId,
                warehouseCode,
                warehouseName,
                warehouseType,
              });
            }}
          >
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <WarehouseIcon className="h-5 w-5 text-indigo-600" />
                Add Warehouse
              </DialogTitle>
              <DialogDescription>
                Configure fulfillment capacity under selected store.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-3 py-4">
              {modalError && (
                <div className="rounded bg-red-50 p-2.5 text-xs text-red-700 border border-red-200">
                  {modalError}
                </div>
              )}
              <div className="space-y-1">
                <Label htmlFor="whCode" required>Warehouse Code</Label>
                <Input
                  id="whCode"
                  placeholder="e.g. WH_EAST_01"
                  value={warehouseCode}
                  onChange={(e) => setWarehouseCode(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="whName" required>Warehouse Name</Label>
                <Input
                  id="whName"
                  placeholder="e.g. Main Distribution Hub"
                  value={warehouseName}
                  onChange={(e) => setWarehouseName(e.target.value)}
                  required
                />
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="ghost" size="sm" onClick={() => setIsCreateWarehouseOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" disabled={createWarehouseMutation.isPending}>
                {createWarehouseMutation.isPending ? "Creating..." : "Add Warehouse"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* CREATE POS DIALOG */}
      <Dialog open={isCreatePosOpen} onOpenChange={setIsCreatePosOpen}>
        <DialogContent className="sm:max-w-md">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              createPosMutation.mutate({
                storeId: activeStoreId,
                posCode,
                posName,
                deviceIdentifier: deviceIdentifier || undefined,
              });
            }}
          >
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <CreditCard className="h-5 w-5 text-indigo-600" />
                Add POS Terminal
              </DialogTitle>
              <DialogDescription>
                Register point-of-sale hardware under selected store.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-3 py-4">
              {modalError && (
                <div className="rounded bg-red-50 p-2.5 text-xs text-red-700 border border-red-200">
                  {modalError}
                </div>
              )}
              <div className="space-y-1">
                <Label htmlFor="posCode" required>Terminal Code</Label>
                <Input
                  id="posCode"
                  placeholder="e.g. POS_TERM_01"
                  value={posCode}
                  onChange={(e) => setPosCode(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="posName" required>Terminal Name</Label>
                <Input
                  id="posName"
                  placeholder="e.g. Counter 1 Terminal"
                  value={posName}
                  onChange={(e) => setPosName(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="posDevId">Device Identifier</Label>
                <Input
                  id="posDevId"
                  placeholder="e.g. SN-8829104"
                  value={deviceIdentifier}
                  onChange={(e) => setDeviceIdentifier(e.target.value)}
                />
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="ghost" size="sm" onClick={() => setIsCreatePosOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" disabled={createPosMutation.isPending}>
                {createPosMutation.isPending ? "Creating..." : "Add POS"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
