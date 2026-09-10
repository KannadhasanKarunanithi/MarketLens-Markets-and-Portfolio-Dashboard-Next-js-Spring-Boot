package com.marketlens.marketdata;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "marketlens.marketdata")
public class MarketDataProperties {

    /** Which provider to use: simulated, or a real provider name. */
    private String provider = "simulated";

    /** How many trading days of history to generate on a fresh database. */
    private int seedDays = 220;

    /** How often the simulator moves prices. */
    private Duration tickInterval = Duration.ofSeconds(15);

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getSeedDays() {
        return seedDays;
    }

    public void setSeedDays(int seedDays) {
        this.seedDays = seedDays;
    }

    public Duration getTickInterval() {
        return tickInterval;
    }

    public void setTickInterval(Duration tickInterval) {
        this.tickInterval = tickInterval;
    }
}
