package com.trading.sim.trader;

import com.trading.sim.order.Order;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * A trading strategy proposes zero or more orders when invoked.
 */
public interface Strategy {
    List<Order> generate(String traderId,
                         List<String> symbols,
                         Function<String, Double> lastPrice,
                         Random rng);
}
