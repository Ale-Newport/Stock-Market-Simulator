package com.trading.sim.trader;

import com.trading.sim.market.Market;
import com.trading.sim.order.Order;
import com.trading.sim.order.Trade;

import java.util.List;
import java.util.Random;

/**
 * Trader that tracks an Account and prints periodic P&L.
 */
public class AccountingTrader implements Runnable {
    private final String id;
    private final Strategy strategy;
    private final Market market;
    private final Random rng;
    private final Account account;
    private volatile boolean running = true;
    private long lastReportMs = System.currentTimeMillis();

    public AccountingTrader(String id, Strategy strategy, Market market, Random rng, double startingCash) {
        this.id = id;
        this.strategy = strategy;
        this.market = market;
        this.rng = rng;
        this.account = new Account(startingCash);
    }

    public String getId() { return id; }
    public Account getAccount() { return account; }

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                List<Order> orders = strategy.generate(id, market.symbols(), market::lastPrice, rng);
                for (Order o : orders) {
                    List<Trade> fills = market.submit(o);
                    if (!fills.isEmpty()) {
                        for (Trade t : fills) {
                            if (id.equals(t.getBuyTraderId()) || id.equals(t.getSellTraderId())) {
                                account.applyFill(t, id);
                            }
                        }
                        System.out.printf("[TRADER %s] Fills: %s%n", id, fills);
                    }
                }

                long now = System.currentTimeMillis();
                if (now - lastReportMs >= 1000) {
                    double pnl = account.unrealizedPnL(market::lastPrice);
                    System.out.printf("[TRADER %s] Cash=%.2f PnL=%.2f Positions=%s%n",
                            id, account.getCash(), pnl, account.positionsSnapshot());
                    lastReportMs = now;
                }

                Thread.sleep(200 + rng.nextInt(400));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
