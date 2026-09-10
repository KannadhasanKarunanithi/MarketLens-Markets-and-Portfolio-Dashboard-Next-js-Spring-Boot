package com.marketlens.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentRepository;
import com.marketlens.portfolio.HoldingCalculator.Position;
import com.marketlens.portfolio.dto.PortfolioDtos.HoldingView;
import com.marketlens.portfolio.dto.PortfolioDtos.PortfolioResponse;
import com.marketlens.portfolio.dto.PortfolioDtos.PortfolioSummary;
import com.marketlens.quote.Quote;
import com.marketlens.quote.QuoteRepository;
import com.marketlens.transaction.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PortfolioService {

    private final TransactionRepository transactions;
    private final InstrumentRepository instruments;
    private final QuoteRepository quotes;

    public PortfolioService(TransactionRepository transactions, InstrumentRepository instruments,
            QuoteRepository quotes) {
        this.transactions = transactions;
        this.instruments = instruments;
        this.quotes = quotes;
    }

    public PortfolioResponse portfolio(UUID userId) {
        Map<UUID, Position> positions = HoldingCalculator.positions(
                transactions.findByUserIdOrderByTradedOnAscCreatedAtAsc(userId));
        Map<UUID, Instrument> instrumentById = instruments.findAll().stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        Map<UUID, Quote> quoteById = quotes.findAll().stream()
                .collect(Collectors.toMap(Quote::getInstrumentId, Function.identity()));

        BigDecimal totalMarketValue = BigDecimal.ZERO;
        for (Map.Entry<UUID, Position> entry : positions.entrySet()) {
            if (!entry.getValue().isOpen()) {
                continue;
            }
            Quote quote = quoteById.get(entry.getKey());
            BigDecimal price = quote == null ? BigDecimal.ZERO : quote.getLastPrice();
            totalMarketValue = totalMarketValue.add(entry.getValue().quantity().multiply(price));
        }

        List<HoldingView> holdings = new ArrayList<>();
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnrealised = BigDecimal.ZERO;
        BigDecimal totalRealised = BigDecimal.ZERO;
        BigDecimal totalDividends = BigDecimal.ZERO;
        BigDecimal totalDayChange = BigDecimal.ZERO;

        for (Map.Entry<UUID, Position> entry : positions.entrySet()) {
            Position position = entry.getValue();
            totalRealised = totalRealised.add(position.realisedPnl());
            totalDividends = totalDividends.add(position.dividendIncome());
            if (!position.isOpen()) {
                continue;
            }
            Instrument instrument = instrumentById.get(entry.getKey());
            Quote quote = quoteById.get(entry.getKey());
            BigDecimal price = quote == null ? position.averageCost() : quote.getLastPrice();
            BigDecimal quantity = position.quantity();
            BigDecimal invested = position.investedValue().setScale(2, RoundingMode.HALF_UP);
            BigDecimal marketValue = quantity.multiply(price).setScale(2, RoundingMode.HALF_UP);
            BigDecimal unrealised = marketValue.subtract(invested);
            BigDecimal unrealisedPct = invested.signum() == 0 ? BigDecimal.ZERO
                    : unrealised.multiply(BigDecimal.valueOf(100)).divide(invested, 2, RoundingMode.HALF_UP);
            BigDecimal dayChange = quote == null ? BigDecimal.ZERO
                    : quantity.multiply(quote.getChangeAbs()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal weight = totalMarketValue.signum() == 0 ? BigDecimal.ZERO
                    : marketValue.multiply(BigDecimal.valueOf(100))
                            .divide(totalMarketValue, 2, RoundingMode.HALF_UP);

            totalInvested = totalInvested.add(invested);
            totalUnrealised = totalUnrealised.add(unrealised);
            totalDayChange = totalDayChange.add(dayChange);

            holdings.add(new HoldingView(
                    entry.getKey(),
                    instrument == null ? "?" : instrument.getSymbol(),
                    instrument == null ? "Unknown" : instrument.getName(),
                    instrument == null ? "" : instrument.getSector(),
                    instrument == null ? "" : instrument.getAssetClass().name(),
                    quantity,
                    position.averageCost(),
                    invested,
                    price,
                    marketValue,
                    unrealised,
                    unrealisedPct,
                    dayChange,
                    weight));
        }

        holdings.sort((a, b) -> b.marketValue().compareTo(a.marketValue()));

        BigDecimal marketValue = totalMarketValue.setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealisedPct = totalInvested.signum() == 0 ? BigDecimal.ZERO
                : totalUnrealised.multiply(BigDecimal.valueOf(100))
                        .divide(totalInvested, 2, RoundingMode.HALF_UP);
        BigDecimal prevValue = marketValue.subtract(totalDayChange);
        BigDecimal dayChangePct = prevValue.signum() == 0 ? BigDecimal.ZERO
                : totalDayChange.multiply(BigDecimal.valueOf(100)).divide(prevValue, 2, RoundingMode.HALF_UP);

        PortfolioSummary summary = new PortfolioSummary(
                totalInvested.setScale(2, RoundingMode.HALF_UP),
                marketValue,
                totalUnrealised.setScale(2, RoundingMode.HALF_UP),
                unrealisedPct,
                totalRealised.setScale(2, RoundingMode.HALF_UP),
                totalDividends.setScale(2, RoundingMode.HALF_UP),
                totalDayChange.setScale(2, RoundingMode.HALF_UP),
                dayChangePct,
                holdings.size());

        return new PortfolioResponse(summary, holdings);
    }
}
