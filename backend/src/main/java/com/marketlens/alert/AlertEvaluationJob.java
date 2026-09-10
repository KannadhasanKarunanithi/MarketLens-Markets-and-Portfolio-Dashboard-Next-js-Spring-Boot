package com.marketlens.alert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.marketlens.quote.QuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AlertEvaluationJob {

    private static final Logger log = LoggerFactory.getLogger(AlertEvaluationJob.class);

    private final AlertRepository alerts;
    private final QuoteService quoteService;

    public AlertEvaluationJob(AlertRepository alerts, QuoteService quoteService) {
        this.alerts = alerts;
        this.quoteService = quoteService;
    }

    @Scheduled(fixedDelayString = "${marketlens.marketdata.tick-interval}", initialDelay = 25_000)
    @Transactional
    public void evaluate() {
        List<Alert> active = alerts.findByStatus(Alert.Status.ACTIVE);
        if (active.isEmpty()) {
            return;
        }
        Map<UUID, BigDecimal> prices = quoteService.lastPrices();
        int triggered = 0;
        for (Alert alert : active) {
            BigDecimal price = prices.get(alert.getInstrumentId());
            if (price != null && alert.isCrossedBy(price)) {
                alert.trigger(price);
                triggered++;
            }
        }
        if (triggered > 0) {
            alerts.saveAll(active);
            log.info("Triggered {} price alerts", triggered);
        }
    }
}
