package dev.xbhou.javalab.deadline;

public interface QuoteProvider {

    String name();

    Quote getQuote(String symbol);
}
