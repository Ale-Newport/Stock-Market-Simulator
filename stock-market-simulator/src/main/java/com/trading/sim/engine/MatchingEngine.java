package com.trading.sim.engine;

import com.trading.sim.order.Order;
import com.trading.sim.order.OrderType;
import com.trading.sim.order.Side;
import com.trading.sim.order.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Single-symbol matching engine operating on one OrderBook.
 * It exposes a synchronous match() method; callers ensure single-threaded access per symbol.
 */
public class MatchingEngine {
    private final OrderBook book;

    public MatchingEngine(OrderBook book) { this.book = book; }

    public List<Trade> match(Order incoming) {
        List<Trade> fills = new ArrayList<>();

        book.lock().lock();
        try {
            if (incoming.getType() == OrderType.MARKET) {
                executeMarket(incoming, fills);
            } else {
                executeLimit(incoming, fills);
            }
        } finally {
            book.lock().unlock();
        }

        return fills;
    }

    private void executeMarket(Order incoming, List<Trade> fills) {
        PriorityQueue<Order> opp = incoming.getSide() == Side.BUY ? book.asks() : book.bids();
        long remaining = incoming.getQuantity();
        while (remaining > 0 && !opp.isEmpty()) {
            Order top = opp.peek();
            long tradeQty = Math.min(remaining, top.getQuantity());
            double price = top.getLimitPrice(); // best available
            fills.add(new Trade(incoming.getSymbol(), tradeQty, price,
                    incoming.getSide() == Side.BUY ? incoming.getTraderId() : top.getTraderId(),
                    incoming.getSide() == Side.SELL ? incoming.getTraderId() : top.getTraderId()));

            remaining -= tradeQty;
            // Remove or reduce top order. Since Order is immutable, we pop and if needed push remainder.
            opp.poll();
            long remainderOpp = top.getQuantity() - tradeQty;
            if (remainderOpp > 0) {
                Order remainder = Order.limit(top.getTraderId(), top.getSymbol(), top.getSide(), remainderOpp, top.getLimitPrice());
                // Preserve original time priority: we cannot change timestamp; this is a simplification.
                opp.add(remainder);
            }
        }
        // Any remaining quantity is unfilled and disappears (IOC behavior for MARKET orders).
    }

    private void executeLimit(Order incoming, List<Trade> fills) {
        PriorityQueue<Order> opp = incoming.getSide() == Side.BUY ? book.asks() : book.bids();
        PriorityQueue<Order> same = incoming.getSide() == Side.BUY ? book.bids() : book.asks();

        long remaining = incoming.getQuantity();
        while (remaining > 0 && !opp.isEmpty()) {
            Order top = opp.peek();
            Double topPx = top.getLimitPrice();
            Double px = incoming.getLimitPrice();
            boolean cross = incoming.getSide() == Side.BUY ? px >= topPx : px <= topPx;
            if (!cross) break;

            long tradeQty = Math.min(remaining, top.getQuantity());
            double price = topPx; // price-time priority => execute at resting order price
            fills.add(new Trade(incoming.getSymbol(), tradeQty, price,
                    incoming.getSide() == Side.BUY ? incoming.getTraderId() : top.getTraderId(),
                    incoming.getSide() == Side.SELL ? incoming.getTraderId() : top.getTraderId()));

            remaining -= tradeQty;
            opp.poll();
            long remainderOpp = top.getQuantity() - tradeQty;
            if (remainderOpp > 0) {
                Order remainder = Order.limit(top.getTraderId(), top.getSymbol(), top.getSide(), remainderOpp, top.getLimitPrice());
                opp.add(remainder);
            }
        }

        if (remaining > 0) {
            Order rest = Order.limit(incoming.getTraderId(), incoming.getSymbol(), incoming.getSide(), remaining, incoming.getLimitPrice());
            same.add(rest);
        }
    }
}
