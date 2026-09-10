package com.marketlens.watchlist;

import java.util.List;
import java.util.UUID;

import com.marketlens.common.security.CurrentUser;
import com.marketlens.watchlist.dto.WatchlistDtos.AddItemRequest;
import com.marketlens.watchlist.dto.WatchlistDtos.CreateWatchlistRequest;
import com.marketlens.watchlist.dto.WatchlistDtos.RenameWatchlistRequest;
import com.marketlens.watchlist.dto.WatchlistDtos.ReorderRequest;
import com.marketlens.watchlist.dto.WatchlistDtos.WatchlistView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    private UUID currentUserId() {
        return CurrentUser.require().id();
    }

    @GetMapping
    public List<WatchlistView> list() {
        return watchlistService.listFor(currentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WatchlistView create(@Valid @RequestBody CreateWatchlistRequest request) {
        return watchlistService.create(currentUserId(), request.name());
    }

    @PutMapping("/{id}")
    public WatchlistView rename(@PathVariable UUID id, @Valid @RequestBody RenameWatchlistRequest request) {
        return watchlistService.rename(currentUserId(), id, request.name());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        watchlistService.delete(currentUserId(), id);
    }

    @PostMapping("/{id}/items")
    public WatchlistView addItem(@PathVariable UUID id, @Valid @RequestBody AddItemRequest request) {
        return watchlistService.addItem(currentUserId(), id, request.instrumentId());
    }

    @DeleteMapping("/{id}/items/{instrumentId}")
    public WatchlistView removeItem(@PathVariable UUID id, @PathVariable UUID instrumentId) {
        return watchlistService.removeItem(currentUserId(), id, instrumentId);
    }

    @PutMapping("/{id}/order")
    public WatchlistView reorder(@PathVariable UUID id, @Valid @RequestBody ReorderRequest request) {
        return watchlistService.reorder(currentUserId(), id, request.instrumentIds());
    }
}
