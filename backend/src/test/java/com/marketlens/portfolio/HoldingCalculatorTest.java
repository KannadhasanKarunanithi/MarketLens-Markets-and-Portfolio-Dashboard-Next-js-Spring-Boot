package com.marketlens.portfolio;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.marketlens.portfolio.HoldingCalculator.Position;
import com.marketlens.transaction.Transaction;
import com.marketlens.transaction.TransactionType;
import org.junit.jupiter.api.Test;

class HoldingCalculatorTest {

    private final UUID instrument = UUID.randomUUID();
    private final UUID user = UUID.randomUUID();

    private Transaction tx(TransactionType type, String qty, String price, String fees, int daysAgo) {
        return new Transaction(user, instrument, type, new BigDecimal(qty), new BigDecimal(price),
                new BigDecimal(fees), LocalDate.now().minusDays(daysAgo), null);
    }

    @Test
    void averageCostFoldsInBuyFees() {
        Position position = HoldingCalculator.positions(List.of(
                tx(TransactionType.BUY, "10", "100", "10", 10))).get(instrument);

        // (10 * 100 + 10) / 10 = 101.00
        assertThat(position.averageCost()).isEqualByComparingTo("101.00");
        assertThat(position.quantity()).isEqualByComparingTo("10");
    }

    @Test
    void fifoRealisedProfitUsesOldestLotFirst() {
        Position position = HoldingCalculator.positions(List.of(
                tx(TransactionType.BUY, "10", "100", "0", 30),
                tx(TransactionType.BUY, "10", "150", "0", 20),
                tx(TransactionType.SELL, "10", "200", "0", 10))).get(instrument);

        // sold the first lot bought at 100, so realised = 10 * (200 - 100) = 1000
        assertThat(position.realisedPnl()).isEqualByComparingTo("1000");
        assertThat(position.quantity()).isEqualByComparingTo("10");
        assertThat(position.averageCost()).isEqualByComparingTo("150");
    }

    @Test
    void dividendsCountAsRealisedIncome() {
        Position position = HoldingCalculator.positions(List.of(
                tx(TransactionType.BUY, "100", "50", "0", 60),
                tx(TransactionType.DIVIDEND, "100", "2", "0", 10))).get(instrument);

        assertThat(position.dividendIncome()).isEqualByComparingTo("200");
        assertThat(position.realisedPnl()).isEqualByComparingTo("200");
    }
}
