package com.marketlens.quote;

import java.util.List;

import com.marketlens.quote.dto.QuoteResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final QuoteStream quoteStream;

    public QuoteController(QuoteService quoteService, QuoteStream quoteStream) {
        this.quoteService = quoteService;
        this.quoteStream = quoteStream;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return quoteStream.open();
    }

    @GetMapping
    public List<QuoteResponse> quotes(@RequestParam(name = "symbols", required = false) String symbols) {
        if (symbols == null || symbols.isBlank()) {
            return quoteService.all();
        }
        return quoteService.forSymbols(List.of(symbols.split(",")));
    }
}
