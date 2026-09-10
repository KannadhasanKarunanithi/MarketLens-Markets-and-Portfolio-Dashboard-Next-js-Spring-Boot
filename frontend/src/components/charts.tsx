"use client";

import {
  Area,
  CartesianGrid,
  Cell,
  ComposedChart,
  Legend,
  Line,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { AllocationSlice, PerformancePoint } from "@/lib/types";
import { formatCurrencyCompact } from "@/lib/format";

const PALETTE = [
  "#2563eb",
  "#0d9488",
  "#f59e0b",
  "#8b5cf6",
  "#ec4899",
  "#65a30d",
  "#0ea5e9",
  "#ef4444",
  "#14b8a6",
  "#a855f7",
];

export function AllocationDonut({ slices }: { slices: AllocationSlice[] }) {
  if (slices.length === 0) {
    return <p className="py-8 text-center text-sm text-slate-500">No holdings to allocate.</p>;
  }
  return (
    <div className="h-64 w-full">
      <ResponsiveContainer>
        <PieChart>
          <Pie
            data={slices}
            dataKey="value"
            nameKey="label"
            innerRadius={55}
            outerRadius={90}
            paddingAngle={2}
          >
            {slices.map((slice, index) => (
              <Cell key={slice.label} fill={PALETTE[index % PALETTE.length]} />
            ))}
          </Pie>
          <Tooltip
            formatter={((value: number) => formatCurrencyCompact(value)) as never}
          />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}

export function PerformanceChart({ series }: { series: PerformancePoint[] }) {
  if (series.length < 2) {
    return (
      <p className="py-8 text-center text-sm text-slate-500">
        Not enough history yet. Record some trades to see performance.
      </p>
    );
  }
  return (
    <div className="h-72 w-full">
      <ResponsiveContainer>
        <ComposedChart data={series} margin={{ top: 8, right: 8, bottom: 0, left: 8 }}>
          <defs>
            <linearGradient id="portfolioFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#2563eb" stopOpacity={0.28} />
              <stop offset="100%" stopColor="#2563eb" stopOpacity={0.02} />
            </linearGradient>
          </defs>
          <CartesianGrid stroke="#f1f5f9" vertical={false} />
          <XAxis
            dataKey="date"
            tick={{ fontSize: 11, fill: "#94a3b8" }}
            minTickGap={40}
            tickLine={false}
            axisLine={false}
          />
          <YAxis
            tick={{ fontSize: 11, fill: "#94a3b8" }}
            tickFormatter={(v) => formatCurrencyCompact(v)}
            width={70}
            tickLine={false}
            axisLine={false}
          />
          <Tooltip
            formatter={((value: number, name: string) => [
              formatCurrencyCompact(value),
              name === "portfolioValue" ? "Portfolio" : "Benchmark",
            ]) as never}
          />
          <Area
            type="monotone"
            dataKey="portfolioValue"
            stroke="#2563eb"
            strokeWidth={2}
            fill="url(#portfolioFill)"
          />
          <Line
            type="monotone"
            dataKey="benchmarkValue"
            stroke="#94a3b8"
            strokeWidth={1.5}
            strokeDasharray="4 3"
            dot={false}
          />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  );
}
