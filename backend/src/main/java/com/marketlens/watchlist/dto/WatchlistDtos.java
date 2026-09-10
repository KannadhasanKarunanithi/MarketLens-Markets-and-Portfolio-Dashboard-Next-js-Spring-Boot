package com.marketlens.watchlist.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class WatchlistDtos {

    private WatchlistDtos() {
    }

    public record CreateWatchlistRequest(@NotBlank @Size(max = 60) String name) {
    }

    public record RenameWatchlistRequest(@NotBlank @Size(max = 60) String name) {
    }

    public record AddItemRequest(@NotNull UUID instrumentId) {
    }

    public record ReorderRequest(@NotNull List<UUID> instrumentIds) {
    }

    public record WatchlistItemView(
            UUID instrumentId,
            String symbol,
            String name,
            String sector,
            BigDecimal lastPrice,
            BigDecimal changeAbs,
            BigDecimal changePct) {
    }

    public record WatchlistView(
            UUID id,
            String name,
            int position,
            List<WatchlistItemView> items) {
    }
}
