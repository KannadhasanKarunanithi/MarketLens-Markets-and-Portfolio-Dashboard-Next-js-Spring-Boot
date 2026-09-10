package com.marketlens.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class PortfolioDtos {

    private PortfolioDtos() {
    }

    public record HoldingView(
            UUID instrumentId,
            String symbol,
            String name,
            String sector,
            String assetClass,
            BigDecimal quantity,
            BigDecimal averageCost,
            BigDecimal investedValue,
            BigDecimal lastPrice,
            BigDecimal marketValue,
            BigDecimal unrealisedPnl,
            BigDecimal unrealisedPnlPct,
            BigDecimal dayChange,
            BigDecimal portfolioWeight) {
    }

    public record PortfolioSummary(
            BigDecimal investedValue,
            BigDecimal marketValue,
            BigDecimal unrealisedPnl,
            BigDecimal unrealisedPnlPct,
            BigDecimal realisedPnl,
            BigDecimal dividendIncome,
            BigDecimal dayChange,
            BigDecimal dayChangePct,
            int holdingsCount) {
    }

    public record PortfolioResponse(PortfolioSummary summary, List<HoldingView> holdings) {
    }

    public record AllocationSlice(String label, BigDecimal value, BigDecimal weight) {
    }

    public record AllocationResponse(
            List<AllocationSlice> byAssetClass,
            List<AllocationSlice> bySector) {
    }

    public record PerformancePoint(LocalDate date, BigDecimal portfolioValue, BigDecimal benchmarkValue) {
    }

    public record PerformanceResponse(
            List<PerformancePoint> series,
            BigDecimal absoluteReturnPct,
            BigDecimal benchmarkReturnPct,
            BigDecimal xirrPct) {
    }
}
