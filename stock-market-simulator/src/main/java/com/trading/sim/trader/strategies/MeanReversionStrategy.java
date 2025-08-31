package com.trading.sim.trader.strategies;

import com.trading.sim.order.Order;
import com.trading.sim.order.Side;
import com.trading.sim.trader.Strategy;

import java.util.*;
import java.util.function.Function;

/**
 * Very simple mean-reversion: if price ticked up since last seen, try sell slightly above;
 * if ticked down, try buy slightly below. Keeps per-symbol last price memory.
 */
public class MeanReversionStrategy implements Strategy {
    private final Map<String, Double> lastSeen = new HashMap<>();

    @Override
    public java.util.List<Order> generate(String traderId, java.util.List<String> symbols, Function<String, Double> lastPrice, java.util.Random rng) {
        java.util.List<Order> out = new java.util.ArrayList<>();
        for (String sym : symbols) {
            double p = lastPrice.apply(sym);
            Double prev = lastSeen.put(sym, p);
            if (prev == null) continue;
            double diff = p - prev;
            if (Math.abs(diff) < prev * 0.0005) continue; // ignore tiny moves

            if (diff > 0) {
                // price went up -> sell near top
                double px = p * (1 + 0.001); // 0.1% above
                out.add(Order.limit(traderId, sym, Side.SELL, 5, round2(px)));
            } else {
                // price went down -> buy near bottom
                double px = p * (1 - 0.001);
                out.add(Order.limit(traderId, sym, Side.BUY, 5, round2(px)));
            }
            // Limit number of orders per cycle
            if (out.size() >= 2) break;
        }
        return out;
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
