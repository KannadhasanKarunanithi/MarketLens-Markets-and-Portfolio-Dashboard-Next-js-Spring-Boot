package com.marketlens.alert;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alerts")
public class Alert {

    public enum Direction { ABOVE, BELOW }

    public enum Status { ACTIVE, TRIGGERED }

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal threshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    @Column(name = "price_at_trigger", precision = 18, scale = 4)
    private BigDecimal priceAtTrigger;

    @Column(nullable = false)
    private boolean acknowledged;

    protected Alert() {
    }

    public Alert(UUID userId, UUID instrumentId, Direction direction, BigDecimal threshold, String note) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.instrumentId = instrumentId;
        this.direction = direction;
        this.threshold = threshold;
        this.note = note;
        this.status = Status.ACTIVE;
        this.createdAt = Instant.now();
    }

    public boolean isCrossedBy(BigDecimal price) {
        if (status != Status.ACTIVE) {
            return false;
        }
        return direction == Direction.ABOVE
                ? price.compareTo(threshold) >= 0
                : price.compareTo(threshold) <= 0;
    }

    public void trigger(BigDecimal price) {
        this.status = Status.TRIGGERED;
        this.triggeredAt = Instant.now();
        this.priceAtTrigger = price;
    }

    public void acknowledge() {
        this.acknowledged = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public Direction getDirection() {
        return direction;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public Status getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public BigDecimal getPriceAtTrigger() {
        return priceAtTrigger;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }
}
