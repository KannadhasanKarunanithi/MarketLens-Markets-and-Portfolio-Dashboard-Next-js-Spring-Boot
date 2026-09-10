package com.marketlens.marketdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentRepository;
import com.marketlens.pricebar.PriceBar;
import com.marketlens.pricebar.PriceBarRepository;
import com.marketlens.quote.Quote;
import com.marketlens.quote.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Moves every instrument price a small step on a fixed interval, updates its
 * quote and keeps the current day price bar in step.
 */
@Component
public class PriceTickJob {

    private static final Logger log = LoggerFactory.getLogger(PriceTickJob.class);

    private final InstrumentRepository instruments;
    private final QuoteRepository quotes;
    private final PriceBarRepository priceBars;
    private final MarketDataProviders providers;

    public PriceTickJob(InstrumentRepository instruments, QuoteRepository quotes,
            PriceBarRepository priceBars, MarketDataProviders providers) {
        this.instruments = instruments;
        this.quotes = quotes;
        this.priceBars = priceBars;
        this.providers = providers;
    }

    @Scheduled(fixedDelayString = "${marketlens.marketdata.tick-interval}", initialDelay = 20_000)
    @Transactional
    public void tick() {
        List<Quote> currentQuotes = quotes.findAll();
        if (currentQuotes.isEmpty()) {
            return;
        }
        Map<java.util.UUID, Instrument> byId = instruments.findAll().stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        MarketDataProvider provider = providers.active();
        LocalDate today = LocalDate.now();

        for (Quote quote : currentQuotes) {
            Instrument instrument = byId.get(quote.getInstrumentId());
            if (instrument == null) {
                continue;
            }
            BigDecimal next = provider.nextTick(instrument, quote.getLastPrice());
            PriceBar bar = priceBars.findByInstrumentIdAndDate(instrument.getId(), today).orElse(null);
            if (bar == null) {
                BigDecimal open = quote.getLastPrice();
                quote.rollDay(open);
                bar = new PriceBar(instrument.getId(), today, open, open.max(next), open.min(next), next, 1_000);
            } else {
                bar.applyTick(next, 1_000);
            }
            quote.update(next);
            priceBars.save(bar);
        }
        log.debug("Ticked {} instruments", currentQuotes.size());
    }
}
