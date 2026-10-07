package dev.xbhou.javalab.deadline;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeadlineQuoteAggregator {

    private final List<QuoteProvider> providers;
    private final ExecutorService executor;
    private final Duration overallDeadline;

    public DeadlineQuoteAggregator(List<QuoteProvider> providers,
                                   ExecutorService executor,
                                   Duration overallDeadline) {
        this.providers = List.copyOf(providers);
        this.executor = executor;
        this.overallDeadline = overallDeadline;
    }

    public AggregationResult aggregate(String symbol) {
        long start = System.nanoTime();

        List<ProviderCall> calls = providers.stream()
                .map(provider -> startCall(provider, symbol))
                .toList();

        CompletableFuture<Void> allOutcomes = CompletableFuture.allOf(
                calls.stream()
                        .map(ProviderCall::outcome)
                        .toArray(CompletableFuture[]::new)
        );

        boolean deadlineReached = false;

        try {
            allOutcomes
                    .orTimeout(overallDeadline.toMillis(), TimeUnit.MILLISECONDS)
                    .join();
        } catch (CompletionException error) {
            if (rootCause(error) instanceof TimeoutException) {
                deadlineReached = true;
            } else {
                throw error;
            }
        }

        if (deadlineReached) {
            for (ProviderCall call : calls) {
                if (!call.outcome().isDone()) {
                    boolean cancelled = call.raw().cancel(true);
                    System.out.printf(
                            "deadline reached: cancel requested for %s, accepted=%s%n",
                            call.provider().name(),
                            cancelled
                    );
                }
            }
        }

        CompletableFuture.allOf(
                calls.stream()
                        .map(ProviderCall::outcome)
                        .toArray(CompletableFuture[]::new)
        ).join();

        List<ProviderOutcome> outcomes = calls.stream()
                .map(call -> call.outcome().join())
                .toList();

        Optional<Quote> bestQuote = outcomes.stream()
                .filter(ProviderOutcome::successful)
                .map(ProviderOutcome::quote)
                .min(Comparator.comparing(Quote::price));

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        return new AggregationResult(
                outcomes,
                bestQuote,
                deadlineReached,
                elapsedMs
        );
    }

    private ProviderCall startCall(QuoteProvider provider, String symbol) {
        CompletableFuture<Quote> raw = CompletableFuture.supplyAsync(
                () -> provider.getQuote(symbol),
                executor
        );

        CompletableFuture<ProviderOutcome> outcome = raw.handle(
                (quote, error) -> error == null
                        ? ProviderOutcome.success(provider.name(), quote)
                        : ProviderOutcome.failure(provider.name(), error)
        );

        return new ProviderCall(provider, raw, outcome);
    }

    private static Throwable rootCause(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private record ProviderCall(
            QuoteProvider provider,
            CompletableFuture<Quote> raw,
            CompletableFuture<ProviderOutcome> outcome
    ) {
    }
}
