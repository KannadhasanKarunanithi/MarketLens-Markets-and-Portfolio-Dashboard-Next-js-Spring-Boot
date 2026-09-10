package com.marketlens.marketdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.marketlens.instrument.Instrument;
import org.springframework.stereotype.Component;

/**
 * Generates price history and ticks with a simple geometric random walk. The
 * backfill is seeded by the instrument symbol so a given symbol always produces
 * the same history on a fresh database.
 */
@Component
public class SimulatedMarketDataProvider implements MarketDataProvider {

    private static final double DAILY_DRIFT = 0.0004;
    private static final double DAILY_VOLATILITY = 0.016;

    @Override
    public String name() {
        return "simulated";
    }

    @Override
    public List<GeneratedBar> backfill(Instrument instrument, int days) {
        Random random = new Random(instrument.getSymbol().hashCode() * 2654435761L);
        BigDecimal target = instrument.getReferencePrice();

        // Walk backwards from the reference price to a start price, then forward
        // again so the final close lands close to the reference price.
        double[] returns = new double[days];
        double cumulative = 0;
        for (int i = 0; i < days; i++) {
            double r = DAILY_DRIFT + DAILY_VOLATILITY * random.nextGaussian();
            returns[i] = r;
            cumulative += r;
        }
        double startPrice = target.doubleValue() / Math.exp(cumulative);

        List<GeneratedBar> bars = new ArrayList<>(days);
        double price = startPrice;
        LocalDate date = tradingDayOffset(LocalDate.now(), -days);
        long baseVolume = Math.max(50_000, (long) (5_000_000 / Math.max(1, target.doubleValue() / 100)));

        for (int i = 0; i < days; i++) {
            double open = price;
            price = open * Math.exp(returns[i]);
            double swing = Math.abs(returns[i]) + 0.004;
            double high = Math.max(open, price) * (1 + swing * random.nextDouble());
            double low = Math.min(open, price) * (1 - swing * random.nextDouble());
            long volume = (long) (baseVolume * (0.6 + random.nextDouble()));

            bars.add(new GeneratedBar(
                    date,
                    round(open),
                    round(high),
                    round(low),
                    round(price),
                    volume));
            date = nextTradingDay(date);
        }
        return bars;
    }

    @Override
    public BigDecimal nextTick(Instrument instrument, BigDecimal currentPrice) {
        Random random = new Random();
        double move = (DAILY_DRIFT / 26) + (DAILY_VOLATILITY / 5) * random.nextGaussian();
        double next = currentPrice.doubleValue() * Math.exp(move);
        return round(Math.max(next, 0.5));
    }

    private static LocalDate tradingDayOffset(LocalDate from, int businessDays) {
        LocalDate date = from;
        int step = businessDays < 0 ? -1 : 1;
        int remaining = Math.abs(businessDays);
        while (remaining > 0) {
            date = date.plusDays(step);
            if (isTradingDay(date)) {
                remaining--;
            }
        }
        return date;
    }

    private static LocalDate nextTradingDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (!isTradingDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    private static boolean isTradingDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> false;
            default -> true;
        };
    }

    private static BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
}
