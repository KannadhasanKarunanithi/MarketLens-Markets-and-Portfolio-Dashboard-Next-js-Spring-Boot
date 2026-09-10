package com.marketlens.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal fees;

    @Column(name = "traded_on", nullable = false)
    private LocalDate tradedOn;

    @Column
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Transaction() {
    }

    public Transaction(UUID userId, UUID instrumentId, TransactionType type, BigDecimal quantity,
            BigDecimal price, BigDecimal fees, LocalDate tradedOn, String note) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.instrumentId = instrumentId;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.fees = fees == null ? BigDecimal.ZERO : fees;
        this.tradedOn = tradedOn;
        this.note = note;
        this.createdAt = Instant.now();
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

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public LocalDate getTradedOn() {
        return tradedOn;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public BigDecimal grossValue() {
        return quantity.multiply(price);
    }
}
