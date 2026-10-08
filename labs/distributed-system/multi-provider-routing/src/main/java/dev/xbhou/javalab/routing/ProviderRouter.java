package dev.xbhou.javalab.routing;

import java.util.Comparator;
import java.util.List;

public class ProviderRouter {

    private final List<ProviderDefinition> providers;

    public ProviderRouter(List<ProviderDefinition> providers) {
        this.providers = List.copyOf(providers);
    }

    public List<ProviderDefinition> route(QuoteRequest request) {
        return providers.stream()
                .filter(provider -> provider.supports(request))
                .sorted(Comparator
                        .comparingInt(ProviderDefinition::priority)
                        .thenComparing(ProviderDefinition::name))
                .limit(request.maxProviders())
                .toList();
    }
}
