package dev.xbhou.javalab.routing;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static dev.xbhou.javalab.routing.SimulatedQuoteProvider.Behavior.FAILURE;
import static dev.xbhou.javalab.routing.SimulatedQuoteProvider.Behavior.SUCCESS;

public class App {

    public static void main(String[] args) throws Exception {
        routingTimeoutAndBestQuote();
        sceneBasedDynamicRouting();
        circuitBreakerIsolation();
        overallDeadlineReturnsPartialResult();
    }

    private static void routingTimeoutAndBestQuote() {
        System.out.println("=== Scenario 1: routing + timeout + best quote ===");

        SystemTimeSource clock = new SystemTimeSource();

        SimulatedQuoteProvider a =
                new SimulatedQuoteProvider(
                        "Provider-A",
                        new BigDecimal("100.10"),
                        80,
                        SUCCESS
                );

        SimulatedQuoteProvider b =
                new SimulatedQuoteProvider(
                        "Provider-B",
                        new BigDecimal("99.95"),
                        120,
                        SUCCESS
                );

        SimulatedQuoteProvider c =
                new SimulatedQuoteProvider(
                        "Provider-C",
                        new BigDecimal("99.80"),
                        500,
                        SUCCESS
                );

        SimulatedQuoteProvider d =
                new SimulatedQuoteProvider(
                        "Provider-D",
                        new BigDecimal("99.70"),
                        50,
                        SUCCESS
                );

        List<ProviderDefinition> providers = List.of(
                definition(a, true, Set.of("BTC-USDT"), Set.of("SPOT"), 1, clock),
                definition(b, true, Set.of("BTC-USDT", "ETH-USDT"), Set.of("SPOT"), 2, clock),
                definition(c, true, Set.of("BTC-USDT"), Set.of("SPOT"), 3, clock),
                definition(d, true, Set.of("BTC-USDT"), Set.of("VIP"), 1, clock)
        );

        AggregationResult result = aggregate(
                providers,
                new QuoteRequest("BTC-USDT", "SPOT", 3),
                Duration.ofMillis(250),
                Duration.ofMillis(500)
        );

        print(result);
    }

    private static void sceneBasedDynamicRouting() {
        System.out.println("=== Scenario 2: scene-based dynamic routing ===");

        SystemTimeSource clock = new SystemTimeSource();

        SimulatedQuoteProvider spot =
                new SimulatedQuoteProvider(
                        "Spot-Provider",
                        new BigDecimal("100.20"),
                        30,
                        SUCCESS
                );

        SimulatedQuoteProvider vip =
                new SimulatedQuoteProvider(
                        "VIP-Provider",
                        new BigDecimal("99.60"),
                        30,
                        SUCCESS
                );

        List<ProviderDefinition> providers = List.of(
                definition(spot, true, Set.of("BTC-USDT"), Set.of("SPOT"), 1, clock),
                definition(vip, true, Set.of("BTC-USDT"), Set.of("VIP"), 1, clock)
        );

        AggregationResult result = aggregate(
                providers,
                new QuoteRequest("BTC-USDT", "VIP", 3),
                Duration.ofMillis(200),
                Duration.ofMillis(400)
        );

        print(result);
    }

    private static void circuitBreakerIsolation() {
        System.out.println("=== Scenario 3: circuit breaker isolates failing provider ===");

        SystemTimeSource clock = new SystemTimeSource();

        SimulatedQuoteProvider failing =
                new SimulatedQuoteProvider(
                        "Failing-Provider",
                        new BigDecimal("99.50"),
                        20,
                        FAILURE
                );

        SimulatedQuoteProvider healthy =
                new SimulatedQuoteProvider(
                        "Healthy-Provider",
                        new BigDecimal("100.00"),
                        30,
                        SUCCESS
                );

        ProviderDefinition failingDefinition =
                definition(
                        failing,
                        true,
                        Set.of("BTC-USDT"),
                        Set.of("SPOT"),
                        1,
                        clock
                );

        ProviderDefinition healthyDefinition =
                definition(
                        healthy,
                        true,
                        Set.of("BTC-USDT"),
                        Set.of("SPOT"),
                        2,
                        clock
                );

        List<ProviderDefinition> providers =
                List.of(failingDefinition, healthyDefinition);

        QuoteRequest request =
                new QuoteRequest("BTC-USDT", "SPOT", 2);

        aggregate(
                providers,
                request,
                Duration.ofMillis(200),
                Duration.ofMillis(400)
        );

        aggregate(
                providers,
                request,
                Duration.ofMillis(200),
                Duration.ofMillis(400)
        );

        int beforeThirdRequest = failing.invocationCount();

        AggregationResult third = aggregate(
                providers,
                request,
                Duration.ofMillis(200),
                Duration.ofMillis(400)
        );

        print(third);

        System.out.println(
                "failing provider real invocations during third request: "
                        + (failing.invocationCount() - beforeThirdRequest)
        );
        System.out.println(
                "failing provider circuit state: "
                        + failingDefinition.circuitBreaker().state()
        );
        System.out.println();
    }

    private static void overallDeadlineReturnsPartialResult()
            throws InterruptedException {
        System.out.println("=== Scenario 4: overall deadline returns partial result ===");

        SystemTimeSource clock = new SystemTimeSource();

        SimulatedQuoteProvider a =
                new SimulatedQuoteProvider(
                        "Provider-A",
                        new BigDecimal("100.10"),
                        80,
                        SUCCESS
                );

        SimulatedQuoteProvider b =
                new SimulatedQuoteProvider(
                        "Provider-B",
                        new BigDecimal("99.95"),
                        220,
                        SUCCESS
                );

        SimulatedQuoteProvider c =
                new SimulatedQuoteProvider(
                        "Provider-C",
                        new BigDecimal("99.70"),
                        700,
                        SUCCESS
                );

        List<ProviderDefinition> providers = List.of(
                definition(a, true, Set.of("BTC-USDT"), Set.of("SPOT"), 1, clock),
                definition(b, true, Set.of("BTC-USDT"), Set.of("SPOT"), 2, clock),
                definition(c, true, Set.of("BTC-USDT"), Set.of("SPOT"), 3, clock)
        );

        AggregationResult result = aggregate(
                providers,
                new QuoteRequest("BTC-USDT", "SPOT", 3),
                Duration.ofMillis(1000),
                Duration.ofMillis(300)
        );

        print(result);

        Thread.sleep(450);
    }

    private static AggregationResult aggregate(
            List<ProviderDefinition> providers,
            QuoteRequest request,
            Duration providerTimeout,
            Duration overallDeadline
    ) {
        ExecutorService executor =
                Executors.newFixedThreadPool(
                        Math.max(1, providers.size())
                );

        try {
            MultiProviderQuoteAggregator aggregator =
                    new MultiProviderQuoteAggregator(
                            new ProviderRouter(providers),
                            executor,
                            providerTimeout,
                            overallDeadline
                    );

            return aggregator.aggregate(request);
        } finally {
            executor.shutdown();

            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException error) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static ProviderDefinition definition(
            QuoteProvider provider,
            boolean enabled,
            Set<String> symbols,
            Set<String> scenes,
            int priority,
            TimeSource clock
    ) {
        return new ProviderDefinition(
                provider,
                enabled,
                symbols,
                scenes,
                priority,
                new ProviderCircuitBreaker(
                        2,
                        Duration.ofSeconds(30),
                        clock
                )
        );
    }

    private static void print(AggregationResult result) {
        System.out.println("routed providers: " + result.routedProviders());

        for (ProviderOutcome outcome : result.outcomes()) {
            System.out.printf(
                    "%-18s -> %-12s %s%n",
                    outcome.provider(),
                    outcome.status(),
                    outcome.quote() == null
                            ? ""
                            : "price=" + outcome.quote().price()
            );
        }

        System.out.println(
                "best quote: "
                        + result.bestQuote()
                        .map(quote ->
                                quote.provider()
                                        + " / "
                                        + quote.price()
                        )
                        .orElse("NONE")
        );

        System.out.println(
                "deadlineReached="
                        + result.deadlineReached()
                        + ", elapsed="
                        + result.elapsedMs()
                        + " ms"
        );
        System.out.println();
    }
}
