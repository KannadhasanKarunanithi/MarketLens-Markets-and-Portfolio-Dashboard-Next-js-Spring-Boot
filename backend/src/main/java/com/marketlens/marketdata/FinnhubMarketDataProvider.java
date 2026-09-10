package com.marketlens.marketdata;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import com.marketlens.instrument.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * A real market data provider that reads from Finnhub. It is only selected when
 * {@code marketlens.marketdata.provider} is set to {@code finnhub} and an API key
 * is configured. Without a key the app runs on the simulator, so this class is
 * never exercised by the tests.
 */
@Component
public class FinnhubMarketDataProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(FinnhubMarketDataProvider.class);

    private final MarketDataProperties.Finnhub config;
    private final RestClient restClient;
    private final SimulatedMarketDataProvider fallback;

    public FinnhubMarketDataProvider(MarketDataProperties properties,
            SimulatedMarketDataProvider fallback) {
        this.config = properties.getFinnhub();
        this.fallback = fallback;
        this.restClient = RestClient.builder().baseUrl(config.getBaseUrl()).build();
    }

    @Override
    public String name() {
        return "finnhub";
    }

    @Override
    public List<GeneratedBar> backfill(Instrument instrument, int days) {
        if (config.getApiKey().isBlank()) {
            return fallback.backfill(instrument, days);
        }
        try {
            long to = Instant.now().getEpochSecond();
            long from = to - (long) days * 2 * 24 * 3600;
            Candles candles = restClient.get()
                    .uri(uri -> uri.path("/stock/candle")
                            .queryParam("symbol", symbol(instrument))
                            .queryParam("resolution", "D")
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .queryParam("token", config.getApiKey())
                            .build())
                    .retrieve()
                    .body(Candles.class);
            if (candles == null || !"ok".equals(candles.s()) || candles.t() == null) {
                return fallback.backfill(instrument, days);
            }
            List<GeneratedBar> bars = new ArrayList<>();
            for (int i = 0; i < candles.t().size(); i++) {
                bars.add(new GeneratedBar(
                        LocalDate.ofInstant(Instant.ofEpochSecond(candles.t().get(i)), ZoneOffset.UTC),
                        BigDecimal.valueOf(candles.o().get(i)),
                        BigDecimal.valueOf(candles.h().get(i)),
                        BigDecimal.valueOf(candles.l().get(i)),
                        BigDecimal.valueOf(candles.c().get(i)),
                        candles.v() == null ? 0L : candles.v().get(i).longValue()));
            }
            return bars;
        } catch (RuntimeException ex) {
            log.warn("Finnhub backfill failed for {}, using the simulator: {}",
                    instrument.getSymbol(), ex.getMessage());
            return fallback.backfill(instrument, days);
        }
    }

    @Override
    public BigDecimal nextTick(Instrument instrument, BigDecimal currentPrice) {
        if (config.getApiKey().isBlank()) {
            return fallback.nextTick(instrument, currentPrice);
        }
        try {
            Snapshot snapshot = restClient.get()
                    .uri(uri -> uri.path("/quote")
                            .queryParam("symbol", symbol(instrument))
                            .queryParam("token", config.getApiKey())
                            .build())
                    .retrieve()
                    .body(Snapshot.class);
            if (snapshot != null && snapshot.c() != null && snapshot.c() > 0) {
                return BigDecimal.valueOf(snapshot.c());
            }
        } catch (RuntimeException ex) {
            log.debug("Finnhub quote failed for {}: {}", instrument.getSymbol(), ex.getMessage());
        }
        return fallback.nextTick(instrument, currentPrice);
    }

    private String symbol(Instrument instrument) {
        return instrument.getSymbol() + config.getSymbolSuffix();
    }

    private record Candles(String s, List<Long> t, List<Double> o, List<Double> h, List<Double> l,
            List<Double> c, List<Double> v) {
    }

    private record Snapshot(Double c, Double d, Double dp, Double h, Double l, Double o, Double pc) {
    }
}
