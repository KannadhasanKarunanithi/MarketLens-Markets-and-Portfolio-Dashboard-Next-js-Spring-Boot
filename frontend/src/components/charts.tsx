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
  "#f2b74a",
  "#35c65a",
  "#e0684a",
  "#7c8cf8",
  "#d267c0",
  "#4bb3c9",
  "#a8b23c",
  "#e8925a",
  "#5ec7a0",
  "#c77dd6",
];

const AXIS = "#626b7e";
const GRID = "#1e2532";

const tooltipStyle = {
  backgroundColor: "#12161f",
  border: "1px solid #262d3b",
  borderRadius: 6,
  color: "#e7eaf0",
  fontSize: 12,
};

export function AllocationDonut({ slices }: { slices: AllocationSlice[] }) {
  if (slices.length === 0) {
    return <p className="py-8 text-center text-sm text-ink-dim">No holdings to allocate.</p>;
  }
  return (
    <div className="h-64 w-full min-w-0">
      <ResponsiveContainer width="99%" height="100%">
        <PieChart>
          <Pie
            data={slices}
            dataKey="value"
            nameKey="label"
            cx="50%"
            cy="45%"
            innerRadius={50}
            outerRadius={82}
            paddingAngle={2}
            stroke="#12161f"
            isAnimationActive={false}
          >
            {slices.map((slice, index) => (
              <Cell key={slice.label} fill={PALETTE[index % PALETTE.length]} />
            ))}
          </Pie>
          <Tooltip
            contentStyle={tooltipStyle}
            formatter={((value: number) => formatCurrencyCompact(value)) as never}
          />
          <Legend wrapperStyle={{ fontSize: 12, color: "#99a1b3" }} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}

export function PerformanceChart({ series }: { series: PerformancePoint[] }) {
  if (series.length < 2) {
    return (
      <p className="py-8 text-center text-sm text-ink-dim">
        Not enough history yet. Record some trades to see performance.
      </p>
    );
  }
  return (
    <div className="h-72 w-full min-w-0">
      <ResponsiveContainer width="99%" height="100%">
        <ComposedChart data={series} margin={{ top: 8, right: 8, bottom: 0, left: 8 }}>
          <defs>
            <linearGradient id="portfolioFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#f2b74a" stopOpacity={0.25} />
              <stop offset="100%" stopColor="#f2b74a" stopOpacity={0.02} />
            </linearGradient>
          </defs>
          <CartesianGrid stroke={GRID} vertical={false} />
          <XAxis
            dataKey="date"
            tick={{ fontSize: 11, fill: AXIS }}
            minTickGap={40}
            tickLine={false}
            axisLine={{ stroke: GRID }}
          />
          <YAxis
            tick={{ fontSize: 11, fill: AXIS }}
            tickFormatter={(v) => formatCurrencyCompact(v)}
            width={70}
            tickLine={false}
            axisLine={false}
          />
          <Tooltip
            contentStyle={tooltipStyle}
            formatter={((value: number, name: string) => [
              formatCurrencyCompact(value),
              name === "portfolioValue" ? "Portfolio" : "Benchmark",
            ]) as never}
          />
          <Area
            type="monotone"
            dataKey="portfolioValue"
            stroke="#f2b74a"
            strokeWidth={2}
            fill="url(#portfolioFill)"
          />
          <Line
            type="monotone"
            dataKey="benchmarkValue"
            stroke="#626b7e"
            strokeWidth={1.5}
            strokeDasharray="4 3"
            dot={false}
          />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  );
}
