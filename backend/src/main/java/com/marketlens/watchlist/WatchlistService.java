package com.marketlens.watchlist;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marketlens.common.error.NotFoundException;
import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentRepository;
import com.marketlens.quote.Quote;
import com.marketlens.quote.QuoteRepository;
import com.marketlens.watchlist.dto.WatchlistDtos.WatchlistItemView;
import com.marketlens.watchlist.dto.WatchlistDtos.WatchlistView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WatchlistService {

    private final WatchlistRepository watchlists;
    private final InstrumentRepository instruments;
    private final QuoteRepository quotes;

    public WatchlistService(WatchlistRepository watchlists, InstrumentRepository instruments,
            QuoteRepository quotes) {
        this.watchlists = watchlists;
        this.instruments = instruments;
        this.quotes = quotes;
    }

    @Transactional(readOnly = true)
    public List<WatchlistView> listFor(UUID userId) {
        Map<UUID, Instrument> instrumentById = instruments.findAll().stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        Map<UUID, Quote> quoteById = quotes.findAll().stream()
                .collect(Collectors.toMap(Quote::getInstrumentId, Function.identity()));
        return watchlists.findByUserIdOrderByPositionAscCreatedAtAsc(userId).stream()
                .map(list -> toView(list, instrumentById, quoteById))
                .toList();
    }

    public WatchlistView create(UUID userId, String name) {
        int position = (int) watchlists.countByUserId(userId);
        Watchlist list = watchlists.save(new Watchlist(userId, name, position));
        return toView(list, Map.of(), Map.of());
    }

    public WatchlistView rename(UUID userId, UUID id, String name) {
        Watchlist list = require(userId, id);
        list.setName(name);
        return detailedView(list);
    }

    public void delete(UUID userId, UUID id) {
        watchlists.delete(require(userId, id));
    }

    public WatchlistView addItem(UUID userId, UUID id, UUID instrumentId) {
        if (!instruments.existsById(instrumentId)) {
            throw new NotFoundException("Instrument not found");
        }
        Watchlist list = require(userId, id);
        list.addItem(instrumentId);
        return detailedView(list);
    }

    public WatchlistView removeItem(UUID userId, UUID id, UUID instrumentId) {
        Watchlist list = require(userId, id);
        list.removeItem(instrumentId);
        return detailedView(list);
    }

    public WatchlistView reorder(UUID userId, UUID id, List<UUID> instrumentOrder) {
        Watchlist list = require(userId, id);
        list.reorder(instrumentOrder);
        return detailedView(list);
    }

    private Watchlist require(UUID userId, UUID id) {
        return watchlists.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Watchlist not found"));
    }

    private WatchlistView detailedView(Watchlist list) {
        Map<UUID, Instrument> instrumentById = instruments.findAll().stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        Map<UUID, Quote> quoteById = quotes.findAll().stream()
                .collect(Collectors.toMap(Quote::getInstrumentId, Function.identity()));
        return toView(list, instrumentById, quoteById);
    }

    private WatchlistView toView(Watchlist list, Map<UUID, Instrument> instrumentById,
            Map<UUID, Quote> quoteById) {
        List<WatchlistItemView> items = list.getItems().stream()
                .map(item -> {
                    Instrument instrument = instrumentById.get(item.getInstrumentId());
                    Quote quote = quoteById.get(item.getInstrumentId());
                    return new WatchlistItemView(
                            item.getInstrumentId(),
                            instrument == null ? "?" : instrument.getSymbol(),
                            instrument == null ? "Unknown" : instrument.getName(),
                            instrument == null ? "" : instrument.getSector(),
                            quote == null ? BigDecimal.ZERO : quote.getLastPrice(),
                            quote == null ? BigDecimal.ZERO : quote.getChangeAbs(),
                            quote == null ? BigDecimal.ZERO : quote.getChangePct());
                })
                .toList();
        return new WatchlistView(list.getId(), list.getName(), list.getPosition(), items);
    }
}
