"use client";

import {
  AreaSeries,
  CandlestickSeries,
  createChart,
  type IChartApi,
  type ISeriesApi,
  type Time,
} from "lightweight-charts";
import { useEffect, useRef } from "react";
import type { Candle } from "@/lib/types";

type Mode = "candles" | "area";

export function PriceChart({ candles, mode }: { candles: Candle[]; mode: Mode }) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<"Candlestick"> | ISeriesApi<"Area"> | null>(null);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const chart = createChart(container, {
      autoSize: true,
      layout: {
        background: { color: "transparent" },
        textColor: "#8b93a7",
        fontFamily: "var(--font-mono-data), monospace",
      },
      grid: {
        vertLines: { color: "#1a2230" },
        horzLines: { color: "#1a2230" },
      },
      rightPriceScale: { borderColor: "#262d3b" },
      timeScale: { borderColor: "#262d3b" },
      crosshair: { mode: 1 },
    });
    chartRef.current = chart;

    return () => {
      chart.remove();
      chartRef.current = null;
      seriesRef.current = null;
    };
  }, []);

  useEffect(() => {
    const chart = chartRef.current;
    if (!chart) return;

    if (seriesRef.current) {
      chart.removeSeries(seriesRef.current);
      seriesRef.current = null;
    }

    if (mode === "candles") {
      const series = chart.addSeries(CandlestickSeries, {
        upColor: "#35c65a",
        downColor: "#f5564a",
        wickUpColor: "#35c65a",
        wickDownColor: "#f5564a",
        borderVisible: false,
      });
      series.setData(
        candles.map((c) => ({
          time: c.date as Time,
          open: c.open,
          high: c.high,
          low: c.low,
          close: c.close,
        })),
      );
      seriesRef.current = series;
    } else {
      const series = chart.addSeries(AreaSeries, {
        lineColor: "#f2b74a",
        topColor: "rgba(242, 183, 74, 0.22)",
        bottomColor: "rgba(242, 183, 74, 0.02)",
        lineWidth: 2,
      });
      series.setData(candles.map((c) => ({ time: c.date as Time, value: c.close })));
      seriesRef.current = series;
    }

    chart.timeScale().fitContent();
  }, [candles, mode]);

  return <div ref={containerRef} className="h-[360px] w-full" />;
}
