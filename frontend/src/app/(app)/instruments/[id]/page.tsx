"use client";

import dynamic from "next/dynamic";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { useQuoteStream } from "@/lib/useQuoteStream";
import type { CandleResponse, Instrument, Quote } from "@/lib/types";
import { formatCurrency, formatPercent, trendClass } from "@/lib/format";
import {
  Card,
  CardBody,
  CardHeader,
  Spinner,
  StatCard,
  cx,
} from "@/components/ui";

const PriceChart = dynamic(() => import("@/components/PriceChart").then((m) => m.PriceChart), {
  ssr: false,
  loading: () => <div className="grid h-[360px] place-items-center"><Spinner label="Drawing chart" /></div>,
});

const RANGES = ["5d", "1m", "3m", "6m", "1y", "max"] as const;

export default function InstrumentDetailPage() {
  const params = useParams<{ id: string }>();
  const id = params.id;
  const [range, setRange] = useState<(typeof RANGES)[number]>("6m");
  const [mode, setMode] = useState<"candles" | "area">("candles");
  const live = useQuoteStream();

  const instrument = useQuery({
    queryKey: ["instrument", id],
    queryFn: () => api.get<Instrument>(`/instruments/${id}`),
  });
  const quote = useQuery({
    queryKey: ["quote", id],
    queryFn: () => api.get<Quote[]>(`/quotes`).then((all) => all.find((q) => q.instrumentId === id) ?? null),
  });
  const candles = useQuery({
    queryKey: ["candles", id, range],
    queryFn: () => api.get<CandleResponse>(`/instruments/${id}/candles?range=${range}`),
  });

  const liveQuote = instrument.data ? live[instrument.data.symbol] : undefined;
  const price = liveQuote?.lastPrice ?? quote.data?.lastPrice ?? instrument.data?.referencePrice ?? 0;
  const changePct = liveQuote?.changePct ?? quote.data?.changePct ?? 0;
  const changeAbs = liveQuote?.changeAbs ?? quote.data?.changeAbs ?? 0;

  if (instrument.isLoading) return <Spinner label="Loading instrument" />;
  if (!instrument.data) return <p className="text-sm text-ink-dim">Instrument not found.</p>;

  const it = instrument.data;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="text-xs text-ink-dim">
            {it.exchange} · {it.sector} · {it.assetClass}
          </p>
          <h1 className="text-2xl font-semibold text-ink">{it.symbol}</h1>
          <p className="text-sm text-ink-dim">{it.name}</p>
        </div>
        <div className="text-right">
          <p className="text-2xl font-semibold tabular-nums text-ink">{formatCurrency(price)}</p>
          <p className={cx("text-sm tabular-nums", trendClass(changePct))}>
            {formatCurrency(changeAbs)} ({formatPercent(changePct)})
          </p>
        </div>
      </div>

      <Card>
        <CardHeader
          title="Price"
          action={
            <div className="flex gap-1">
              {(["candles", "area"] as const).map((m) => (
                <button
                  key={m}
                  onClick={() => setMode(m)}
                  className={cx(
                    "rounded-md px-2 py-1 text-xs font-medium capitalize",
                    mode === m ? "bg-panel-2 text-ink" : "text-ink-dim hover:bg-panel-2",
                  )}
                >
                  {m}
                </button>
              ))}
            </div>
          }
        />
        <CardBody>
          <div className="mb-3 flex gap-1">
            {RANGES.map((r) => (
              <button
                key={r}
                onClick={() => setRange(r)}
                className={cx(
                  "rounded-md px-2 py-1 text-xs font-medium uppercase",
                  range === r ? "bg-gold text-base" : "text-ink-dim hover:bg-panel-2",
                )}
              >
                {r}
              </button>
            ))}
          </div>
          {candles.isLoading ? (
            <div className="grid h-[360px] place-items-center">
              <Spinner />
            </div>
          ) : (
            <PriceChart candles={candles.data?.candles ?? []} mode={mode} />
          )}
        </CardBody>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Day open" value={formatCurrency(quote.data?.dayOpen)} />
        <StatCard label="Day high" value={formatCurrency(quote.data?.dayHigh)} />
        <StatCard label="Day low" value={formatCurrency(quote.data?.dayLow)} />
        <StatCard label="Previous close" value={formatCurrency(quote.data?.prevClose)} />
      </div>

      <p className="text-sm text-ink-dim">
        Manage this instrument from{" "}
        <Link href="/markets" className="text-gold hover:underline">
          Markets
        </Link>{" "}
        or set a price alert from{" "}
        <Link href="/alerts" className="text-gold hover:underline">
          Alerts
        </Link>
        .
      </p>
    </div>
  );
}
