package com.marketlens.portfolio.bootstrap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.marketlens.alert.Alert;
import com.marketlens.alert.AlertRepository;
import com.marketlens.instrument.Instrument;
import com.marketlens.instrument.InstrumentRepository;
import com.marketlens.transaction.Transaction;
import com.marketlens.transaction.TransactionRepository;
import com.marketlens.transaction.TransactionType;
import com.marketlens.user.UserAccount;
import com.marketlens.user.UserAccountRepository;
import com.marketlens.watchlist.Watchlist;
import com.marketlens.watchlist.WatchlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gives the demo account a ready made portfolio, two watchlists and a couple of
 * price alerts so the dashboard has something to show on first run.
 */
@Component
public class DemoPortfolioSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoPortfolioSeeder.class);

    private final UserAccountRepository users;
    private final InstrumentRepository instruments;
    private final TransactionRepository transactions;
    private final WatchlistRepository watchlists;
    private final AlertRepository alerts;

    public DemoPortfolioSeeder(UserAccountRepository users, InstrumentRepository instruments,
            TransactionRepository transactions, WatchlistRepository watchlists, AlertRepository alerts) {
        this.users = users;
        this.instruments = instruments;
        this.transactions = transactions;
        this.watchlists = watchlists;
        this.alerts = alerts;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(30)
    @Transactional
    public void seed() {
        Optional<UserAccount> demo = users.findByUsername("demo");
        if (demo.isEmpty() || transactions.existsByUserId(demo.get().getId())) {
            return;
        }
        var userId = demo.get().getId();
        Random random = new Random(42);

        List<String> core = List.of("RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK",
                "ITC", "LT", "SUNPHARMA", "TATAMOTORS", "BHARTIARTL");
        for (String symbol : core) {
            Instrument instrument = instruments.findBySymbol(symbol).orElseThrow();
            int buys = 1 + random.nextInt(3);
            for (int i = 0; i < buys; i++) {
                LocalDate date = LocalDate.now().minusDays(30 + random.nextInt(300));
                BigDecimal price = instrument.getReferencePrice()
                        .multiply(BigDecimal.valueOf(0.72 + random.nextDouble() * 0.26))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal quantity = BigDecimal.valueOf(5L + random.nextInt(40));
                transactions.save(new Transaction(userId, instrument.getId(), TransactionType.BUY,
                        quantity, price, BigDecimal.valueOf(20), date, "Opening position"));
            }
        }

        // one trim and one dividend
        Instrument tcs = instruments.findBySymbol("TCS").orElseThrow();
        transactions.save(new Transaction(userId, tcs.getId(), TransactionType.SELL,
                BigDecimal.valueOf(5), tcs.getReferencePrice().multiply(BigDecimal.valueOf(1.04))
                        .setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(20), LocalDate.now().minusDays(20), "Booked some profit"));
        Instrument itc = instruments.findBySymbol("ITC").orElseThrow();
        transactions.save(new Transaction(userId, itc.getId(), TransactionType.DIVIDEND,
                BigDecimal.valueOf(60), BigDecimal.valueOf(6.50), BigDecimal.ZERO,
                LocalDate.now().minusDays(45), "Final dividend"));

        Watchlist bluechips = watchlists.save(new Watchlist(userId, "Blue chips", 0));
        for (String symbol : List.of("RELIANCE", "TCS", "HDFCBANK", "INFY", "LT")) {
            instruments.findBySymbol(symbol).ifPresent(i -> bluechips.addItem(i.getId()));
        }
        Watchlist watching = watchlists.save(new Watchlist(userId, "On the radar", 1));
        for (String symbol : List.of("TATAMOTORS", "MARUTI", "APOLLOHOSP", "TITAN", "JSWSTEEL")) {
            instruments.findBySymbol(symbol).ifPresent(i -> watching.addItem(i.getId()));
        }

        Instrument reliance = instruments.findBySymbol("RELIANCE").orElseThrow();
        alerts.save(new Alert(userId, reliance.getId(), Alert.Direction.ABOVE,
                reliance.getReferencePrice().multiply(BigDecimal.valueOf(1.05))
                        .setScale(2, RoundingMode.HALF_UP),
                "Add on a breakout"));
        Instrument infy = instruments.findBySymbol("INFY").orElseThrow();
        alerts.save(new Alert(userId, infy.getId(), Alert.Direction.BELOW,
                infy.getReferencePrice().multiply(BigDecimal.valueOf(1.20))
                        .setScale(2, RoundingMode.HALF_UP),
                "Watching for a dip"));

        log.info("Seeded the demo portfolio");
    }
}
