package com.marketlens.portfolio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.marketlens.support.ApiClient;
import com.marketlens.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PortfolioFlowTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @SuppressWarnings("unchecked")
    private String firstInstrumentId(ApiClient api, String query) {
        ResponseEntity<List> instruments = api.get("/api/instruments?q=" + query, List.class);
        return (String) ((Map<String, Object>) instruments.getBody().get(0)).get("id");
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildAPositionAndReadThePortfolio() {
        ApiClient api = new ApiClient(rest, port).registerAndLogin("trader1");
        String instrumentId = firstInstrumentId(api, "RELIANCE");

        ResponseEntity<Map> buy = api.post("/api/transactions", Map.of(
                "instrumentId", instrumentId,
                "type", "BUY",
                "quantity", 10,
                "price", 2000,
                "fees", 15,
                "tradedOn", LocalDate.now().minusDays(40).toString()), Map.class);
        assertThat(buy.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> portfolio = api.get("/api/portfolio", Map.class);
        assertThat(portfolio.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> holdings = (List<Map<String, Object>>) portfolio.getBody().get("holdings");
        assertThat(holdings).hasSize(1);
        assertThat(holdings.get(0).get("symbol")).isEqualTo("RELIANCE");

        Map<String, Object> summary = (Map<String, Object>) portfolio.getBody().get("summary");
        assertThat(summary.get("holdingsCount")).isEqualTo(1);
    }

    @Test
    void cannotSellMoreThanHeld() {
        ApiClient api = new ApiClient(rest, port).registerAndLogin("trader2");
        String instrumentId = firstInstrumentId(api, "INFY");

        api.post("/api/transactions", Map.of(
                "instrumentId", instrumentId, "type", "BUY", "quantity", 5, "price", 1500,
                "tradedOn", LocalDate.now().minusDays(10).toString()), Map.class);

        ResponseEntity<Map> oversell = api.post("/api/transactions", Map.of(
                "instrumentId", instrumentId, "type", "SELL", "quantity", 8, "price", 1600,
                "tradedOn", LocalDate.now().toString()), Map.class);
        assertThat(oversell.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @SuppressWarnings("unchecked")
    void watchlistsAndAlerts() {
        ApiClient api = new ApiClient(rest, port).registerAndLogin("trader3");
        String instrumentId = firstInstrumentId(api, "TCS");

        ResponseEntity<Map> list = api.post("/api/watchlists", Map.of("name", "Tech"), Map.class);
        String listId = (String) list.getBody().get("id");
        ResponseEntity<Map> withItem = api.post("/api/watchlists/" + listId + "/items",
                Map.of("instrumentId", instrumentId), Map.class);
        assertThat((List<Object>) withItem.getBody().get("items")).hasSize(1);

        ResponseEntity<Map> alert = api.post("/api/alerts", Map.of(
                "instrumentId", instrumentId, "direction", "ABOVE", "threshold", 99999), Map.class);
        assertThat(alert.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(alert.getBody().get("status")).isEqualTo("ACTIVE");
    }

    @Test
    void auditIsAdminOnly() {
        ApiClient user = new ApiClient(rest, port).registerAndLogin("trader4");
        assertThat(user.get("/api/audit", Map.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ApiClient admin = new ApiClient(rest, port);
        admin.loginExisting("admin", "admin12345");
        assertThat(admin.get("/api/audit", Map.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
