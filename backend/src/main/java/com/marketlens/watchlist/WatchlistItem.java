package com.marketlens.watchlist;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "watchlist_items")
public class WatchlistItem {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Column(nullable = false)
    private int position;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    protected WatchlistItem() {
    }

    public WatchlistItem(UUID instrumentId, int position) {
        this.id = UUID.randomUUID();
        this.instrumentId = instrumentId;
        this.position = position;
        this.addedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
