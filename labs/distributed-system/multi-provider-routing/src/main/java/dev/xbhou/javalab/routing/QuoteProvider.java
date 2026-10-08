package dev.xbhou.javalab.routing;

public interface QuoteProvider {

    String name();

    Quote quote(QuoteRequest request);
}
