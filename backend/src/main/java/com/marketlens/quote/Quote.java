package com.marketlens.quote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quotes")
public class Quote {

    @Id
    @Column(name = "instrument_id", nullable = false, updatable = false)
    private UUID instrumentId;

    @Column(name = "last_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal lastPrice;

    @Column(name = "prev_close", nullable = false, precision = 18, scale = 4)
    private BigDecimal prevClose;

    @Column(name = "day_open", nullable = false, precision = 18, scale = 4)
    private BigDecimal dayOpen;

    @Column(name = "day_high", nullable = false, precision = 18, scale = 4)
    private BigDecimal dayHigh;

    @Column(name = "day_low", nullable = false, precision = 18, scale = 4)
    private BigDecimal dayLow;

    @Column(name = "change_abs", nullable = false, precision = 18, scale = 4)
    private BigDecimal changeAbs;

    @Column(name = "change_pct", nullable = false, precision = 10, scale = 4)
    private BigDecimal changePct;

    @Column(name = "as_of", nullable = false)
    private Instant asOf;

    protected Quote() {
    }

    public Quote(UUID instrumentId, BigDecimal price, BigDecimal prevClose, BigDecimal dayOpen) {
        this.instrumentId = instrumentId;
        this.prevClose = prevClose;
        this.dayOpen = dayOpen;
        this.dayHigh = dayOpen.max(price);
        this.dayLow = dayOpen.min(price);
        update(price);
    }

    public void update(BigDecimal price) {
        this.lastPrice = price;
        if (price.compareTo(dayHigh) > 0) {
            this.dayHigh = price;
        }
        if (price.compareTo(dayLow) < 0) {
            this.dayLow = price;
        }
        this.changeAbs = price.subtract(prevClose);
        this.changePct = prevClose.signum() == 0
                ? BigDecimal.ZERO
                : changeAbs.multiply(BigDecimal.valueOf(100))
                        .divide(prevClose, 4, java.math.RoundingMode.HALF_UP);
        this.asOf = Instant.now();
    }

    public void rollDay(BigDecimal newDayOpen) {
        this.prevClose = this.lastPrice;
        this.dayOpen = newDayOpen;
        this.dayHigh = newDayOpen;
        this.dayLow = newDayOpen;
        update(newDayOpen);
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public BigDecimal getPrevClose() {
        return prevClose;
    }

    public BigDecimal getDayOpen() {
        return dayOpen;
    }

    public BigDecimal getDayHigh() {
        return dayHigh;
    }

    public BigDecimal getDayLow() {
        return dayLow;
    }

    public BigDecimal getChangeAbs() {
        return changeAbs;
    }

    public BigDecimal getChangePct() {
        return changePct;
    }

    public Instant getAsOf() {
        return asOf;
    }
}
