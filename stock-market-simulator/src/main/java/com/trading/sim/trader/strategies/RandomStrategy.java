package com.trading.sim.trader.strategies;

import com.trading.sim.order.Order;
import com.trading.sim.order.Side;
import com.trading.sim.trader.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Places small random limit orders around the current price.
 * This creates background liquidity and occasional trades.
 */
public class RandomStrategy implements Strategy {
    @Override
    public List<Order> generate(String traderId, List<String> symbols, Function<String, Double> lastPrice, Random rng) {
        List<Order> out = new ArrayList<>();
        if (symbols.isEmpty()) return out;
        // 50% chance to do nothing this cycle
        if (rng.nextDouble() < 0.5) return out;

        String sym = symbols.get(rng.nextInt(symbols.size()));
        double p = lastPrice.apply(sym);
        long qty = 1 + rng.nextInt(10);
        double px = p * (1 + (rng.nextDouble() - 0.5) * 0.01); // +/-0.5%
        Side side = rng.nextBoolean() ? Side.BUY : Side.SELL;
        out.add(Order.limit(traderId, sym, side, qty, round2(px)));
        return out;
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
