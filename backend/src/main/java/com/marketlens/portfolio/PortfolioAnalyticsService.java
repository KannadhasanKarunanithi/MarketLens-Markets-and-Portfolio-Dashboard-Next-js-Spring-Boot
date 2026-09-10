package com.marketlens.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marketlens.instrument.AssetClass;
import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentRepository;
import com.marketlens.pricebar.ChartRange;
import com.marketlens.pricebar.PriceBar;
import com.marketlens.pricebar.PriceBarRepository;
import com.marketlens.portfolio.dto.PortfolioDtos.AllocationResponse;
import com.marketlens.portfolio.dto.PortfolioDtos.AllocationSlice;
import com.marketlens.portfolio.dto.PortfolioDtos.HoldingView;
import com.marketlens.portfolio.dto.PortfolioDtos.PerformancePoint;
import com.marketlens.portfolio.dto.PortfolioDtos.PerformanceResponse;
import com.marketlens.transaction.Transaction;
import com.marketlens.transaction.TransactionRepository;
import com.marketlens.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PortfolioAnalyticsService {

    private static final String BENCHMARK_SYMBOL = "NIFTY50";

    private final PortfolioService portfolioService;
    private final TransactionRepository transactions;
    private final InstrumentRepository instruments;
    private final PriceBarRepository priceBars;

    public PortfolioAnalyticsService(PortfolioService portfolioService, TransactionRepository transactions,
            InstrumentRepository instruments, PriceBarRepository priceBars) {
        this.portfolioService = portfolioService;
        this.transactions = transactions;
        this.instruments = instruments;
        this.priceBars = priceBars;
    }

    public AllocationResponse allocation(UUID userId) {
        List<HoldingView> holdings = portfolioService.portfolio(userId).holdings();
        BigDecimal total = holdings.stream().map(HoldingView::marketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new AllocationResponse(
                slices(holdings, HoldingView::assetClass, total),
                slices(holdings, HoldingView::sector, total));
    }

    private List<AllocationSlice> slices(List<HoldingView> holdings,
            Function<HoldingView, String> classifier, BigDecimal total) {
        Map<String, BigDecimal> grouped = new TreeMap<>();
        for (HoldingView holding : holdings) {
            grouped.merge(classifier.apply(holding), holding.marketValue(), BigDecimal::add);
        }
        return grouped.entrySet().stream()
                .map(e -> new AllocationSlice(
                        e.getKey(),
                        e.getValue().setScale(2, RoundingMode.HALF_UP),
                        total.signum() == 0 ? BigDecimal.ZERO
                                : e.getValue().multiply(BigDecimal.valueOf(100))
                                        .divide(total, 2, RoundingMode.HALF_UP)))
                .sorted(Comparator.comparing(AllocationSlice::value).reversed())
                .toList();
    }

    public PerformanceResponse performance(UUID userId, String rangeValue) {
        ChartRange range = ChartRange.parse(rangeValue == null ? "1y" : rangeValue);
        LocalDate from = range.from(LocalDate.now());

        List<Transaction> ledger = transactions.findByUserIdOrderByTradedOnAscCreatedAtAsc(userId);
        if (ledger.isEmpty()) {
            return new PerformanceResponse(List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        Map<UUID, Instrument> instrumentById = instruments.findAll().stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        UUID benchmarkId = instruments.findBySymbol(BENCHMARK_SYMBOL).map(Instrument::getId).orElse(null);

        // close price by instrument by date
        Map<UUID, TreeMap<LocalDate, BigDecimal>> closes = new java.util.HashMap<>();
        for (PriceBar bar : priceBars.findAllSince(from.minusDays(5))) {
            closes.computeIfAbsent(bar.getInstrumentId(), k -> new TreeMap<>())
                    .put(bar.getDate(), bar.getClose());
        }

        TreeMap<LocalDate, BigDecimal> benchmarkCloses = benchmarkId == null
                ? new TreeMap<>()
                : closes.getOrDefault(benchmarkId, new TreeMap<>());
        List<LocalDate> calendar = benchmarkCloses.isEmpty()
                ? closes.values().stream().flatMap(m -> m.keySet().stream()).distinct().sorted().toList()
                : new ArrayList<>(benchmarkCloses.keySet());
        calendar = calendar.stream().filter(d -> !d.isBefore(from)).toList();

        List<PerformancePoint> series = new ArrayList<>();
        BigDecimal firstPortfolioValue = null;
        BigDecimal firstBenchmarkClose = null;

        for (LocalDate date : calendar) {
            Map<UUID, BigDecimal> heldByInstrument = quantitiesAsOf(ledger, date);
            BigDecimal value = BigDecimal.ZERO;
            for (Map.Entry<UUID, BigDecimal> entry : heldByInstrument.entrySet()) {
                if (entry.getValue().signum() <= 0 || !instrumentById.containsKey(entry.getKey())) {
                    continue;
                }
                BigDecimal close = priceOnOrBefore(closes.get(entry.getKey()), date);
                if (close != null) {
                    value = value.add(entry.getValue().multiply(close));
                }
            }
            value = value.setScale(2, RoundingMode.HALF_UP);
            if (value.signum() == 0 && firstPortfolioValue == null) {
                continue;
            }
            if (firstPortfolioValue == null) {
                firstPortfolioValue = value;
                firstBenchmarkClose = priceOnOrBefore(benchmarkCloses, date);
            }
            BigDecimal benchmarkValue = BigDecimal.ZERO;
            BigDecimal benchClose = priceOnOrBefore(benchmarkCloses, date);
            if (benchClose != null && firstBenchmarkClose != null && firstBenchmarkClose.signum() != 0) {
                benchmarkValue = firstPortfolioValue.multiply(benchClose)
                        .divide(firstBenchmarkClose, 2, RoundingMode.HALF_UP);
            }
            series.add(new PerformancePoint(date, value, benchmarkValue));
        }

        BigDecimal absoluteReturn = pctChange(series, PerformancePoint::portfolioValue);
        BigDecimal benchmarkReturn = pctChange(series, PerformancePoint::benchmarkValue);
        BigDecimal xirr = BigDecimal.valueOf(computeXirr(ledger, series))
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

        return new PerformanceResponse(series, absoluteReturn, benchmarkReturn, xirr);
    }

    private Map<UUID, BigDecimal> quantitiesAsOf(List<Transaction> ledger, LocalDate date) {
        Map<UUID, BigDecimal> held = new java.util.HashMap<>();
        for (Transaction tx : ledger) {
            if (tx.getTradedOn().isAfter(date)) {
                break;
            }
            if (tx.getType() == TransactionType.BUY) {
                held.merge(tx.getInstrumentId(), tx.getQuantity(), BigDecimal::add);
            } else if (tx.getType() == TransactionType.SELL) {
                held.merge(tx.getInstrumentId(), tx.getQuantity().negate(), BigDecimal::add);
            }
        }
        return held;
    }

    private BigDecimal priceOnOrBefore(TreeMap<LocalDate, BigDecimal> byDate, LocalDate date) {
        if (byDate == null || byDate.isEmpty()) {
            return null;
        }
        Map.Entry<LocalDate, BigDecimal> entry = byDate.floorEntry(date);
        return entry == null ? byDate.firstEntry().getValue() : entry.getValue();
    }

    private BigDecimal pctChange(List<PerformancePoint> series, Function<PerformancePoint, BigDecimal> field) {
        if (series.size() < 2) {
            return BigDecimal.ZERO;
        }
        BigDecimal start = field.apply(series.get(0));
        BigDecimal end = field.apply(series.get(series.size() - 1));
        if (start.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return end.subtract(start).multiply(BigDecimal.valueOf(100))
                .divide(start, 2, RoundingMode.HALF_UP);
    }

    private double computeXirr(List<Transaction> ledger, List<PerformancePoint> series) {
        if (series.isEmpty()) {
            return 0;
        }
        List<Xirr.CashFlow> flows = new ArrayList<>();
        LocalDate seriesStart = series.get(0).date();
        for (Transaction tx : ledger) {
            LocalDate date = tx.getTradedOn().isBefore(seriesStart) ? seriesStart : tx.getTradedOn();
            double amount = switch (tx.getType()) {
                case BUY -> -(tx.grossValue().doubleValue() + tx.getFees().doubleValue());
                case SELL -> tx.grossValue().doubleValue() - tx.getFees().doubleValue();
                case DIVIDEND -> tx.getQuantity().multiply(tx.getPrice()).doubleValue();
            };
            flows.add(new Xirr.CashFlow(date, amount));
        }
        PerformancePoint last = series.get(series.size() - 1);
        flows.add(new Xirr.CashFlow(last.date(), last.portfolioValue().doubleValue()));
        flows.sort(Comparator.comparing(Xirr.CashFlow::date));
        return Xirr.annualRate(flows);
    }
}
