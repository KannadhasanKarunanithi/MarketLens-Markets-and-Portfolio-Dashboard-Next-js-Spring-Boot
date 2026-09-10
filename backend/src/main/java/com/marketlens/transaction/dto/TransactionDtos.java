package com.marketlens.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.marketlens.transaction.Transaction;
import com.marketlens.transaction.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class TransactionDtos {

    private TransactionDtos() {
    }

    public record RecordTransactionRequest(
            @NotNull UUID instrumentId,
            @NotNull TransactionType type,
            @NotNull @Positive BigDecimal quantity,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @DecimalMin("0.0") BigDecimal fees,
            @NotNull @PastOrPresent LocalDate tradedOn,
            @Size(max = 200) String note) {
    }

    public record TransactionResponse(
            UUID id,
            UUID instrumentId,
            String symbol,
            String name,
            String type,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal fees,
            BigDecimal grossValue,
            LocalDate tradedOn,
            String note) {

        public static TransactionResponse of(Transaction tx, String symbol, String name) {
            return new TransactionResponse(
                    tx.getId(),
                    tx.getInstrumentId(),
                    symbol,
                    name,
                    tx.getType().name(),
                    tx.getQuantity(),
                    tx.getPrice(),
                    tx.getFees(),
                    tx.grossValue(),
                    tx.getTradedOn(),
                    tx.getNote());
        }
    }
}
