"use client";

import React, { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { platformApi } from "@/lib/services";
import { ReferenceValue } from "@/types/api";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Sparkles, Database } from "lucide-react";

const DATA_TYPES = [
  { code: "BUSINESS_TYPE", label: "Business Types" },
  { code: "STORE_TYPE", label: "Store Types" },
  { code: "CHANNEL_TYPE", label: "Sales Channel Types" },
  { code: "CURRENCY", label: "Currencies" },
  { code: "COUNTRY", label: "Countries" },
];

export function ReferenceDataView() {
  const [selectedType, setSelectedType] = useState("BUSINESS_TYPE");

  const { data: response, isLoading } = useQuery({
    queryKey: ["reference-data", selectedType],
    queryFn: () => platformApi.getReferenceValues(selectedType),
  });

  const values: ReferenceValue[] = response?.data || [];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
          <Sparkles className="h-6 w-6 text-indigo-600" />
          Standard Reference Data
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Platform-standard enumeration and lookup dictionaries backed by schema reference tables.
        </p>
      </div>

      <div className="flex flex-wrap gap-2">
        {DATA_TYPES.map((t) => (
          <button
            key={t.code}
            onClick={() => setSelectedType(t.code)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors cursor-pointer ${
              selectedType === t.code
                ? "bg-indigo-600 text-white shadow-xs"
                : "bg-white text-slate-600 border border-slate-200 hover:bg-slate-50"
            }`}
          >
            {t.label} ({t.code})
          </button>
        ))}
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-3">
          <CardTitle className="text-base flex items-center gap-2">
            <Database className="h-4 w-4 text-indigo-600" />
            Lookup Values for {selectedType}
          </CardTitle>
          <Badge variant="secondary" className="font-mono text-xs">
            {values.length} records
          </Badge>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="py-8 text-center text-xs text-slate-400">Loading reference data...</div>
          ) : values.length === 0 ? (
            <div className="py-8 text-center text-xs text-slate-400">No records found for {selectedType}.</div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Code</TableHead>
                  <TableHead>Display Value</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Order</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {values.map((v) => (
                  <TableRow key={v.referenceValueId || v.valueCode}>
                    <TableCell className="font-mono text-xs font-semibold text-indigo-700">
                      {v.valueCode}
                    </TableCell>
                    <TableCell className="font-medium text-slate-900">
                      {v.valueName}
                    </TableCell>
                    <TableCell className="text-xs text-slate-500">
                      {v.description || "—"}
                    </TableCell>
                    <TableCell className="font-mono text-xs text-slate-400">
                      {v.sortOrder ?? "—"}
                    </TableCell>
                    <TableCell>
                      <Badge variant={v.status === "ACTIVE" ? "success" : "inactive"}>
                        {v.status || "ACTIVE"}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
