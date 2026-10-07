package dev.xbhou.javalab.retry;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class RetryExecutor {

    private final FullJitterBackoff backoff;

    public RetryExecutor(FullJitterBackoff backoff) {
        this.backoff = backoff;
    }

    public <T> RetryResult<T> execute(
            Callable<T> operation,
            RetryPolicy policy,
            Duration overallDeadline,
            Predicate<Throwable> retryable
    ) {
        long start = System.nanoTime();
        long deadline = start + overallDeadline.toNanos();

        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            long remainingBeforeAttempt = deadline - System.nanoTime();
            if (remainingBeforeAttempt <= 0) {
                return result(
                        RetryResult.Status.DEADLINE_EXHAUSTED,
                        null,
                        null,
                        attempt - 1,
                        start
                );
            }

            System.out.printf(
                    "attempt=%d remainingBudget=%d ms%n",
                    attempt,
                    TimeUnit.NANOSECONDS.toMillis(remainingBeforeAttempt)
            );

            try {
                T value = operation.call();
                return result(
                        RetryResult.Status.SUCCESS,
                        value,
                        null,
                        attempt,
                        start
                );
            } catch (Throwable error) {
                System.out.printf(
                        "attempt=%d failed: %s%n",
                        attempt,
                        error.getClass().getSimpleName()
                );

                if (!retryable.test(error)) {
                    return result(
                            RetryResult.Status.NON_RETRYABLE_FAILURE,
                            null,
                            error,
                            attempt,
                            start
                    );
                }

                if (attempt == policy.maxAttempts()) {
                    return result(
                            RetryResult.Status.MAX_ATTEMPTS_EXHAUSTED,
                            null,
                            error,
                            attempt,
                            start
                    );
                }

                int retryNumber = attempt;
                Duration delay = backoff.delayForRetry(retryNumber, policy);
                long remainingAfterFailure = deadline - System.nanoTime();

                System.out.printf(
                        "retry=%d backoff=%d ms remainingAfterFailure=%d ms%n",
                        retryNumber,
                        delay.toMillis(),
                        TimeUnit.NANOSECONDS.toMillis(Math.max(0, remainingAfterFailure))
                );

                if (delay.toNanos() >= remainingAfterFailure) {
                    return result(
                            RetryResult.Status.DEADLINE_EXHAUSTED,
                            null,
                            error,
                            attempt,
                            start
                    );
                }

                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return result(
                            RetryResult.Status.NON_RETRYABLE_FAILURE,
                            null,
                            interrupted,
                            attempt,
                            start
                    );
                }
            }
        }

        throw new IllegalStateException("unreachable");
    }

    private static <T> RetryResult<T> result(
            RetryResult.Status status,
            T value,
            Throwable error,
            int attempts,
            long startNanos
    ) {
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - startNanos
        );

        return new RetryResult<>(
                status,
                value,
                error,
                attempts,
                elapsedMs
        );
    }
}
