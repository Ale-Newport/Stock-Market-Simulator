package com.trading.sim.model;

import java.util.concurrent.atomic.AtomicReference;

/**
* Thread-safe holder for the current mark price of a stock.
*/
public class Stock {
    private final String ticker;
    private final AtomicReference<Double> markPrice = new AtomicReference<>();

    public Stock(String ticker, double initialPrice) {
        this.ticker = ticker;
        this.markPrice.set(initialPrice);
    }

    public String getTicker() { return ticker; }

    public double getMarkPrice() { return markPrice.get(); }

    public void setMarkPrice(double newPrice) { markPrice.set(newPrice); }
}