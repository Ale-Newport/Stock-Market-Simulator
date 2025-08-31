package com.trading.sim;

import com.trading.sim.market.Market;
import com.trading.sim.model.Company;
import com.trading.sim.trader.Trader;
import com.trading.sim.trader.strategies.MeanReversionStrategy;
import com.trading.sim.trader.strategies.RandomStrategy;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Entry point. Builds a small market with a few symbols and spins up trader threads.
 * All code and comments are in English by request.
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        Random rng = new Random(42);
        try (Market market = new Market(rng)) {
            // List a few companies/symbols
            market.listCompany(new Company("Acme Robotics", "ACME", 100.00, 0.25));
            market.listCompany(new Company("Nimbus Cloud", "NIMB", 55.50, 0.30));
            market.listCompany(new Company("Solaris Energy", "SOLR", 22.15, 0.40));

            market.start();

            // Spin up a handful of traders with different strategies
            ExecutorService pool = Executors.newFixedThreadPool(6);
            Trader t1 = new Trader("T-001", new RandomStrategy(), market, new Random(rng.nextLong()));
            Trader t2 = new Trader("T-002", new RandomStrategy(), market, new Random(rng.nextLong()));
            Trader t3 = new Trader("T-003", new MeanReversionStrategy(), market, new Random(rng.nextLong()));
            Trader t4 = new Trader("T-004", new MeanReversionStrategy(), market, new Random(rng.nextLong()));

            pool.submit(t1);
            pool.submit(t2);
            pool.submit(t3);
            pool.submit(t4);

            // Let the simulation run for a while
            TimeUnit.SECONDS.sleep(20);

            // Stop traders and shutdown
            t1.stop(); t2.stop(); t3.stop(); t4.stop();
            pool.shutdownNow();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }

        System.out.println("Simulation finished.");
    }
}
