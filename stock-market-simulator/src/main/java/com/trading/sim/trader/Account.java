package com.trading.sim.trader;

import com.trading.sim.order.Trade;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Simple account: tracks cash and per-symbol positions.
 * P&L is computed as (cash + sum(pos * lastPrice)) - initialCash.
 */
public class Account {
    private final double initialCash;
    private double cash;
    private final Map<String, Long> positions = new HashMap<>();

    public Account(double startingCash) {
        this.initialCash = startingCash;
        this.cash = startingCash;
    }

    public synchronized void applyFill(Trade t, String traderId) {
        long qty = t.getQuantity();
        double px = t.getPrice();

        if (traderId.equals(t.getBuyTraderId())) {
            // Buy -> spend cash, increase position
            cash -= px * qty;
            positions.merge(t.getSymbol(), qty, Long::sum);
        } else if (traderId.equals(t.getSellTraderId())) {
            // Sell -> receive cash, decrease position
            cash += px * qty;
            positions.merge(t.getSymbol(), -qty, Long::sum);
        }
    }

    public synchronized double netLiq(Function<String, Double> lastPrice) {
        double value = cash;
        for (Map.Entry<String, Long> e : positions.entrySet()) {
            value += e.getValue() * lastPrice.apply(e.getKey());
        }
        return value;
    }

    public synchronized double unrealizedPnL(Function<String, Double> lastPrice) {
        return netLiq(lastPrice) - initialCash;
    }

    public synchronized double getCash() { return cash; }

    public synchronized long getPosition(String symbol) {
        return positions.getOrDefault(symbol, 0L);
    }

    public synchronized Map<String, Long> positionsSnapshot() {
        return Collections.unmodifiableMap(new HashMap<>(positions));
    }
}
