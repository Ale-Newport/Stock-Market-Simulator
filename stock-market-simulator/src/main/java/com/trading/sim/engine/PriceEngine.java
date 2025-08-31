package com.trading.sim.engine;

import com.trading.sim.model.Stock;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically perturbs stock mark prices with a simple geometric random walk.
 * This simulates market micro-movements that strategies can react to.
 */
public class PriceEngine {
    private final ScheduledExecutorService scheduler;
    private final Map<String, Stock> stocksBySymbol;
    private final Random rng;

    public PriceEngine(ScheduledExecutorService scheduler, Map<String, Stock> stocksBySymbol, Random rng) {
        this.scheduler = scheduler;
        this.stocksBySymbol = stocksBySymbol;
        this.rng = rng;
    }

    public void start(long periodMillis) {
        scheduler.scheduleAtFixedRate(this::tickAll, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    private void tickAll() {
        // Per tick, apply a small percentage move around 0, bounded to avoid negative prices.
        for (Stock s : stocksBySymbol.values()) {
            double p = s.getMarkPrice();
            double pctMove = rng.nextGaussian() * 0.001; // ~0.1% std dev per tick
            double np = Math.max(0.01, p * (1.0 + pctMove));
            s.setMarkPrice(np);
        }
    }
}
