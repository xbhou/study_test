package dev.xbhou.javalab.retry;

import java.time.Duration;

public class App {

    private static final RetryPolicy POLICY = new RetryPolicy(
            4,
            Duration.ofMillis(100),
            Duration.ofMillis(500)
    );

    public static void main(String[] args) {
        transientSuccessScenario();
        permanentFailureScenario();
        deadlineExhaustedScenario();
    }

    private static void transientSuccessScenario() {
        System.out.println("=== Scenario 1: transient failure eventually succeeds ===");

        FlakyService service = new FlakyService(
                "TransientService",
                2,
                false,
                40
        );

        RetryExecutor executor = new RetryExecutor(
                new FullJitterBackoff(42)
        );

        RetryResult<String> result = executor.execute(
                service::call,
                POLICY,
                Duration.ofSeconds(2),
                error -> error instanceof RetryableException
        );

        print(result);
    }

    private static void permanentFailureScenario() {
        System.out.println("=== Scenario 2: permanent failure stops immediately ===");

        FlakyService service = new FlakyService(
                "PermanentService",
                0,
                true,
                30
        );

        RetryExecutor executor = new RetryExecutor(
                new FullJitterBackoff(42)
        );

        RetryResult<String> result = executor.execute(
                service::call,
                POLICY,
                Duration.ofSeconds(2),
                error -> error instanceof RetryableException
        );

        print(result);
    }

    private static void deadlineExhaustedScenario() {
        System.out.println("=== Scenario 3: deadline prevents another retry ===");

        FlakyService service = new FlakyService(
                "SlowTransientService",
                10,
                false,
                120
        );

        RetryExecutor executor = new RetryExecutor(
                new FullJitterBackoff(7)
        );

        RetryResult<String> result = executor.execute(
                service::call,
                POLICY,
                Duration.ofMillis(260),
                error -> error instanceof RetryableException
        );

        print(result);
    }

    private static void print(RetryResult<String> result) {
        System.out.println(
                "status=" + result.status()
                        + ", attempts=" + result.attempts()
                        + ", elapsed=" + result.elapsedMs() + " ms"
                        + ", value=" + result.value()
        );
        System.out.println();
    }
}
