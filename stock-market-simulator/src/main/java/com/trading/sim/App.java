package com.trading.sim;

import com.trading.sim.market.Market;
import com.trading.sim.model.Company;
import com.trading.sim.trader.AccountingTrader;
import com.trading.sim.trader.strategies.MeanReversionStrategy;
import com.trading.sim.trader.strategies.RandomStrategy;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) throws InterruptedException {
        Random rng = new Random(42);
        try (Market market = new Market(rng)) {
            market.listCompany(new Company("Acme Robotics", "ACME", 100.00, 0.25));
            market.listCompany(new Company("Nimbus Cloud", "NIMB", 55.50, 0.30));
            market.listCompany(new Company("Solaris Energy", "SOLR", 22.15, 0.40));
            market.start();

            ExecutorService pool = Executors.newFixedThreadPool(6);
            AccountingTrader t1 = new AccountingTrader("T-001", new RandomStrategy(),        market, new Random(rng.nextLong()), 100_000);
            AccountingTrader t2 = new AccountingTrader("T-002", new RandomStrategy(),        market, new Random(rng.nextLong()), 100_000);
            AccountingTrader t3 = new AccountingTrader("T-003", new MeanReversionStrategy(), market, new Random(rng.nextLong()), 100_000);
            AccountingTrader t4 = new AccountingTrader("T-004", new MeanReversionStrategy(), market, new Random(rng.nextLong()), 100_000);

            pool.submit(t1); pool.submit(t2); pool.submit(t3); pool.submit(t4);

            TimeUnit.SECONDS.sleep(20);

            t1.stop(); t2.stop(); t3.stop(); t4.stop();
            pool.shutdownNow();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
        System.out.println("Simulation finished.");
    }
}
