package com.marketlens.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Helpers for consistent decimal handling. Prices carry four decimal places,
 * money totals two.
 */
public final class Money {

    public static final int PRICE_SCALE = 4;
    public static final int AMOUNT_SCALE = 2;

    private Money() {
    }

    public static BigDecimal price(BigDecimal value) {
        return value.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal amount(BigDecimal value) {
        return value.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal percent(BigDecimal ratio) {
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator, int scale) {
        if (denominator == null || denominator.signum() == 0) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }
        return numerator.divide(denominator, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
