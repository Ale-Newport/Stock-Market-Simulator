package com.trading.sim.trader;

import com.trading.sim.market.Market;
import com.trading.sim.order.Order;
import com.trading.sim.order.Trade;

import java.util.List;
import java.util.Random;

/**
 * A Trader runs on its own thread, periodically asking its Strategy for orders
 * and submitting them to the Market.
 */
public class Trader implements Runnable {
    private final String id;
    private final Strategy strategy;
    private final Market market;
    private final Random rng;
    private volatile boolean running = true;

    public Trader(String id, Strategy strategy, Market market, Random rng) {
        this.id = id;
        this.strategy = strategy;
        this.market = market;
        this.rng = rng;
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                List<Order> orders = strategy.generate(id, market.symbols(), market::lastPrice, rng);
                for (Order o : orders) {
                    List<Trade> fills = market.submit(o);
                    if (!fills.isEmpty()) {
                        System.out.printf("[TRADER %s] Fills: %s%n", id, fills);
                    }
                }
                // Sleep a short, jittered interval to avoid lockstep behavior.
                Thread.sleep(200 + rng.nextInt(400));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
