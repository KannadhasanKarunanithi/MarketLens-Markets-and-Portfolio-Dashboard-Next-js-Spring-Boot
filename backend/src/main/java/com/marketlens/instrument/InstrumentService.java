package com.marketlens.instrument;

import java.util.List;
import java.util.UUID;

import com.marketlens.common.error.NotFoundException;
import com.marketlens.instrument.dto.InstrumentResponse;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InstrumentService {

    private final InstrumentRepository instruments;

    public InstrumentService(InstrumentRepository instruments) {
        this.instruments = instruments;
    }

    public List<InstrumentResponse> list(String query) {
        List<Instrument> found = (query == null || query.isBlank())
                ? instruments.findByActiveTrueOrderBySymbolAsc()
                : instruments.search(query.trim(), Limit.of(25));
        return found.stream().map(InstrumentResponse::from).toList();
    }

    public List<InstrumentResponse> tradeable() {
        return instruments.findByAssetClassAndActiveTrueOrderBySymbolAsc(AssetClass.EQUITY).stream()
                .map(InstrumentResponse::from)
                .toList();
    }

    public InstrumentResponse get(UUID id) {
        return InstrumentResponse.from(require(id));
    }

    public Instrument require(UUID id) {
        return instruments.findById(id)
                .orElseThrow(() -> new NotFoundException("Instrument not found"));
    }

    public Instrument requireBySymbol(String symbol) {
        return instruments.findBySymbol(symbol)
                .orElseThrow(() -> new NotFoundException("Instrument " + symbol + " not found"));
    }
}
