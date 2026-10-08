package dev.xbhou.javalab.routing;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public record ProviderOutcome(
        String provider,
        Status status,
        Quote quote,
        Throwable error
) {
    public enum Status {
        SUCCESS,
        FAILED,
        TIMEOUT,
        CIRCUIT_OPEN,
        CANCELLED
    }

    public static ProviderOutcome success(String provider, Quote quote) {
        return new ProviderOutcome(provider, Status.SUCCESS, quote, null);
    }

    public static ProviderOutcome circuitOpen(String provider) {
        return new ProviderOutcome(provider, Status.CIRCUIT_OPEN, null, null);
    }

    public static ProviderOutcome failure(String provider, Throwable error) {
        Throwable cause = unwrap(error);

        Status status;
        if (cause instanceof TimeoutException) {
            status = Status.TIMEOUT;
        } else if (cause instanceof CancellationException) {
            status = Status.CANCELLED;
        } else {
            status = Status.FAILED;
        }

        return new ProviderOutcome(provider, status, null, cause);
    }

    public boolean successful() {
        return status == Status.SUCCESS;
    }

    private static Throwable unwrap(Throwable error) {
        Throwable current = error;

        while ((current instanceof CompletionException
                || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }
}
