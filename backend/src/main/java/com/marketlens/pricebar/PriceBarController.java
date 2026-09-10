package com.marketlens.pricebar;

import java.util.UUID;

import com.marketlens.pricebar.dto.CandleResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instruments/{id}/candles")
public class PriceBarController {

    private final PriceBarService priceBarService;

    public PriceBarController(PriceBarService priceBarService) {
        this.priceBarService = priceBarService;
    }

    @GetMapping
    public CandleResponse candles(@PathVariable UUID id,
            @RequestParam(name = "range", required = false) String range) {
        return priceBarService.candles(id, range);
    }
}
