"use client";

import dynamic from "next/dynamic";
import Link from "next/link";
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Allocation, Performance, Portfolio } from "@/lib/types";
import {
  formatCurrency,
  formatNumber,
  formatPercent,
  formatSignedCurrency,
  trendClass,
} from "@/lib/format";
import {
  Card,
  CardBody,
  CardHeader,
  EmptyState,
  PageHeader,
  Spinner,
  StatCard,
  cx,
} from "@/components/ui";

const AllocationDonut = dynamic(
  () => import("@/components/charts").then((m) => m.AllocationDonut),
  { ssr: false, loading: () => <Spinner /> },
);
const PerformanceChart = dynamic(
  () => import("@/components/charts").then((m) => m.PerformanceChart),
  { ssr: false, loading: () => <Spinner /> },
);

const RANGES = ["3m", "6m", "1y", "max"] as const;

export default function PortfolioPage() {
  const [range, setRange] = useState<(typeof RANGES)[number]>("1y");

  const portfolio = useQuery({
    queryKey: ["portfolio"],
    queryFn: () => api.get<Portfolio>("/portfolio"),
  });
  const allocation = useQuery({
    queryKey: ["allocation"],
    queryFn: () => api.get<Allocation>("/portfolio/allocation"),
  });
  const performance = useQuery({
    queryKey: ["performance", range],
    queryFn: () => api.get<Performance>(`/portfolio/performance?range=${range}`),
  });

  const summary = portfolio.data?.summary;
  const holdings = portfolio.data?.holdings ?? [];

  if (portfolio.isLoading) return <Spinner label="Loading portfolio" />;

  if (!summary || summary.holdingsCount === 0) {
    return (
      <div className="space-y-6">
        <PageHeader title="Portfolio" subtitle="Holdings, allocation and returns" />
        <EmptyState
          title="Your portfolio is empty"
          hint="Record buy transactions and they will show up here with live valuations."
        />
        <Link href="/transactions" className="text-sm text-blue-600 hover:underline">
          Go to transactions
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader title="Portfolio" subtitle="Holdings, allocation and returns" />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Market value" value={formatCurrency(summary.marketValue)} />
        <StatCard
          label="Unrealised P and L"
          value={formatSignedCurrency(summary.unrealisedPnl)}
          hint={formatPercent(summary.unrealisedPnlPct)}
          tone={summary.unrealisedPnl >= 0 ? "up" : "down"}
        />
        <StatCard
          label="Realised and dividends"
          value={formatSignedCurrency(summary.realisedPnl)}
          hint={`Dividends ${formatCurrency(summary.dividendIncome)}`}
          tone={summary.realisedPnl >= 0 ? "up" : "down"}
        />
        <StatCard
          label="Money weighted return"
          value={performance.data ? formatPercent(performance.data.xirrPct) : "-"}
          hint="XIRR, annualised"
          tone={(performance.data?.xirrPct ?? 0) >= 0 ? "up" : "down"}
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader title="Allocation by sector" />
          <CardBody>
            {allocation.isLoading ? <Spinner /> : <AllocationDonut slices={allocation.data?.bySector ?? []} />}
          </CardBody>
        </Card>
        <Card>
          <CardHeader
            title="Performance vs Nifty 50"
            subtitle={
              performance.data
                ? `Portfolio ${formatPercent(performance.data.absoluteReturnPct)} · Benchmark ${formatPercent(
                    performance.data.benchmarkReturnPct,
                  )}`
                : undefined
            }
            action={
              <div className="flex gap-1">
                {RANGES.map((r) => (
                  <button
                    key={r}
                    onClick={() => setRange(r)}
                    className={cx(
                      "rounded-md px-2 py-1 text-xs font-medium uppercase",
                      range === r ? "bg-blue-600 text-white" : "text-slate-600 hover:bg-slate-100",
                    )}
                  >
                    {r}
                  </button>
                ))}
              </div>
            }
          />
          <CardBody>
            {performance.isLoading ? (
              <Spinner />
            ) : (
              <PerformanceChart series={performance.data?.series ?? []} />
            )}
          </CardBody>
        </Card>
      </div>

      <Card>
        <CardHeader title="Holdings" subtitle={`${holdings.length} positions`} />
        <CardBody className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b border-slate-100 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-5 py-2 font-medium">Instrument</th>
                  <th className="px-5 py-2 text-right font-medium">Qty</th>
                  <th className="px-5 py-2 text-right font-medium">Avg cost</th>
                  <th className="px-5 py-2 text-right font-medium">Last</th>
                  <th className="px-5 py-2 text-right font-medium">Value</th>
                  <th className="px-5 py-2 text-right font-medium">P and L</th>
                  <th className="px-5 py-2 text-right font-medium">Weight</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {holdings.map((h) => (
                  <tr key={h.instrumentId} className="hover:bg-slate-50">
                    <td className="px-5 py-3">
                      <Link
                        href={`/instruments/${h.instrumentId}`}
                        className="font-medium text-slate-900 hover:text-blue-600"
                      >
                        {h.symbol}
                      </Link>
                      <p className="text-xs text-slate-500">{h.sector}</p>
                    </td>
                    <td className="px-5 py-3 text-right tabular-nums">{formatNumber(h.quantity)}</td>
                    <td className="px-5 py-3 text-right tabular-nums">{formatCurrency(h.averageCost)}</td>
                    <td className="px-5 py-3 text-right tabular-nums">{formatCurrency(h.lastPrice)}</td>
                    <td className="px-5 py-3 text-right tabular-nums">{formatCurrency(h.marketValue)}</td>
                    <td className={cx("px-5 py-3 text-right tabular-nums", trendClass(h.unrealisedPnl))}>
                      {formatSignedCurrency(h.unrealisedPnl)}
                      <span className="block text-xs">{formatPercent(h.unrealisedPnlPct)}</span>
                    </td>
                    <td className="px-5 py-3 text-right tabular-nums text-slate-500">
                      {formatPercent(h.portfolioWeight).replace("+", "")}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardBody>
      </Card>
    </div>
  );
}
