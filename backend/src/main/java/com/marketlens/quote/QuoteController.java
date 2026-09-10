package com.marketlens.quote;

import java.util.List;

import com.marketlens.quote.dto.QuoteResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping
    public List<QuoteResponse> quotes(@RequestParam(name = "symbols", required = false) String symbols) {
        if (symbols == null || symbols.isBlank()) {
            return quoteService.all();
        }
        return quoteService.forSymbols(List.of(symbols.split(",")));
    }
}
