package com.trading.sim.engine;

import com.trading.sim.order.Order;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Price–time priority order book with explicit comparators to avoid type inference issues.
 */
public class OrderBook {
    private final String symbol;
    private final PriorityQueue<Order> bids; // highest price first, then earliest time
    private final PriorityQueue<Order> asks; // lowest price first, then earliest time
    private final ReentrantLock lock = new ReentrantLock();

    public OrderBook(String symbol) {
        this.symbol = symbol;

        Comparator<Order> bidCmp = (o1, o2) -> {
            double p1 = o1.getLimitPrice() == null ? Double.NEGATIVE_INFINITY : o1.getLimitPrice();
            double p2 = o2.getLimitPrice() == null ? Double.NEGATIVE_INFINITY : o2.getLimitPrice();
            int pc = Double.compare(p2, p1); // higher price first
            if (pc != 0) return pc;
            return Long.compare(o1.getTimestampNanos(), o2.getTimestampNanos()); // earlier time first
        };

        Comparator<Order> askCmp = (o1, o2) -> {
            double p1 = o1.getLimitPrice() == null ? Double.POSITIVE_INFINITY : o1.getLimitPrice();
            double p2 = o2.getLimitPrice() == null ? Double.POSITIVE_INFINITY : o2.getLimitPrice();
            int pc = Double.compare(p1, p2); // lower price first
            if (pc != 0) return pc;
            return Long.compare(o1.getTimestampNanos(), o2.getTimestampNanos()); // earlier time first
        };

        this.bids = new PriorityQueue<>(bidCmp);
        this.asks = new PriorityQueue<>(askCmp);
    }

    public String getSymbol() { return symbol; }
    public ReentrantLock lock() { return lock; }
    public PriorityQueue<Order> bids() { return bids; }
    public PriorityQueue<Order> asks() { return asks; }

    public Optional<Double> bestBid() {
        lock.lock();
        try {
            Order o = bids.peek();
            return (o == null) ? Optional.empty() : Optional.ofNullable(o.getLimitPrice());
        } finally {
            lock.unlock();
        }
    }

    public Optional<Double> bestAsk() {
        lock.lock();
        try {
            Order o = asks.peek();
            return (o == null) ? Optional.empty() : Optional.ofNullable(o.getLimitPrice());
        } finally {
            lock.unlock();
        }
    }
}
