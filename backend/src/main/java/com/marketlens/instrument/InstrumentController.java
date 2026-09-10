package com.marketlens.instrument;

import java.util.List;
import java.util.UUID;

import com.marketlens.instrument.dto.InstrumentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public List<InstrumentResponse> list(@RequestParam(name = "q", required = false) String query) {
        return instrumentService.list(query);
    }

    @GetMapping("/tradeable")
    public List<InstrumentResponse> tradeable() {
        return instrumentService.tradeable();
    }

    @GetMapping("/{id}")
    public InstrumentResponse get(@PathVariable UUID id) {
        return instrumentService.get(id);
    }
}
