package com.marketlens.common.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final String version;

    public MetaController(@Value("${marketlens.version:dev}") String version) {
        this.version = version;
    }

    @GetMapping
    public Map<String, Object> meta() {
        return Map.of(
                "name", "MarketLens",
                "version", version,
                "time", Instant.now().toString());
    }
}
