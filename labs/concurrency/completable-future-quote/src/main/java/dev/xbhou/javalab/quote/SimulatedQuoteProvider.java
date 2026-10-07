package dev.xbhou.javalab.quote;

import java.math.BigDecimal;

public class SimulatedQuoteProvider implements QuoteProvider {

    private final String name;
    private final BigDecimal price;
    private final long delayMs;

    public SimulatedQuoteProvider(String name, BigDecimal price, long delayMs) {
        this.name = name;
        this.price = price;
        this.delayMs = delayMs;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Quote getQuote(String symbol) {
        long start = System.nanoTime();
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Provider call interrupted: " + name, e);
        }

        long latencyMs = (System.nanoTime() - start) / 1_000_000;
        return new Quote(name, symbol, price, latencyMs);
    }
}
