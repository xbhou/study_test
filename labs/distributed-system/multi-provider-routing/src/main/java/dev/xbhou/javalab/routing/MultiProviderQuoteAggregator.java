package dev.xbhou.javalab.routing;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MultiProviderQuoteAggregator {

    private final ProviderRouter router;
    private final ExecutorService executor;
    private final Duration providerTimeout;
    private final Duration overallDeadline;

    public MultiProviderQuoteAggregator(
            ProviderRouter router,
            ExecutorService executor,
            Duration providerTimeout,
            Duration overallDeadline
    ) {
        this.router = router;
        this.executor = executor;
        this.providerTimeout = providerTimeout;
        this.overallDeadline = overallDeadline;
    }

    public AggregationResult aggregate(QuoteRequest request) {
        long start = System.nanoTime();

        List<ProviderDefinition> routed = router.route(request);
        List<ProviderCall> calls = new ArrayList<>();

        for (ProviderDefinition provider : routed) {
            calls.add(startCall(provider, request));
        }

        CompletableFuture<Void> all = CompletableFuture.allOf(
                calls.stream()
                        .map(ProviderCall::outcome)
                        .toArray(CompletableFuture[]::new)
        );

        boolean deadlineReached = false;

        try {
            all.orTimeout(
                    overallDeadline.toMillis(),
                    TimeUnit.MILLISECONDS
            ).join();
        } catch (CompletionException error) {
            if (rootCause(error) instanceof TimeoutException) {
                deadlineReached = true;
            } else {
                throw error;
            }
        }

        if (deadlineReached) {
            for (ProviderCall call : calls) {
                if (call.raw() != null && !call.outcome().isDone()) {
                    call.raw().cancel(true);
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

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - start
        );

        return new AggregationResult(
                routed.stream().map(ProviderDefinition::name).toList(),
                outcomes,
                bestQuote,
                deadlineReached,
                elapsedMs
        );
    }

    private ProviderCall startCall(
            ProviderDefinition provider,
            QuoteRequest request
    ) {
        ProviderCircuitBreaker.Permit permit =
                provider.circuitBreaker().acquirePermission();

        if (!permit.allowed()) {
            CompletableFuture<ProviderOutcome> skipped =
                    CompletableFuture.completedFuture(
                            ProviderOutcome.circuitOpen(provider.name())
                    );

            return new ProviderCall(null, skipped);
        }

        CompletableFuture<Quote> raw = CompletableFuture
                .supplyAsync(
                        () -> provider.provider().quote(request),
                        executor
                )
                .orTimeout(
                        providerTimeout.toMillis(),
                        TimeUnit.MILLISECONDS
                );

        CompletableFuture<ProviderOutcome> outcome = raw.handle(
                (quote, error) -> {
                    if (error == null) {
                        provider.circuitBreaker().onSuccess(permit);
                        return ProviderOutcome.success(
                                provider.name(),
                                quote
                        );
                    }

                    provider.circuitBreaker().onFailure(permit);
                    return ProviderOutcome.failure(
                            provider.name(),
                            error
                    );
                }
        );

        return new ProviderCall(raw, outcome);
    }

    private static Throwable rootCause(Throwable error) {
        Throwable current = error;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }

    private record ProviderCall(
            CompletableFuture<Quote> raw,
            CompletableFuture<ProviderOutcome> outcome
    ) {
    }
}
