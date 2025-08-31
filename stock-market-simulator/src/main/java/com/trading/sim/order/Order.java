package com.trading.sim.order;

import java.util.concurrent.atomic.AtomicLong;

/**
* Immutable order object. Instances are created by traders and submitted to the market.
*/
public final class Order {
    private static final AtomicLong SEQ = new AtomicLong(1);

    private final long id;
    private final String traderId;
    private final String symbol;
    private final Side side;
    private final OrderType type;
    private final long quantity; // shares
    private final Double limitPrice; // null when MARKET
    private final long timestampNanos; // time priority

    private Order(long id, String traderId, String symbol, Side side, OrderType type,
    long quantity, Double limitPrice, long timestampNanos) {
        this.id = id;
        this.traderId = traderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.timestampNanos = timestampNanos;
    }

    public static Order market(String traderId, String symbol, Side side, long quantity) {
        return new Order(SEQ.getAndIncrement(), traderId, symbol, side, OrderType.MARKET, quantity, null,
        System.nanoTime());
    }

    public static Order limit(String traderId, String symbol, Side side, long quantity, double price) {
        return new Order(SEQ.getAndIncrement(), traderId, symbol, side, OrderType.LIMIT, quantity, price,
        System.nanoTime());
    }

    public long getId() { return id; }
    public String getTraderId() { return traderId; }
    public String getSymbol() { return symbol; }
    public Side getSide() { return side; }
    public OrderType getType() { return type; }
    public long getQuantity() { return quantity; }
    public Double getLimitPrice() { return limitPrice; }
    public long getTimestampNanos() { return timestampNanos; }

    @Override public String toString() {
        return "Order{" +
        "id=" + id +
        ", trader='" + traderId + '\'' +
        ", sym='" + symbol + '\'' +
        ", side=" + side +
        ", type=" + type +
        ", qty=" + quantity +
        (limitPrice != null ? ", px=" + limitPrice : "") +
        '}';
    }
}