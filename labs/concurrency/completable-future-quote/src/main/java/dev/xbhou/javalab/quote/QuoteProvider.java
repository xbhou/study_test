package dev.xbhou.javalab.quote;

public interface QuoteProvider {

    String name();

    Quote getQuote(String symbol);
}
