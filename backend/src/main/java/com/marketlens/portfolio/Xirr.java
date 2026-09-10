package com.marketlens.portfolio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Money weighted return. Solves for the annual rate that makes the net present
 * value of the dated cash flows zero, using bisection so it stays stable.
 */
public final class Xirr {

    private Xirr() {
    }

    public record CashFlow(LocalDate date, double amount) {
    }

    public static double annualRate(List<CashFlow> flows) {
        if (flows.size() < 2) {
            return 0;
        }
        LocalDate start = flows.get(0).date();
        boolean hasNegative = flows.stream().anyMatch(f -> f.amount() < 0);
        boolean hasPositive = flows.stream().anyMatch(f -> f.amount() > 0);
        if (!hasNegative || !hasPositive) {
            return 0;
        }

        double low = -0.9999;
        double high = 10.0;
        double npvLow = npv(flows, start, low);
        double npvHigh = npv(flows, start, high);
        if (npvLow * npvHigh > 0) {
            return 0;
        }
        for (int i = 0; i < 200; i++) {
            double mid = (low + high) / 2;
            double npvMid = npv(flows, start, mid);
            if (Math.abs(npvMid) < 1e-6) {
                return mid;
            }
            if (npvLow * npvMid < 0) {
                high = mid;
                npvHigh = npvMid;
            } else {
                low = mid;
                npvLow = npvMid;
            }
        }
        return (low + high) / 2;
    }

    private static double npv(List<CashFlow> flows, LocalDate start, double rate) {
        double total = 0;
        for (CashFlow flow : flows) {
            double years = ChronoUnit.DAYS.between(start, flow.date()) / 365.0;
            total += flow.amount() / Math.pow(1 + rate, years);
        }
        return total;
    }
}
