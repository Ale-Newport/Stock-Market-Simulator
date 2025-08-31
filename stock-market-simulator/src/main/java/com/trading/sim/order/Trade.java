package com.trading.sim.order;

import java.util.concurrent.atomic.AtomicLong;

/**
* Trade fill produced by the matching engine.
*/
public final class Trade {
    private static final AtomicLong SEQ = new AtomicLong(1);

    private final long id;
    private final String symbol;
    private final long quantity;
    private final double price;
    private final String buyTraderId;
    private final String sellTraderId;
    private final long timestampNanos;

    public Trade(String symbol, long quantity, double price, String buyTraderId, String sellTraderId) {
        this.id = SEQ.getAndIncrement();
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.buyTraderId = buyTraderId;
        this.sellTraderId = sellTraderId;
        this.timestampNanos = System.nanoTime();
    }

    public long getId() { return id; }
    public String getSymbol() { return symbol; }
    public long getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getBuyTraderId() { return buyTraderId; }
    public String getSellTraderId() { return sellTraderId; }
    public long getTimestampNanos() { return timestampNanos; }

    @Override public String toString() {
        return "Trade{" + "id=" + id + ", sym='" + symbol + '\'' + ", qty=" + quantity + ", px=" + price +
        ", B='" + buyTraderId + '\'' + ", S='" + sellTraderId + '\'' + '}';
    }
}