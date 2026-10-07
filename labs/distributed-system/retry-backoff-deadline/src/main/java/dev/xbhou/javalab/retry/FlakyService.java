package dev.xbhou.javalab.retry;

import java.util.concurrent.atomic.AtomicInteger;

public class FlakyService {

    private final String name;
    private final int retryableFailuresBeforeSuccess;
    private final boolean permanentFailure;
    private final long latencyMs;
    private final AtomicInteger attempts = new AtomicInteger();

    public FlakyService(
            String name,
            int retryableFailuresBeforeSuccess,
            boolean permanentFailure,
            long latencyMs
    ) {
        this.name = name;
        this.retryableFailuresBeforeSuccess = retryableFailuresBeforeSuccess;
        this.permanentFailure = permanentFailure;
        this.latencyMs = latencyMs;
    }

    public String call() {
        int attempt = attempts.incrementAndGet();

        sleep(latencyMs);

        if (permanentFailure) {
            throw new PermanentException(
                    name + " rejected the request permanently"
            );
        }

        if (attempt <= retryableFailuresBeforeSuccess) {
            throw new RetryableException(
                    name + " transient failure on attempt " + attempt
            );
        }

        return name + " success on attempt " + attempt;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PermanentException("interrupted");
        }
    }
}
