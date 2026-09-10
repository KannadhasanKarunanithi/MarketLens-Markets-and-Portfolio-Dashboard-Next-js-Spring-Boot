package com.marketlens.pricebar;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentService;
import com.marketlens.pricebar.dto.CandleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PriceBarService {

    private final PriceBarRepository priceBars;
    private final InstrumentService instrumentService;

    public PriceBarService(PriceBarRepository priceBars, InstrumentService instrumentService) {
        this.priceBars = priceBars;
        this.instrumentService = instrumentService;
    }

    public CandleResponse candles(UUID instrumentId, String rangeValue) {
        Instrument instrument = instrumentService.require(instrumentId);
        ChartRange range = ChartRange.parse(rangeValue);
        LocalDate from = range.from(LocalDate.now());

        List<CandleResponse.Candle> candles = priceBars
                .findByInstrumentIdAndDateGreaterThanEqualOrderByDateAsc(instrument.getId(), from).stream()
                .map(bar -> new CandleResponse.Candle(
                        bar.getDate(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume()))
                .toList();
        return new CandleResponse(range.label(), candles);
    }
}
