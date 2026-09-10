package com.marketlens.instrument.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.marketlens.instrument.Instrument;

public record InstrumentResponse(
        UUID id,
        String symbol,
        String name,
        String exchange,
        String assetClass,
        String sector,
        String currency,
        BigDecimal referencePrice,
        boolean tradeable) {

    public static InstrumentResponse from(Instrument instrument) {
        return new InstrumentResponse(
                instrument.getId(),
                instrument.getSymbol(),
                instrument.getName(),
                instrument.getExchange(),
                instrument.getAssetClass().name(),
                instrument.getSector(),
                instrument.getCurrency(),
                instrument.getReferencePrice(),
                instrument.isTradeable());
    }
}
