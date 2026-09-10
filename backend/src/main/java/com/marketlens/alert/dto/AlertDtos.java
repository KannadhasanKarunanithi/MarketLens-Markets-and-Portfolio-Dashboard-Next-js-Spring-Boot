package com.marketlens.alert.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.marketlens.alert.Alert;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class AlertDtos {

    private AlertDtos() {
    }

    public record CreateAlertRequest(
            @NotNull UUID instrumentId,
            @NotNull Alert.Direction direction,
            @NotNull @Positive BigDecimal threshold,
            @Size(max = 200) String note) {
    }

    public record AlertResponse(
            UUID id,
            UUID instrumentId,
            String symbol,
            String name,
            String direction,
            BigDecimal threshold,
            String status,
            String note,
            Instant createdAt,
            Instant triggeredAt,
            BigDecimal priceAtTrigger,
            boolean acknowledged) {

        public static AlertResponse of(Alert alert, String symbol, String name) {
            return new AlertResponse(
                    alert.getId(),
                    alert.getInstrumentId(),
                    symbol,
                    name,
                    alert.getDirection().name(),
                    alert.getThreshold(),
                    alert.getStatus().name(),
                    alert.getNote(),
                    alert.getCreatedAt(),
                    alert.getTriggeredAt(),
                    alert.getPriceAtTrigger(),
                    alert.isAcknowledged());
        }
    }
}
