package dev.xbhou.javalab.retry;

import java.time.Duration;
import java.util.SplittableRandom;

public class FullJitterBackoff {

    private final SplittableRandom random;

    public FullJitterBackoff(long seed) {
        this.random = new SplittableRandom(seed);
    }

    public Duration delayForRetry(int retryNumber, RetryPolicy policy) {
        if (retryNumber < 1) {
            throw new IllegalArgumentException("retryNumber must be >= 1");
        }

        long initialMs = policy.initialBackoff().toMillis();
        long maxMs = policy.maxBackoff().toMillis();

        long exponentialCap = initialMs;
        for (int i = 1; i < retryNumber; i++) {
            if (exponentialCap >= maxMs / 2) {
                exponentialCap = maxMs;
                break;
            }
            exponentialCap *= 2;
        }

        long capMs = Math.min(exponentialCap, maxMs);
        long delayMs = capMs == 0 ? 0 : random.nextLong(capMs + 1);

        return Duration.ofMillis(delayMs);
    }
}
