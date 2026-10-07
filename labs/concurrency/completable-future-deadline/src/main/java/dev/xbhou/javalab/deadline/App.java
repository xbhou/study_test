package dev.xbhou.javalab.deadline;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static dev.xbhou.javalab.deadline.SimulatedQuoteProvider.Behavior.FAILURE;
import static dev.xbhou.javalab.deadline.SimulatedQuoteProvider.Behavior.SUCCESS;

public class App {

    public static void main(String[] args) throws InterruptedException {
        List<QuoteProvider> providers = List.of(
                new SimulatedQuoteProvider("Provider-A", new BigDecimal("100.12"), 80, SUCCESS),
                new SimulatedQuoteProvider("Provider-D", new BigDecimal("100.05"), 120, FAILURE),
                new SimulatedQuoteProvider("Provider-B", new BigDecimal("99.98"), 220, SUCCESS),
                new SimulatedQuoteProvider("Provider-C", new BigDecimal("99.50"), 800, SUCCESS)
        );

        ExecutorService executor = Executors.newFixedThreadPool(providers.size());

        try {
            DeadlineQuoteAggregator aggregator =
                    new DeadlineQuoteAggregator(
                            providers,
                            executor,
                            Duration.ofMillis(300)
                    );

            AggregationResult result = aggregator.aggregate("BTC-USDT");

            System.out.println();
            System.out.printf(
                    "Aggregation returned in %d ms, deadlineReached=%s%n",
                    result.elapsedMs(),
                    result.deadlineReached()
            );

            for (ProviderOutcome outcome : result.outcomes()) {
                System.out.printf(
                        "%-10s -> %s%n",
                        outcome.provider(),
                        outcome.status()
                );
            }

            System.out.println();
            result.bestQuote().ifPresentOrElse(
                    quote -> System.out.println("Best available quote: " + quote),
                    () -> System.out.println("No valid quote")
            );

            System.out.println();
            System.out.println(
                    "Waiting to show that logical CompletableFuture cancellation "
                            + "does not necessarily stop the running supplier..."
            );

            Thread.sleep(650);
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}
