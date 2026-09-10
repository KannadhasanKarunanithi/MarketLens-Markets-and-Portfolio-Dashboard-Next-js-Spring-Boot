package com.marketlens.quote.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.marketlens.instrument.Instrument;
import com.marketlens.quote.Quote;

public record QuoteResponse(
        UUID instrumentId,
        String symbol,
        String name,
        BigDecimal lastPrice,
        BigDecimal changeAbs,
        BigDecimal changePct,
        BigDecimal dayOpen,
        BigDecimal dayHigh,
        BigDecimal dayLow,
        BigDecimal prevClose,
        Instant asOf) {

    public static QuoteResponse of(Instrument instrument, Quote quote) {
        return new QuoteResponse(
                instrument.getId(),
                instrument.getSymbol(),
                instrument.getName(),
                quote.getLastPrice(),
                quote.getChangeAbs(),
                quote.getChangePct(),
                quote.getDayOpen(),
                quote.getDayHigh(),
                quote.getDayLow(),
                quote.getPrevClose(),
                quote.getAsOf());
    }
}
