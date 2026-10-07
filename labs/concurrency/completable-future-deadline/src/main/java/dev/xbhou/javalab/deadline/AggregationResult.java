package dev.xbhou.javalab.deadline;

import java.util.List;
import java.util.Optional;

public record AggregationResult(
        List<ProviderOutcome> outcomes,
        Optional<Quote> bestQuote,
        boolean deadlineReached,
        long elapsedMs
) {
}
