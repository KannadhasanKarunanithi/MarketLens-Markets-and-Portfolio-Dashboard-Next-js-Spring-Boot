package com.marketlens.watchlist;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "watchlists")
public class Watchlist {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "watchlist_id", nullable = false)
    @OrderBy("position asc")
    private List<WatchlistItem> items = new ArrayList<>();

    protected Watchlist() {
    }

    public Watchlist(UUID userId, String name, int position) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.position = position;
        this.createdAt = Instant.now();
    }

    public void addItem(UUID instrumentId) {
        boolean exists = items.stream().anyMatch(i -> i.getInstrumentId().equals(instrumentId));
        if (exists) {
            return;
        }
        int nextPosition = items.stream().mapToInt(WatchlistItem::getPosition).max().orElse(-1) + 1;
        items.add(new WatchlistItem(instrumentId, nextPosition));
    }

    public void removeItem(UUID instrumentId) {
        items.removeIf(i -> i.getInstrumentId().equals(instrumentId));
        reindex();
    }

    public void reorder(List<UUID> instrumentOrder) {
        items.sort(Comparator.comparingInt(i -> {
            int idx = instrumentOrder.indexOf(i.getInstrumentId());
            return idx < 0 ? Integer.MAX_VALUE : idx;
        }));
        reindex();
    }

    private void reindex() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPosition(i);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<WatchlistItem> getItems() {
        return items;
    }
}
