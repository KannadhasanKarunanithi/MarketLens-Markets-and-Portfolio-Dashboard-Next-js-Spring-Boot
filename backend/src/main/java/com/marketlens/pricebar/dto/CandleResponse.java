package com.marketlens.pricebar.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CandleResponse(String range, List<Candle> candles) {

    public record Candle(
            LocalDate date,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            long volume) {
    }
}
