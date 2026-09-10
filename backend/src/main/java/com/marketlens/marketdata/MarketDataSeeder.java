package com.marketlens.marketdata;

import java.util.ArrayList;
import java.util.List;

import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentRepository;
import com.marketlens.pricebar.PriceBar;
import com.marketlens.pricebar.PriceBarRepository;
import com.marketlens.quote.Quote;
import com.marketlens.quote.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * On a fresh database, generates price history and an opening quote for every
 * instrument so the charts and the portfolio have something to show.
 */
@Component
public class MarketDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(MarketDataSeeder.class);

    private final InstrumentRepository instruments;
    private final PriceBarRepository priceBars;
    private final QuoteRepository quotes;
    private final MarketDataProviders providers;
    private final MarketDataProperties properties;

    public MarketDataSeeder(InstrumentRepository instruments, PriceBarRepository priceBars,
            QuoteRepository quotes, MarketDataProviders providers, MarketDataProperties properties) {
        this.instruments = instruments;
        this.priceBars = priceBars;
        this.quotes = quotes;
        this.providers = providers;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(10)
    @Transactional
    public void seed() {
        if (priceBars.countAll() > 0) {
            return;
        }
        MarketDataProvider provider = providers.active();
        int days = properties.getSeedDays();
        List<PriceBar> allBars = new ArrayList<>();
        List<Quote> allQuotes = new ArrayList<>();

        for (Instrument instrument : instruments.findAll()) {
            List<GeneratedBar> generated = provider.backfill(instrument, days);
            if (generated.isEmpty()) {
                continue;
            }
            for (GeneratedBar bar : generated) {
                allBars.add(new PriceBar(instrument.getId(), bar.date(), bar.open(), bar.high(), bar.low(),
                        bar.close(), bar.volume()));
            }
            GeneratedBar last = generated.get(generated.size() - 1);
            GeneratedBar prev = generated.size() > 1 ? generated.get(generated.size() - 2) : last;
            Quote quote = new Quote(instrument.getId(), last.close(), prev.close(), last.open());
            allQuotes.add(quote);
        }

        priceBars.saveAll(allBars);
        quotes.saveAll(allQuotes);
        log.info("Seeded {} price bars and {} quotes across {} instruments",
                allBars.size(), allQuotes.size(), instruments.count());
    }
}
