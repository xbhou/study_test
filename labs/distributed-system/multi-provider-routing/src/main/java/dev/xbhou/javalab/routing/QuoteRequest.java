package dev.xbhou.javalab.routing;

public record QuoteRequest(
        String symbol,
        String scene,
        int maxProviders
) {
    public QuoteRequest {
        if (maxProviders < 1) {
            throw new IllegalArgumentException("maxProviders must be >= 1");
        }
    }
}
