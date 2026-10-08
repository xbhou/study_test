package dev.xbhou.javalab.routing;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulatedQuoteProvider implements QuoteProvider {

    public enum Behavior {
        SUCCESS,
        FAILURE
    }

    private final String name;
    private final BigDecimal price;
    private final long latencyMs;
    private final Behavior behavior;
    private final AtomicInteger invocationCount = new AtomicInteger();

    public SimulatedQuoteProvider(
            String name,
            BigDecimal price,
            long latencyMs,
            Behavior behavior
    ) {
        this.name = name;
        this.price = price;
        this.latencyMs = latencyMs;
        this.behavior = behavior;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Quote quote(QuoteRequest request) {
        invocationCount.incrementAndGet();
        long start = System.nanoTime();

        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("provider interrupted: " + name, error);
        }

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        if (behavior == Behavior.FAILURE) {
            throw new IllegalStateException("simulated provider failure: " + name);
        }

        return new Quote(name, request.symbol(), price, elapsedMs);
    }

    public int invocationCount() {
        return invocationCount.get();
    }
}
