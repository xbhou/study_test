package dev.xbhou.javalab.quote;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class QuoteAggregator {

    private final List<QuoteProvider> providers;
    private final ExecutorService executor;
    private final Duration providerTimeout;

    public QuoteAggregator(List<QuoteProvider> providers,
                           ExecutorService executor,
                           Duration providerTimeout) {
        this.providers = List.copyOf(providers);
        this.executor = executor;
        this.providerTimeout = providerTimeout;
    }

    public Optional<Quote> bestQuote(String symbol) {
        List<CompletableFuture<Quote>> futures = providers.stream()
                .map(provider -> CompletableFuture
                        .supplyAsync(() -> provider.getQuote(symbol), executor)
                        .orTimeout(providerTimeout.toMillis(), TimeUnit.MILLISECONDS)
                        .exceptionally(error -> {
                            System.out.printf(
                                    "%s ignored: %s%n",
                                    provider.name(),
                                    error.getClass().getSimpleName()
                            );
                            return null;
                        }))
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(Quote::price));
    }
}
