"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import type { Portfolio, Quote } from "@/lib/types";
import {
  formatCurrency,
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
} from "@/components/ui";

export default function DashboardPage() {
  const { user } = useAuth();
  const portfolio = useQuery({
    queryKey: ["portfolio"],
    queryFn: () => api.get<Portfolio>("/portfolio"),
  });
  const quotes = useQuery({
    queryKey: ["quotes", "all"],
    queryFn: () => api.get<Quote[]>("/quotes"),
    refetchInterval: 20_000,
  });

  const movers = [...(quotes.data ?? [])].sort((a, b) => b.changePct - a.changePct);
  const gainers = movers.slice(0, 5);
  const losers = movers.slice(-5).reverse();
  const summary = portfolio.data?.summary;

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Welcome back, ${user?.displayName?.split(" ")[0] ?? ""}`}
        subtitle="Your portfolio and the market at a glance"
      />

      {portfolio.isLoading ? (
        <Spinner label="Loading your portfolio" />
      ) : summary && summary.holdingsCount > 0 ? (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <StatCard
            label="Portfolio value"
            value={formatCurrency(summary.marketValue)}
            hint={`Invested ${formatCurrency(summary.investedValue)}`}
          />
          <StatCard
            label="Unrealised P and L"
            value={formatSignedCurrency(summary.unrealisedPnl)}
            hint={formatPercent(summary.unrealisedPnlPct)}
            tone={summary.unrealisedPnl >= 0 ? "up" : "down"}
          />
          <StatCard
            label="Day change"
            value={formatSignedCurrency(summary.dayChange)}
            hint={formatPercent(summary.dayChangePct)}
            tone={summary.dayChange >= 0 ? "up" : "down"}
          />
          <StatCard
            label="Realised and income"
            value={formatSignedCurrency(summary.realisedPnl)}
            hint={`Dividends ${formatCurrency(summary.dividendIncome)}`}
            tone={summary.realisedPnl >= 0 ? "up" : "down"}
          />
        </div>
      ) : (
        <EmptyState
          title="No holdings yet"
          hint="Add a few transactions to build your portfolio."
        />
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <MoversCard title="Top gainers" rows={gainers} loading={quotes.isLoading} />
        <MoversCard title="Top losers" rows={losers} loading={quotes.isLoading} />
      </div>

      <Card>
        <CardHeader title="Jump back in" />
        <CardBody className="flex flex-wrap gap-3 text-sm">
          <Link href="/portfolio" className="rounded border border-line px-3 py-2 hover:bg-panel-2">
            View portfolio analytics
          </Link>
          <Link href="/markets" className="rounded border border-line px-3 py-2 hover:bg-panel-2">
            Open your watchlists
          </Link>
          <Link href="/transactions" className="rounded border border-line px-3 py-2 hover:bg-panel-2">
            Record a trade
          </Link>
        </CardBody>
      </Card>
    </div>
  );
}

function MoversCard({
  title,
  rows,
  loading,
}: {
  title: string;
  rows: Quote[];
  loading: boolean;
}) {
  return (
    <Card>
      <CardHeader title={title} subtitle="By day change" />
      <CardBody className="p-0">
        {loading ? (
          <div className="px-5 py-4">
            <Spinner />
          </div>
        ) : (
          <ul className="divide-y divide-line-soft">
            {rows.map((quote) => (
              <li key={quote.instrumentId}>
                <Link
                  href={`/instruments/${quote.instrumentId}`}
                  className="flex items-center justify-between px-5 py-3 text-sm hover:bg-panel-2"
                >
                  <div>
                    <p className="font-medium text-ink">{quote.symbol}</p>
                    <p className="text-xs text-ink-dim">{quote.name}</p>
                  </div>
                  <div className="text-right tabular-nums">
                    <p className="font-medium text-ink">{formatCurrency(quote.lastPrice)}</p>
                    <p className={`text-xs ${trendClass(quote.changePct)}`}>
                      {formatPercent(quote.changePct)}
                    </p>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </CardBody>
    </Card>
  );
}
