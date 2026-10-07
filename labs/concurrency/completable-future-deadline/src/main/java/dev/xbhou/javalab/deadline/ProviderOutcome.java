package dev.xbhou.javalab.deadline;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public record ProviderOutcome(String provider, Quote quote, Throwable error) {

    public static ProviderOutcome success(String provider, Quote quote) {
        return new ProviderOutcome(provider, quote, null);
    }

    public static ProviderOutcome failure(String provider, Throwable error) {
        return new ProviderOutcome(provider, null, unwrap(error));
    }

    public boolean successful() {
        return quote != null;
    }

    public boolean cancelled() {
        return error instanceof java.util.concurrent.CancellationException;
    }

    public String status() {
        if (successful()) {
            return "SUCCESS";
        }
        if (cancelled()) {
            return "CANCELLED";
        }
        return "FAILED(" + error.getClass().getSimpleName() + ")";
    }

    private static Throwable unwrap(Throwable error) {
        Throwable current = error;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
