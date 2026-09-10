package com.marketlens.marketdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import com.marketlens.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class MarketDataTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void instrumentsAreSeededAndSearchable() {
        List<?> all = rest.getForObject(url("/api/instruments"), List.class);
        assertThat(all).hasSizeGreaterThan(30);

        List<?> search = rest.getForObject(url("/api/instruments?q=bank"), List.class);
        assertThat(search).isNotEmpty();
    }

    @Test
    void quotesAreSeededForEveryInstrument() {
        List<?> quotes = rest.getForObject(url("/api/quotes"), List.class);
        assertThat(quotes).hasSizeGreaterThan(30);
    }

    @Test
    void candlesReturnHistoryForAnInstrument() {
        List<Map<String, Object>> instruments = rest.getForObject(url("/api/instruments?q=RELIANCE"), List.class);
        String id = (String) instruments.get(0).get("id");

        Map<?, ?> candles = rest.getForObject(url("/api/instruments/" + id + "/candles?range=1m"), Map.class);
        assertThat(candles.get("range")).isEqualTo("1m");
        assertThat((List<?>) candles.get("candles")).isNotEmpty();
    }
}
