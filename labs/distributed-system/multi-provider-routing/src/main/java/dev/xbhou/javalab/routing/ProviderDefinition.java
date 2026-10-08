package dev.xbhou.javalab.routing;

import java.util.Set;

public record ProviderDefinition(
        QuoteProvider provider,
        boolean enabled,
        Set<String> supportedSymbols,
        Set<String> supportedScenes,
        int priority,
        ProviderCircuitBreaker circuitBreaker
) {
    public ProviderDefinition {
        supportedSymbols = Set.copyOf(supportedSymbols);
        supportedScenes = Set.copyOf(supportedScenes);
    }

    public String name() {
        return provider.name();
    }

    public boolean supports(QuoteRequest request) {
        return enabled
                && supportedSymbols.contains(request.symbol())
                && supportedScenes.contains(request.scene());
    }
}
