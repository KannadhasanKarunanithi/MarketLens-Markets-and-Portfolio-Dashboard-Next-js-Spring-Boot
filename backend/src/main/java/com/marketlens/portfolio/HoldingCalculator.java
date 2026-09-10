package com.marketlens.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.marketlens.transaction.Transaction;
import com.marketlens.transaction.TransactionType;

/**
 * Turns a transaction ledger into per instrument positions using first in first
 * out cost basis. Buy fees are folded into unit cost, sell fees reduce proceeds.
 */
public final class HoldingCalculator {

    private HoldingCalculator() {
    }

    public static Map<UUID, Position> positions(List<Transaction> ledger) {
        Map<UUID, Position> byInstrument = new LinkedHashMap<>();
        for (Transaction tx : ledger) {
            Position position = byInstrument.computeIfAbsent(tx.getInstrumentId(), id -> new Position());
            switch (tx.getType()) {
                case BUY -> position.buy(tx.getQuantity(), tx.getPrice(), tx.getFees());
                case SELL -> position.sell(tx.getQuantity(), tx.getPrice(), tx.getFees());
                case DIVIDEND -> position.dividend(tx.getQuantity().multiply(tx.getPrice()));
            }
        }
        return byInstrument;
    }

    public static final class Position {
        private final Deque<Lot> lots = new ArrayDeque<>();
        private BigDecimal realisedPnl = BigDecimal.ZERO;
        private BigDecimal dividendIncome = BigDecimal.ZERO;

        void buy(BigDecimal quantity, BigDecimal price, BigDecimal fees) {
            BigDecimal gross = quantity.multiply(price).add(fees == null ? BigDecimal.ZERO : fees);
            BigDecimal unitCost = gross.divide(quantity, 6, RoundingMode.HALF_UP);
            lots.addLast(new Lot(quantity, unitCost));
        }

        void sell(BigDecimal quantity, BigDecimal price, BigDecimal fees) {
            BigDecimal remaining = quantity;
            BigDecimal costConsumed = BigDecimal.ZERO;
            while (remaining.signum() > 0 && !lots.isEmpty()) {
                Lot lot = lots.peekFirst();
                BigDecimal take = lot.quantity.min(remaining);
                costConsumed = costConsumed.add(take.multiply(lot.unitCost));
                lot.quantity = lot.quantity.subtract(take);
                remaining = remaining.subtract(take);
                if (lot.quantity.signum() == 0) {
                    lots.pollFirst();
                }
            }
            BigDecimal proceeds = quantity.multiply(price).subtract(fees == null ? BigDecimal.ZERO : fees);
            realisedPnl = realisedPnl.add(proceeds.subtract(costConsumed));
        }

        void dividend(BigDecimal amount) {
            dividendIncome = dividendIncome.add(amount);
            realisedPnl = realisedPnl.add(amount);
        }

        public BigDecimal quantity() {
            return lots.stream().map(l -> l.quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public BigDecimal investedValue() {
            return lots.stream().map(l -> l.quantity.multiply(l.unitCost))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public BigDecimal averageCost() {
            BigDecimal qty = quantity();
            if (qty.signum() == 0) {
                return BigDecimal.ZERO;
            }
            return investedValue().divide(qty, 4, RoundingMode.HALF_UP);
        }

        public BigDecimal realisedPnl() {
            return realisedPnl;
        }

        public BigDecimal dividendIncome() {
            return dividendIncome;
        }

        public boolean isOpen() {
            return quantity().signum() > 0;
        }
    }

    private static final class Lot {
        private BigDecimal quantity;
        private final BigDecimal unitCost;

        Lot(BigDecimal quantity, BigDecimal unitCost) {
            this.quantity = quantity;
            this.unitCost = unitCost;
        }
    }
}
