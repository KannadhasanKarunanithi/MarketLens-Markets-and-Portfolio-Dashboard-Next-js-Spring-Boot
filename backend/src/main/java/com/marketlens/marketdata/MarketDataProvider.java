package com.marketlens.marketdata;

import java.math.BigDecimal;
import java.util.List;

import com.marketlens.instrument.Instrument;

/**
 * Source of price history and price movement. The default implementation is a
 * simulator so the app runs with no external dependency. A real provider can be
 * added behind this interface and selected with configuration.
 */
public interface MarketDataProvider {

    String name();

    List<GeneratedBar> backfill(Instrument instrument, int days);

    BigDecimal nextTick(Instrument instrument, BigDecimal currentPrice);
}
