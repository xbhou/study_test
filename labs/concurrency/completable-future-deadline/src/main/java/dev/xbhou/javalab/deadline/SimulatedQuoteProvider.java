package dev.xbhou.javalab.deadline;

import java.math.BigDecimal;

public class SimulatedQuoteProvider implements QuoteProvider {

    public enum Behavior {
        SUCCESS,
        FAILURE
    }

    private final String name;
    private final BigDecimal price;
    private final long delayMs;
    private final Behavior behavior;

    public SimulatedQuoteProvider(String name,
                                  BigDecimal price,
                                  long delayMs,
                                  Behavior behavior) {
        this.name = name;
        this.price = price;
        this.delayMs = delayMs;
        this.behavior = behavior;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Quote getQuote(String symbol) {
        long start = System.nanoTime();
        System.out.printf("%s started%n", name);

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("%s interrupted%n", name);
            throw new IllegalStateException("Provider interrupted: " + name, e);
        }

        long latencyMs = (System.nanoTime() - start) / 1_000_000;

        if (behavior == Behavior.FAILURE) {
            System.out.printf("%s failed after %d ms%n", name, latencyMs);
            throw new IllegalStateException("Simulated provider failure: " + name);
        }

        System.out.printf("%s finished after %d ms%n", name, latencyMs);
        return new Quote(name, symbol, price, latencyMs);
    }
}
