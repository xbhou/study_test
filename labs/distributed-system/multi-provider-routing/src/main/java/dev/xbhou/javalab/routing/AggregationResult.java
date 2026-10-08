package dev.xbhou.javalab.routing;

import java.util.List;
import java.util.Optional;

public record AggregationResult(
        List<String> routedProviders,
        List<ProviderOutcome> outcomes,
        Optional<Quote> bestQuote,
        boolean deadlineReached,
        long elapsedMs
) {
}
