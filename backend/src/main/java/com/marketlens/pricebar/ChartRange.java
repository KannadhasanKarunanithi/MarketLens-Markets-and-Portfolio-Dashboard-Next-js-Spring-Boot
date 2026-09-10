package com.marketlens.pricebar;

import java.time.LocalDate;

public enum ChartRange {
    FIVE_DAY(7),
    ONE_MONTH(31),
    THREE_MONTH(93),
    SIX_MONTH(186),
    ONE_YEAR(372),
    MAX(100_000);

    private final int calendarDays;

    ChartRange(int calendarDays) {
        this.calendarDays = calendarDays;
    }

    public LocalDate from(LocalDate today) {
        return today.minusDays(calendarDays);
    }

    public static ChartRange parse(String value) {
        if (value == null) {
            return SIX_MONTH;
        }
        return switch (value.trim().toLowerCase()) {
            case "5d" -> FIVE_DAY;
            case "1m" -> ONE_MONTH;
            case "3m" -> THREE_MONTH;
            case "6m" -> SIX_MONTH;
            case "1y" -> ONE_YEAR;
            case "max", "all" -> MAX;
            default -> SIX_MONTH;
        };
    }

    public String label() {
        return switch (this) {
            case FIVE_DAY -> "5d";
            case ONE_MONTH -> "1m";
            case THREE_MONTH -> "3m";
            case SIX_MONTH -> "6m";
            case ONE_YEAR -> "1y";
            case MAX -> "max";
        };
    }
}
