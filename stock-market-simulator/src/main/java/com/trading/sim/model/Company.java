package com.trading.sim.model;

/**
* Represents a listed company. In a richer model, this could include
* financial statements, sector, news stream, etc.
*/
public class Company {
    private final String name;
    private final String ticker;
    private final double initialPrice;
    private final double annualVolatility; // e.g. 0.20 == 20%

    public Company(String name, String ticker, double initialPrice, double annualVolatility) {
        this.name = name;
        this.ticker = ticker;
        this.initialPrice = initialPrice;
        this.annualVolatility = annualVolatility;
    }

    public String getName() { return name; }
    public String getTicker() { return ticker; }
    public double getInitialPrice() { return initialPrice; }
    public double getAnnualVolatility() { return annualVolatility; }
}