package com.marketlens.quote;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marketlens.common.error.NotFoundException;
import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentRepository;
import com.marketlens.quote.dto.QuoteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QuoteService {

    private final QuoteRepository quotes;
    private final InstrumentRepository instruments;

    public QuoteService(QuoteRepository quotes, InstrumentRepository instruments) {
        this.quotes = quotes;
        this.instruments = instruments;
    }

    public List<QuoteResponse> all() {
        Map<UUID, Instrument> byId = instruments.findAll().stream()
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        return quotes.findAll().stream()
                .filter(q -> byId.containsKey(q.getInstrumentId()))
                .map(q -> QuoteResponse.of(byId.get(q.getInstrumentId()), q))
                .sorted(Comparator.comparing(QuoteResponse::symbol))
                .toList();
    }

    public List<QuoteResponse> forSymbols(List<String> symbols) {
        List<String> wanted = symbols.stream().map(String::trim).map(String::toUpperCase).toList();
        Map<UUID, Instrument> byId = instruments.findAll().stream()
                .filter(i -> wanted.contains(i.getSymbol()))
                .collect(Collectors.toMap(Instrument::getId, Function.identity()));
        return quotes.findByInstrumentIdIn(List.copyOf(byId.keySet())).stream()
                .map(q -> QuoteResponse.of(byId.get(q.getInstrumentId()), q))
                .sorted(Comparator.comparing(QuoteResponse::symbol))
                .toList();
    }

    public QuoteResponse forInstrument(UUID instrumentId) {
        Instrument instrument = instruments.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found"));
        Quote quote = quotes.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("No quote yet for this instrument"));
        return QuoteResponse.of(instrument, quote);
    }

    public BigDecimal lastPrice(UUID instrumentId) {
        return quotes.findById(instrumentId)
                .map(Quote::getLastPrice)
                .orElseThrow(() -> new NotFoundException("No quote yet for this instrument"));
    }

    public Map<UUID, BigDecimal> lastPrices() {
        return quotes.findAll().stream()
                .collect(Collectors.toMap(Quote::getInstrumentId, Quote::getLastPrice));
    }
}
