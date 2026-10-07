package dev.xbhou.javalab.retry;

public record RetryResult<T>(
        Status status,
        T value,
        Throwable error,
        int attempts,
        long elapsedMs
) {
    public enum Status {
        SUCCESS,
        NON_RETRYABLE_FAILURE,
        MAX_ATTEMPTS_EXHAUSTED,
        DEADLINE_EXHAUSTED
    }
}
