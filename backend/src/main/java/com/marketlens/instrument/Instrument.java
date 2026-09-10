package com.marketlens.instrument;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String exchange;

    @Column(name = "asset_class", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssetClass assetClass;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private String currency;

    @Column(name = "reference_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal referencePrice;

    @Column(nullable = false)
    private boolean active = true;

    protected Instrument() {
    }

    public Instrument(String symbol, String name, String exchange, AssetClass assetClass, String sector,
            String currency, BigDecimal referencePrice) {
        this.id = UUID.randomUUID();
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
        this.assetClass = assetClass;
        this.sector = sector;
        this.currency = currency;
        this.referencePrice = referencePrice;
    }

    public UUID getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getExchange() {
        return exchange;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    public String getSector() {
        return sector;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getReferencePrice() {
        return referencePrice;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isTradeable() {
        return assetClass != AssetClass.INDEX;
    }
}
