package com.marketlens.pricebar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "price_bars")
public class PriceBar implements Persistable<UUID> {

    @Transient
    private boolean freshlyCreated;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Column(name = "bar_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal open;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal high;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal low;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal close;

    @Column(nullable = false)
    private long volume;

    protected PriceBar() {
    }

    public PriceBar(UUID instrumentId, LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low,
            BigDecimal close, long volume) {
        this.id = UUID.randomUUID();
        this.instrumentId = instrumentId;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.freshlyCreated = true;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return freshlyCreated;
    }

    @PostLoad
    @PostPersist
    void markPersisted() {
        this.freshlyCreated = false;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    public void applyTick(BigDecimal price, long extraVolume) {
        this.close = price;
        if (price.compareTo(this.high) > 0) {
            this.high = price;
        }
        if (price.compareTo(this.low) < 0) {
            this.low = price;
        }
        this.volume += extraVolume;
    }
}
