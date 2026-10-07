package dev.xbhou.javalab.quote;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {
        List<QuoteProvider> providers = List.of(
                new SimulatedQuoteProvider("Provider-A", new BigDecimal("100.12"), 80),
                new SimulatedQuoteProvider("Provider-B", new BigDecimal("99.98"), 150),
                new SimulatedQuoteProvider("Provider-C", new BigDecimal("99.50"), 600)
        );

        ExecutorService executor = Executors.newFixedThreadPool(providers.size());
        try {
            QuoteAggregator aggregator =
                    new QuoteAggregator(providers, executor, Duration.ofMillis(300));

            aggregator.bestQuote("BTC-USDT")
                    .ifPresentOrElse(
                            quote -> System.out.println("Best quote: " + quote),
                            () -> System.out.println("No valid quote")
                    );
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
