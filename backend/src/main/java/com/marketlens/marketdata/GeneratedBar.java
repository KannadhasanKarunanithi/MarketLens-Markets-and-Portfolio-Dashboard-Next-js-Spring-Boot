package com.marketlens.marketdata;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GeneratedBar(
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume) {
}
