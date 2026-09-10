package com.marketlens.marketdata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resolves the active market data provider by name from configuration, falling
 * back to the simulator.
 */
@Component
public class MarketDataProviders {

    private static final Logger log = LoggerFactory.getLogger(MarketDataProviders.class);

    private final MarketDataProvider active;

    public MarketDataProviders(List<MarketDataProvider> providers, MarketDataProperties properties) {
        this.active = providers.stream()
                .filter(p -> p.name().equalsIgnoreCase(properties.getProvider()))
                .findFirst()
                .orElseGet(() -> providers.stream()
                        .filter(p -> p.name().equals("simulated"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No market data provider available")));
        log.info("Using market data provider: {}", active.name());
    }

    public MarketDataProvider active() {
        return active;
    }
}
