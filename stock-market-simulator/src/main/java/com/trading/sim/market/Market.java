package com.trading.sim.market;

import com.trading.sim.engine.MatchingEngine;
import com.trading.sim.engine.OrderBook;
import com.trading.sim.engine.PriceEngine;
import com.trading.sim.model.Company;
import com.trading.sim.model.Stock;
import com.trading.sim.order.Order;
import com.trading.sim.order.Trade;

import java.util.*;
import java.util.concurrent.*;

/**
 * Orchestrates stocks, order books, matching engines and background price engine.
 * Provides a thread-safe gateway for traders.
 */
public class Market implements AutoCloseable {
    private final Map<String, Stock> stocks = new ConcurrentHashMap<>();
    private final Map<String, OrderBook> books = new ConcurrentHashMap<>();
    private final Map<String, MatchingEngine> engines = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService matcherPool = Executors.newCachedThreadPool();

    private final Random rng;
    private PriceEngine priceEngine;

    public Market(Random rng) {
        this.rng = rng;
    }

    public void listCompany(Company c) {
        stocks.putIfAbsent(c.getTicker(), new Stock(c.getTicker(), c.getInitialPrice()));
        books.putIfAbsent(c.getTicker(), new OrderBook(c.getTicker()));
        engines.putIfAbsent(c.getTicker(), new MatchingEngine(books.get(c.getTicker())));
    }

    public void start() {
        this.priceEngine = new PriceEngine(scheduler, stocks, rng);
        priceEngine.start(200); // 5 ticks per second
    }

    public double lastPrice(String symbol) { return stocks.get(symbol).getMarkPrice(); }

    public List<String> symbols() { return new ArrayList<>(stocks.keySet()); }

    /**
     * Synchronously submit an order to the per-symbol matching engine.
     * For realism you could queue per symbol; here we keep it simple and synchronous.
     */
    public List<Trade> submit(Order o) {
        MatchingEngine me = engines.get(o.getSymbol());
        if (me == null) throw new IllegalArgumentException("Unknown symbol: " + o.getSymbol());
        return me.match(o);
    }

    @Override public void close() {
        scheduler.shutdownNow();
        matcherPool.shutdownNow();
    }
}
